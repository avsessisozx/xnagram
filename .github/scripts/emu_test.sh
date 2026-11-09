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
echo "=== libs in apk ==="; unzip -l "$APK" | grep -E "\.so$" | awk '{print $4}' | head -30
echo "=== ARM64 APK UNDER TRANSLATION ==="
adb uninstall $PKG >/dev/null 2>&1
adb shell getprop ro.product.cpu.abilist
mkdir -p arm && (gh release download test-21 -R avsessisozx/xnagram -p '*arm64*.apk' -D arm 2>&1 | tail -2)
A=$(ls arm/*.apk 2>/dev/null | head -1); echo "ARM APK: $A"
if [ -n "$A" ]; then
  adb install -r "$A" 2>&1 | tail -3
  adb logcat -c
  adb shell monkey -p $PKG -c android.intent.category.LAUNCHER 1 >/dev/null 2>&1
  sleep 30
  echo "pid: $(adb shell pidof $PKG)"
  adb logcat -d > emu_logs/arm_full.txt
  grep -nE "FATAL EXCEPTION|Fatal signal|SIGSEGV|SIGABRT|UnsatisfiedLink|NoSuchMethod|NoClassDefFound|Process $PKG.*died|has died" emu_logs/arm_full.txt | cut -c1-300 | head -30
  adb logcat -d -b crash | cut -c1-300 | head -60
fi
exit 0
