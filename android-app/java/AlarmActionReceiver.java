package io.github.seifeldinnasser24.hesabaty;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import org.json.JSONObject;

/** زراير الإشعار (تمام / غفوة): بتشتغل من غير ما التطبيق يتفتح، وبتسيب ملحوظة التطبيق يطبّقها أول ما يتفتح. */
public class AlarmActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        int id = intent.getIntExtra("id", 0);
        String noteId = intent.getStringExtra("noteId");
        if (noteId == null) noteId = "";
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.cancel(id);
        if (AlarmReceiver.ACTION_SNOOZE.equals(intent.getAction())) {
            try {
                JSONObject a = new JSONObject();
                a.put("id", id);
                a.put("at", System.currentTimeMillis() + 10 * 60 * 1000L);
                String t = intent.getStringExtra("title");
                String b = intent.getStringExtra("body");
                a.put("title", t == null ? "⏰ تذكير" : t);
                a.put("body", b == null ? "" : b);
                a.put("noteId", noteId);
                AlarmScheduler.remove(c, id);
                AlarmScheduler.add(c, a);
            } catch (Exception ignored) { }
            AlarmScheduler.addAck(c, noteId, "snooze");
        } else {
            AlarmScheduler.addAck(c, noteId, "done");
        }
    }
}
