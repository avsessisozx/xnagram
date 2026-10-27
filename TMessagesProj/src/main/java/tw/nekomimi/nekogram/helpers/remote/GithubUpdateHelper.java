package tw.nekomimi.nekogram.helpers.remote;

import android.os.Build;

import org.json.JSONArray;
import org.json.JSONObject;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.Utilities;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Checks this fork's own GitHub releases for a newer version.
 * Release tag format: "v<versionCode>" (for example v1262). Title and body carry the human readable text.
 */
public class GithubUpdateHelper {

    public static final String REPO = "avsessisozx/xnagram";
    public static final String RELEASES_PAGE = "https://github.com/" + REPO + "/releases";
    private static final String API_URL = "https://api.github.com/repos/" + REPO + "/releases?per_page=10";

    public static class Result {
        public final int versionCode;
        public final String title;
        public final String notes;
        public final String apkUrl;
        public final String pageUrl;
        public final boolean newer;

        Result(int versionCode, String title, String notes, String apkUrl, String pageUrl) {
            this.versionCode = versionCode;
            this.title = title;
            this.notes = notes;
            this.apkUrl = apkUrl;
            this.pageUrl = pageUrl;
            this.newer = versionCode > BuildConfig.VERSION_CODE;
        }
    }

    public interface Callback {
        /**
         * Called on a background thread. result is null if no release was found, error is null on success.
         */
        void onDone(Result result, String error);
    }

    public static void check(boolean includePreReleases, Callback callback) {
        Utilities.globalQueue.postRunnable(() -> {
            HttpURLConnection connection = null;
            try {
                connection = (HttpURLConnection) new URL(API_URL).openConnection();
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setRequestProperty("Accept", "application/vnd.github+json");
                connection.setRequestProperty("User-Agent", "XNagram/" + BuildConfig.VERSION_NAME);
                int code = connection.getResponseCode();
                if (code != 200) {
                    callback.onDone(null, "HTTP " + code);
                    return;
                }
                String body = readAll(connection.getInputStream());
                JSONArray releases = new JSONArray(body);
                for (int i = 0; i < releases.length(); i++) {
                    JSONObject release = releases.getJSONObject(i);
                    if (release.optBoolean("draft")) {
                        continue;
                    }
                    if (release.optBoolean("prerelease") && !includePreReleases) {
                        continue;
                    }
                    int versionCode = parseVersionCode(release.optString("tag_name"));
                    if (versionCode <= 0) {
                        continue;
                    }
                    callback.onDone(new Result(
                            versionCode,
                            release.optString("name", release.optString("tag_name")),
                            release.optString("body", ""),
                            pickApk(release.optJSONArray("assets")),
                            release.optString("html_url", RELEASES_PAGE)
                    ), null);
                    return;
                }
                callback.onDone(null, null);
            } catch (Exception e) {
                FileLog.e(e);
                callback.onDone(null, String.valueOf(e.getMessage()));
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        });
    }

    private static int parseVersionCode(String tag) {
        if (tag == null) {
            return -1;
        }
        String digits = tag.startsWith("v") || tag.startsWith("V") ? tag.substring(1) : tag;
        try {
            return Integer.parseInt(digits.trim());
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String pickApk(JSONArray assets) {
        if (assets == null) {
            return null;
        }
        String universal = null;
        String preferred = null;
        for (int i = 0; i < assets.length(); i++) {
            JSONObject asset = assets.optJSONObject(i);
            if (asset == null) {
                continue;
            }
            String name = asset.optString("name");
            String url = asset.optString("browser_download_url");
            if (!name.endsWith(".apk") || url.isEmpty()) {
                continue;
            }
            for (String abi : Build.SUPPORTED_ABIS) {
                if (name.contains(abi) && preferred == null) {
                    preferred = url;
                }
            }
            if (name.contains("universal")) {
                universal = url;
            }
        }
        return preferred != null ? preferred : universal;
    }

    private static String readAll(InputStream in) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        }
    }
}
