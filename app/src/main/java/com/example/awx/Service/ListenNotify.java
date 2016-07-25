package com.example.awx.Service;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.example.utils.Pres;

public class ListenNotify extends NotificationListenerService {
    @Override
    public void onCreate() {
        super.onCreate();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Utils.mi("notify", "onPosted...");
        Utils.mi("notify", "onPosted..." + sbn.getNotification().extras.getString(Notification.EXTRA_TITLE));
        Utils.mi("notify", "onPosted..." + sbn.getNotification().extras.getString(Notification.EXTRA_SUB_TEXT));

        boolean running = Pres.getBoolean(getApplicationContext(), "running", false);
        int actionType = Pres.getInt(getApplicationContext(), "actionType", -1);
        if (running && (actionType == ActionType.AutoApply) && sbn.getPackageName().equals(QInfos.name)) {
            try {
                sbn.getNotification().contentIntent.send();
            } catch (PendingIntent.CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
    }

}
