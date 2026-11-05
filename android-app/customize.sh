#!/usr/bin/env bash
# بيظبط مشروع الأندرويد اللي Capacitor عمله: رقم النسخة، التوقيع الثابت، الصلاحيات، الأيقونات، الصوت، شاشة البداية.
set -euo pipefail
N="${1:-1}"
A=android/app
sed -i "s/versionCode 1$/versionCode $N/; s/versionName \"1.0\"/versionName \"1.$N\"/" "$A/build.gradle"
grep -q "versionCode $N" "$A/build.gradle"
cat >> "$A/build.gradle" <<'G'

// حساباتي: نفس مفتاح التوقيع في كل نسخة عشان التحديث يتثبت فوق القديم والبيانات تفضل
android {
    signingConfigs {
        hesabaty {
            storeFile file('../../keystore/hesabaty.jks')
            storePassword 'hesabaty-store-2026'
            keyAlias 'hesabaty'
            keyPassword 'hesabaty-store-2026'
        }
    }
    buildTypes { release { signingConfig signingConfigs.hesabaty } }
    lint { checkReleaseBuilds false; abortOnError false }
}
G
python3 - <<'P'
p='android/app/src/main/AndroidManifest.xml';s=open(p,encoding='utf-8').read()
add='''    <uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
    <uses-permission android:name="android.permission.USE_EXACT_ALARM" />
    <uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM" android:maxSdkVersion="32" />
    <uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
    <uses-permission android:name="android.permission.VIBRATE" />
    <uses-permission android:name="android.permission.WAKE_LOCK" />
    <uses-permission android:name="android.permission.USE_FULL_SCREEN_INTENT" />
    <uses-permission android:name="android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS" />
'''
rcv='''        <receiver android:name=".AlarmReceiver" android:exported="false" />
        <receiver android:name=".AlarmActionReceiver" android:exported="false" />
        <receiver android:name=".BootReceiver" android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.BOOT_COMPLETED" />
                <action android:name="android.intent.action.MY_PACKAGE_REPLACED" />
                <action android:name="android.intent.action.QUICKBOOT_POWERON" />
                <action android:name="com.htc.intent.action.QUICKBOOT_POWERON" />
            </intent-filter>
        </receiver>
'''
assert '</application>' in s and 'android:name=".MainActivity"' in s
s=s.replace('</application>',rcv+'    </application>',1)
s=s.replace('android:name=".MainActivity"','android:name=".MainActivity"\n            android:showWhenLocked="true"\n            android:turnScreenOn="true"',1)
assert '</manifest>' in s
s=s.replace('</manifest>',add+'</manifest>');open(p,'w',encoding='utf-8').write(s)
P
R="$A/src/main/res"
rm -rf "$R/mipmap-anydpi-v26" "$R/drawable-v24/ic_launcher_foreground.xml"
find "$R" -name 'splash.png' -delete
find "$R" -name 'ic_launcher_foreground.png' -delete
cp -r res/* "$R/"
J="$A/src/main/java/io/github/seifeldinnasser24/hesabaty"
test -f "$J/MainActivity.java"
cp java/*.java "$J/"
echo "customized: versionCode $N"
