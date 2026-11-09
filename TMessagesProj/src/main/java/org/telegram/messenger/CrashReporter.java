package org.telegram.messenger;

import android.app.ActivityManager;
import android.app.ApplicationExitInfo;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.List;

/**
 * Collects a copyable error report about the previous crash at next start (logcat of own uid + exit info),
 * copies it to the clipboard and saves it to Downloads/XNagram-hata-raporu.txt.
 */
public class CrashReporter {

    public static void checkAndReport(final Context context) {
        final Thread t = new Thread(() -> {
            try {
                final String report = build(context);
                if (report == null) {
                    return;
                }
                save(context, report);
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    try {
                        ClipboardManager cm = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        cm.setPrimaryClip(ClipData.newPlainText("XNagram hata raporu", report));
                        Toast.makeText(context, "Önceki çökme raporu panoya kopyalandı ve Download klasörüne yazıldı", Toast.LENGTH_LONG).show();
                    } catch (Throwable ignore) {
                    }
                }, 1500);
            } catch (Throwable ignore) {
            }
        }, "CrashReporter");
        t.setPriority(Thread.MIN_PRIORITY);
        t.start();
    }

    private static String build(Context context) throws Exception {
        StringBuilder sb = new StringBuilder();
        boolean crashed = false;
        sb.append("XNagram hata raporu\n");
        sb.append("Cihaz: ").append(Build.MANUFACTURER).append(' ').append(Build.MODEL)
                .append(" Android ").append(Build.VERSION.RELEASE).append(" (SDK ").append(Build.VERSION.SDK_INT).append(")\n");
        sb.append("ABI: ").append(Build.SUPPORTED_ABIS[0]).append("\n");
        sb.append("Surum: ").append(BuildVars.BUILD_VERSION_STRING).append("\n\n");

        if (Build.VERSION.SDK_INT >= 30) {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<ApplicationExitInfo> list = am.getHistoricalProcessExitReasons(null, 0, 3);
            for (ApplicationExitInfo i : list) {
                int r = i.getReason();
                boolean bad = r == ApplicationExitInfo.REASON_CRASH || r == ApplicationExitInfo.REASON_CRASH_NATIVE
                        || r == ApplicationExitInfo.REASON_ANR || r == ApplicationExitInfo.REASON_SIGNALED
                        || r == ApplicationExitInfo.REASON_INITIALIZATION_FAILURE;
                if (!bad) {
                    continue;
                }
                crashed = true;
                sb.append("Cikis: reason=").append(r).append(" status=").append(i.getStatus())
                        .append(" desc=").append(i.getDescription()).append(" time=").append(i.getTimestamp()).append("\n");
                if (r == ApplicationExitInfo.REASON_CRASH_NATIVE || r == ApplicationExitInfo.REASON_ANR) {
                    try (InputStream in = i.getTraceInputStream()) {
                        if (in != null) {
                            ByteArrayOutputStream bo = new ByteArrayOutputStream();
                            byte[] buf = new byte[8192];
                            int n, total = 0;
                            while ((n = in.read(buf)) > 0 && total < 300000) {
                                bo.write(buf, 0, n);
                                total += n;
                            }
                            sb.append("Iz (yazdirilabilir kisimlar):\n").append(printable(bo.toByteArray(), 7000)).append("\n");
                        }
                    } catch (Throwable ignore) {
                    }
                }
            }
        }

        // own-uid logcat (includes the "FATAL EXCEPTION" / "Fatal signal" lines of the previous process)
        Process p = Runtime.getRuntime().exec(new String[]{"logcat", "-d", "-v", "time", "-b", "main,crash", "-t", "1500", "*:W"});
        BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
        StringBuilder log = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            if (line.contains("FATAL EXCEPTION") || line.contains("Fatal signal") || line.contains("AndroidRuntime")
                    || line.contains("UnsatisfiedLink") || line.contains("backtrace") || line.contains("Caused by")
                    || line.contains("\tat ") || line.contains("DEBUG") || line.contains("libc") || line.contains("tmessages")) {
                if (line.contains("FATAL EXCEPTION") || line.contains("Fatal signal")) {
                    crashed = true;
                }
                log.append(line).append('\n');
                if (log.length() > 40000) {
                    break;
                }
            }
        }
        if (!crashed) {
            return null;
        }
        sb.append("\nLogcat:\n").append(log);
        return sb.toString();
    }

    private static String printable(byte[] data, int max) {
        StringBuilder out = new StringBuilder();
        StringBuilder cur = new StringBuilder();
        for (byte b : data) {
            if (b >= 32 && b < 127) {
                cur.append((char) b);
            } else {
                if (cur.length() >= 6) {
                    out.append(cur).append('\n');
                }
                cur.setLength(0);
                if (out.length() > max) {
                    break;
                }
            }
        }
        return out.length() > max ? out.substring(0, max) : out.toString();
    }

    private static void save(Context context, String report) {
        try {
            if (Build.VERSION.SDK_INT >= 29) {
                ContentValues v = new ContentValues();
                v.put(MediaStore.Downloads.DISPLAY_NAME, "XNagram-hata-raporu.txt");
                v.put(MediaStore.Downloads.MIME_TYPE, "text/plain");
                v.put(MediaStore.Downloads.RELATIVE_PATH, "Download");
                Uri uri = context.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, v);
                if (uri != null) {
                    try (OutputStream os = context.getContentResolver().openOutputStream(uri)) {
                        os.write(report.getBytes("UTF-8"));
                    }
                }
            }
        } catch (Throwable ignore) {
        }
    }
}
