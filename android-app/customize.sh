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
'''
assert '</manifest>' in s
s=s.replace('</manifest>',add+'</manifest>');open(p,'w',encoding='utf-8').write(s)
P
R="$A/src/main/res"
rm -rf "$R/mipmap-anydpi-v26" "$R/drawable-v24/ic_launcher_foreground.xml"
find "$R" -name 'splash.png' -delete
find "$R" -name 'ic_launcher_foreground.png' -delete
cp -r res/* "$R/"
echo "customized: versionCode $N"
