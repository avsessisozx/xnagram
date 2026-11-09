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
exit 0
