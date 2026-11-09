#!/bin/bash
mkdir -p emu_logs
APK=$(ls TMessagesProj/build/outputs/apk/release/*x86_64*.apk | head -1)
echo "APK: $APK"
adb install -r "$APK" 2>&1 | tee emu_logs/install.txt
PKG=com.avsessisozx.xnagram
adb logcat -c
adb shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 2>&1 | tee emu_logs/launch.txt
sleep 25
echo "=== pid ===" ; adb shell pidof $PKG | tee emu_logs/pid.txt
adb logcat -d > emu_logs/full.txt
echo "=== CRASH BUFFER ===" ; adb logcat -d -b crash | tee emu_logs/crash.txt | head -80
echo "=== FATAL / signal ===" ; grep -nE "FATAL EXCEPTION|AndroidRuntime|Fatal signal|SIGSEGV|SIGABRT|UnsatisfiedLink|ClassNotFound|NoSuchMethod|NoClassDefFound|Process .* has died" emu_logs/full.txt | head -40
echo "=== focused window ===" ; adb shell dumpsys window | grep -E "mCurrentFocus" | head -2
adb exec-out screencap -p > emu_logs/screen.png
PID=$(adb shell pidof $PKG | tr -d '\r')
echo "=== APP PID LOG ($PID) ==="; adb logcat -d --pid=$PID | cut -c1-300 | grep -vE " (D|V) " | head -120
echo "=== TOP ACTIVITY ==="; adb shell dumpsys activity activities | grep -E "topResumedActivity|ResumedActivity" | head -3
echo "=== UI TEXT ==="; adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1; adb shell cat /sdcard/ui.xml | grep -oE 'text="[^"]+"' | head -20
echo "=== AUTOMATIC CHECKS ==="
FAIL=0
if [ -z "$(cat emu_logs/pid.txt | tr -d '\r\n ')" ]; then echo "CHECK FAIL: uygulama calismiyor (pid yok)"; FAIL=1; else echo "CHECK OK: uygulama calisiyor"; fi
if grep -A4 "FATAL EXCEPTION" emu_logs/full.txt | grep -q "Process: $PKG"; then echo "CHECK FAIL: uygulama cokmesi (FATAL EXCEPTION)"; FAIL=1; else echo "CHECK OK: cokme yok"; fi
adb shell uiautomator dump /sdcard/ui_check.xml >/dev/null 2>&1
adb shell cat /sdcard/ui_check.xml > emu_logs/ui_check.xml 2>/dev/null
if grep -q "LOC_ERR" emu_logs/ui_check.xml emu_logs/full.txt; then echo "CHECK FAIL: LOC_ERR bulundu"; grep -o "LOC_ERR[^\"]*" emu_logs/ui_check.xml emu_logs/full.txt | sort | uniq -c | head -20; FAIL=1; else echo "CHECK OK: LOC_ERR yok"; fi
echo "=== libs in apk ==="; unzip -l "$APK" | grep -E "\.so$" | awk '{print $4}' | head -30
echo "=== EMOJI PACK STORAGE ==="; unzip -v "$APK" | grep -E "emoji.pack"
echo "=== CRASH REPORTER TEST ==="
adb shell am crash $PKG
sleep 3
adb logcat -c >/dev/null 2>&1
adb shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
sleep 20
echo "pid: $(adb shell pidof $PKG)"
adb shell ls -la /sdcard/Download/ | head
adb shell cat /sdcard/Download/XNagram-hata-raporu.txt | head -40 | cut -c1-200
adb logcat -d | grep -i "CrashReporter" | head
exit $FAIL

