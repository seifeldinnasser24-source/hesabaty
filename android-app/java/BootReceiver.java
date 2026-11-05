package io.github.seifeldinnasser24.hesabaty;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/** بعد ما الموبايل يفتح أو التطبيق يتحدّث: رجّع كل المنبّهات الجاية. */
public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        AlarmScheduler.rescheduleAll(c);
    }
}
