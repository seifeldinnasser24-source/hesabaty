package io.github.seifeldinnasser24.hesabaty;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import androidx.core.app.NotificationCompat;

/** وقت المنبّه: إشعار بأعلى أولوية، بصوت منبّه بيفضل يرن (INSISTENT) لحد ما تدوس عليه، ويظهر فوق شاشة القفل. */
public class AlarmReceiver extends BroadcastReceiver {
    static final String CH = "hesabaty_alarm_v1";
    static final String ACTION_DONE = "io.github.seifeldinnasser24.hesabaty.DONE";
    static final String ACTION_SNOOZE = "io.github.seifeldinnasser24.hesabaty.SNOOZE";

    static Uri soundUri(Context c) {
        int raw = c.getResources().getIdentifier("alarm", "raw", c.getPackageName());
        if (raw != 0) return Uri.parse("android.resource://" + c.getPackageName() + "/" + raw);
        return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    }

    static void ensureChannel(Context c, NotificationManager nm) {
        if (Build.VERSION.SDK_INT < 26 || nm.getNotificationChannel(CH) != null) return;
        NotificationChannel ch = new NotificationChannel(CH, "منبّه التذكيرات", NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription("تذكيرات النوت — بترن زي منبّه الساعة");
        AudioAttributes aa = new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        ch.setSound(soundUri(c), aa);
        ch.enableVibration(true);
        ch.setVibrationPattern(new long[]{0, 600, 300, 600, 300, 900});
        ch.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        nm.createNotificationChannel(ch);
    }

    @Override
    public void onReceive(Context c, Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String title = intent.getStringExtra("title");
        String body = intent.getStringExtra("body");
        String noteId = intent.getStringExtra("noteId");
        if (title == null) title = "⏰ تذكير";
        if (body == null) body = "";
        if (noteId == null) noteId = "";
        AlarmScheduler.remove(c, id);

        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) return;
        if (Build.VERSION.SDK_INT >= 33
                && c.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return;
        ensureChannel(c, nm);

        Intent open = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (open == null) open = new Intent(c, MainActivity.class);
        open.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        open.putExtra("hesabatyNote", noteId);
        PendingIntent contentPI = PendingIntent.getActivity(c, id, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Intent done = new Intent(c, AlarmActionReceiver.class).setAction(ACTION_DONE)
                .putExtra("id", id).putExtra("noteId", noteId);
        Intent snooze = new Intent(c, AlarmActionReceiver.class).setAction(ACTION_SNOOZE)
                .putExtra("id", id).putExtra("noteId", noteId).putExtra("title", title).putExtra("body", body);
        PendingIntent donePI = PendingIntent.getBroadcast(c, id, done, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent snoozePI = PendingIntent.getBroadcast(c, id, snooze, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        int icon = c.getResources().getIdentifier("ic_stat_notify", "drawable", c.getPackageName());
        if (icon == 0) icon = android.R.drawable.ic_lock_idle_alarm;

        NotificationCompat.Builder b = new NotificationCompat.Builder(c, CH)
                .setSmallIcon(icon)
                .setColor(0xFF1F6FEB)
                .setContentTitle(title)
                .setContentText(body.isEmpty() ? "دوس هنا تفتح حساباتي" : body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body.isEmpty() ? "دوس هنا تفتح حساباتي" : body))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setSound(soundUri(c), AudioManager.STREAM_ALARM)
                .setVibrate(new long[]{0, 600, 300, 600, 300, 900})
                .setAutoCancel(true)
                .setContentIntent(contentPI)
                .setFullScreenIntent(contentPI, true)
                .setTimeoutAfter(5 * 60 * 1000)
                .addAction(0, "✓ تمام", donePI)
                .addAction(0, "😴 بعد 10 دقايق", snoozePI);
        Notification n = b.build();
        n.flags |= Notification.FLAG_INSISTENT;
        nm.notify(id, n);
    }
}
