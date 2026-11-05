package io.github.seifeldinnasser24.hesabaty;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import com.getcapacitor.JSArray;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import org.json.JSONArray;
import org.json.JSONObject;

@CapacitorPlugin(name = "HesabatySys")
public class HesabatySysPlugin extends Plugin {

    @PluginMethod
    public void status(PluginCall call) {
        Context c = getContext();
        JSObject r = new JSObject();
        boolean battery = true;
        if (Build.VERSION.SDK_INT >= 23) {
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            battery = pm != null && pm.isIgnoringBatteryOptimizations(c.getPackageName());
        }
        boolean exact = true;
        if (Build.VERSION.SDK_INT >= 31) {
            AlarmManager am = (AlarmManager) c.getSystemService(Context.ALARM_SERVICE);
            exact = am != null && am.canScheduleExactAlarms();
        }
        NotificationManager nm = (NotificationManager) c.getSystemService(Context.NOTIFICATION_SERVICE);
        boolean fullScreen = true;
        if (Build.VERSION.SDK_INT >= 34 && nm != null) fullScreen = nm.canUseFullScreenIntent();
        boolean notif = nm == null || Build.VERSION.SDK_INT < 24 || nm.areNotificationsEnabled();
        r.put("battery", battery);
        r.put("exact", exact);
        r.put("fullScreen", fullScreen);
        r.put("notifications", notif);
        r.put("manufacturer", Build.MANUFACTURER);
        r.put("sdk", Build.VERSION.SDK_INT);
        r.put("alarms", AlarmScheduler.load(c).length());
        call.resolve(r);
    }

    @PluginMethod
    public void setAlarms(PluginCall call) {
        JSArray arr = call.getArray("alarms");
        try {
            int n = AlarmScheduler.replaceAll(getContext(), arr == null ? new JSONArray() : arr);
            JSObject r = new JSObject();
            r.put("scheduled", n);
            call.resolve(r);
        } catch (Exception e) {
            call.reject("setAlarms failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void testAlarm(PluginCall call) {
        try {
            int sec = call.getInt("seconds", 10);
            JSONObject a = new JSONObject();
            a.put("id", 1999999999);
            a.put("at", System.currentTimeMillis() + sec * 1000L);
            a.put("title", call.getString("title", "⏰ تجربة المنبّه"));
            a.put("body", call.getString("body", ""));
            a.put("noteId", "");
            AlarmScheduler.scheduleOne(getContext(), a);
            call.resolve();
        } catch (Exception e) {
            call.reject("testAlarm failed: " + e.getMessage());
        }
    }

    @PluginMethod
    public void takeAcks(PluginCall call) {
        JSObject r = new JSObject();
        r.put("acks", AlarmScheduler.takeAcks(getContext()));
        call.resolve(r);
    }

    private boolean tryStart(Intent i) {
        try {
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(i);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void appDetails() {
        tryStart(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getContext().getPackageName())));
    }

    @PluginMethod
    public void requestBattery(PluginCall call) {
        boolean ok = false;
        if (Build.VERSION.SDK_INT >= 23) {
            ok = tryStart(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:" + getContext().getPackageName())));
            if (!ok) ok = tryStart(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        }
        if (!ok) appDetails();
        call.resolve();
    }

    @PluginMethod
    public void openAutoStart(PluginCall call) {
        String[][] targets = {
                {"com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"},
                {"com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"},
                {"com.oplus.safecenter", "com.oplus.safecenter.startupapp.StartupAppListActivity"},
                {"com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"},
                {"com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"},
                {"com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
                {"com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"},
                {"com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"},
                {"com.asus.mobilemanager", "com.asus.mobilemanager.MainActivity"}
        };
        boolean ok = false;
        for (String[] t : targets) {
            Intent i = new Intent();
            i.setComponent(new ComponentName(t[0], t[1]));
            if (tryStart(i)) { ok = true; break; }
        }
        if (!ok) appDetails();
        JSObject r = new JSObject();
        r.put("opened", ok);
        call.resolve(r);
    }

    @PluginMethod
    public void openAppSettings(PluginCall call) {
        appDetails();
        call.resolve();
    }

    @PluginMethod
    public void openFullScreen(PluginCall call) {
        boolean ok = false;
        if (Build.VERSION.SDK_INT >= 34) {
            ok = tryStart(new Intent(Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT, Uri.parse("package:" + getContext().getPackageName())));
        }
        if (!ok) appDetails();
        call.resolve();
    }
}
