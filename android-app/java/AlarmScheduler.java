package io.github.seifeldinnasser24.hesabaty;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import org.json.JSONArray;
import org.json.JSONObject;

/** منبّهات حساباتي: بتتسجل بنفس طريقة منبّه الساعة (setAlarmClock) عشان النظام يعاملها كمنبّه حقيقي. */
public class AlarmScheduler {
    static final String PREF = "hesabaty_alarms";
    static final String ACTION_FIRE = "io.github.seifeldinnasser24.hesabaty.ALARM";

    static SharedPreferences sp(Context c) { return c.getSharedPreferences(PREF, Context.MODE_PRIVATE); }

    static JSONArray load(Context c) {
        try { return new JSONArray(sp(c).getString("list", "[]")); } catch (Exception e) { return new JSONArray(); }
    }

    static void save(Context c, JSONArray a) { sp(c).edit().putString("list", a.toString()).apply(); }

    static PendingIntent firePI(Context c, int id, JSONObject a, boolean create) {
        Intent i = new Intent(c, AlarmReceiver.class);
        i.setAction(ACTION_FIRE);
        if (a != null) {
            i.putExtra("id", id);
            i.putExtra("title", a.optString("title", "⏰ تذكير"));
            i.putExtra("body", a.optString("body", ""));
            i.putExtra("noteId", a.optString("noteId", ""));
        }
        int flags = PendingIntent.FLAG_IMMUTABLE | (create ? PendingIntent.FLAG_UPDATE_CURRENT : PendingIntent.FLAG_NO_CREATE);
        return PendingIntent.getBroadcast(c, id, i, flags);
    }

    static void scheduleOne(Context c, JSONObject a) {
        long at = a.optLong("at", 0);
        int id = a.optInt("id", 0);
        if (id == 0 || at <= System.currentTimeMillis()) return;
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        if (am == null) return;
        PendingIntent pi = firePI(c, id, a, true);
        Intent open = c.getPackageManager().getLaunchIntentForPackage(c.getPackageName());
        if (open == null) open = new Intent(c, MainActivity.class);
        PendingIntent show = PendingIntent.getActivity(c, id, open, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        try {
            if (Build.VERSION.SDK_INT >= 31 && !am.canScheduleExactAlarms()) {
                am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
            } else {
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(at, show), pi);
            }
        } catch (SecurityException e) {
            am.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at, pi);
        }
    }

    static void cancelOne(Context c, int id) {
        AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = firePI(c, id, null, false);
        if (pi != null) {
            if (am != null) am.cancel(pi);
            pi.cancel();
        }
    }

    static int replaceAll(Context c, JSONArray arr) throws Exception {
        JSONArray old = load(c);
        for (int i = 0; i < old.length(); i++) cancelOne(c, old.getJSONObject(i).optInt("id", 0));
        JSONArray keep = new JSONArray();
        long now = System.currentTimeMillis();
        for (int i = 0; i < arr.length(); i++) {
            JSONObject a = arr.getJSONObject(i);
            if (a.optLong("at", 0) > now) {
                scheduleOne(c, a);
                keep.put(a);
            }
        }
        save(c, keep);
        return keep.length();
    }

    static void add(Context c, JSONObject a) {
        JSONArray list = load(c);
        list.put(a);
        save(c, list);
        scheduleOne(c, a);
    }

    static void rescheduleAll(Context c) {
        JSONArray list = load(c);
        JSONArray keep = new JSONArray();
        long now = System.currentTimeMillis();
        for (int i = 0; i < list.length(); i++) {
            JSONObject a = list.optJSONObject(i);
            if (a != null && a.optLong("at", 0) > now) {
                scheduleOne(c, a);
                keep.put(a);
            }
        }
        save(c, keep);
    }

    static void remove(Context c, int id) {
        JSONArray list = load(c);
        JSONArray keep = new JSONArray();
        for (int i = 0; i < list.length(); i++) {
            JSONObject a = list.optJSONObject(i);
            if (a != null && a.optInt("id", 0) != id) keep.put(a);
        }
        save(c, keep);
    }

    static void addAck(Context c, String noteId, String action) {
        try {
            JSONArray a = new JSONArray(sp(c).getString("acks", "[]"));
            JSONObject o = new JSONObject();
            o.put("noteId", noteId);
            o.put("action", action);
            o.put("t", System.currentTimeMillis());
            a.put(o);
            sp(c).edit().putString("acks", a.toString()).apply();
        } catch (Exception ignored) { }
    }

    static JSONArray takeAcks(Context c) {
        JSONArray a;
        try { a = new JSONArray(sp(c).getString("acks", "[]")); } catch (Exception e) { a = new JSONArray(); }
        sp(c).edit().putString("acks", "[]").apply();
        return a;
    }
}
