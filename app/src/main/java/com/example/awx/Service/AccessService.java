package com.example.awx.Service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.RemoteViews;

import com.example.awx.MainActivity;
import com.example.awx.R;
import com.example.utils.Pres;

public class AccessService extends AccessibilityService {

    private NotificationManager notificationManager;
    private Handler handler;
    private WindowManager windowManager;
    private WindowManager.LayoutParams wLayoutParams;
    private LayoutInflater layoutInflater;
    private MyThread myThread;


    @Override
    protected void onServiceConnected() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        registerReceiver(new MyBroadcastReceiver(), new IntentFilter(getPackageName() + "_sendNotify"));

        boolean running = Pres.getBoolean(getApplicationContext(), "running", false);
        int actionType = Pres.getInt(getApplicationContext(), "actionType", -1);
        Notification notification = creatNotify(actionType, running);
        startForeground(Utils.NOTIFY_ID, notification);

        myThread = new MyThread(this, handler);
        myThread.start();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        //在此实现多路分发
        if (event.getSource() != null) {

            Log.i("SE", "text:" + event.getText().toString());
            Log.i("SE", "desc:" + event.getContentDescription() + "");
            Log.i("SE", "ids:" + event.getSource().getViewIdResourceName() + "");


        }
    }

    @Override
    public void onInterrupt() {

    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            int oldActionType = Pres.getInt(getApplicationContext(), "actionType", -1);
            int newActionType = intent.getIntExtra("actionType", -1);
            boolean running = (oldActionType == newActionType ? !Pres.getBoolean(getApplicationContext(), "running", false) : true);

            Pres.putBoolean(getApplicationContext(), "running", running);
            Pres.putInt(getApplicationContext(), "actionType", newActionType);
            notificationManager.notify(Utils.NOTIFY_ID, creatNotify(newActionType, running));

        }
    }

    public Notification creatNotify(int actionType, boolean running) {
        Notification notification = new Notification();
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.my_notify);
        if (actionType == ActionType.SendFriend && running) {
            remoteViews.setImageViewResource(R.id.n_img_sendFriend, R.drawable.s_user_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_sendFriend, R.drawable.s_user_n);
        }
        if (actionType == ActionType.SendGroup && running) {
            remoteViews.setImageViewResource(R.id.n_img_sendGroup, R.drawable.s_group_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_sendGroup, R.drawable.s_group_n);
        }
        if (actionType == ActionType.SendNear && running) {
            remoteViews.setImageViewResource(R.id.n_img_sendNear, R.drawable.s_near_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_sendNear, R.drawable.s_near_n);
        }
        if (actionType == ActionType.AddFriend && running) {
            remoteViews.setImageViewResource(R.id.n_img_addFriend, R.drawable.group_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_addFriend, R.drawable.group_n);
        }

        if (actionType == ActionType.AddGroup && running) {
            remoteViews.setImageViewResource(R.id.n_img_addgroup, R.drawable.group_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_addgroup, R.drawable.group_n);
        }
        if (actionType == ActionType.AutoApply && running) {
            remoteViews.setImageViewResource(R.id.n_img_autoapply, R.drawable.auto_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_autoapply, R.drawable.auto_n);
        }


        Intent sendFriendIntent = new Intent(getPackageName() + "_sendNotify");
        sendFriendIntent.putExtra("actionType", ActionType.SendFriend);
        PendingIntent p_sendFriendIntent = PendingIntent.getBroadcast(this, 1, sendFriendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.n_sendFriend, p_sendFriendIntent);

        Intent sendGroupIntent = new Intent(getPackageName() + "_sendNotify");
        sendGroupIntent.putExtra("actionType", ActionType.SendGroup);
        PendingIntent p_sendGroupIntent = PendingIntent.getBroadcast(this, 2, sendGroupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.n_sendGroup, p_sendGroupIntent);

        Intent sendNearIntent = new Intent(getPackageName() + "_sendNotify");
        sendNearIntent.putExtra("actionType", ActionType.SendNear);
        PendingIntent p_sendNearIntent = PendingIntent.getBroadcast(this, 3, sendNearIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.n_sendNear, p_sendNearIntent);

        Intent addFriendIntent = new Intent(getPackageName() + "_sendNotify");
        addFriendIntent.putExtra("actionType", ActionType.AddFriend);
        PendingIntent p_addFriendIntent = PendingIntent.getBroadcast(this, 4, addFriendIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.n_addFriend, p_addFriendIntent);

        Intent autoapply = new Intent(getPackageName() + "_sendNotify");
        autoapply.putExtra("actionType", ActionType.AutoApply);
        PendingIntent p_autoapply = PendingIntent.getBroadcast(this, 5, autoapply, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.n_autoapply, p_autoapply);

        Intent addGroupIntent = new Intent(getPackageName() + "_sendNotify");
        addGroupIntent.putExtra("actionType", ActionType.AddGroup);
        PendingIntent p_addGroupIntent = PendingIntent.getBroadcast(this, 6, addGroupIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setOnClickPendingIntent(R.id.n_addGroup, p_addGroupIntent);


        notification.tickerText = "";
        notification.icon = R.mipmap.ic_launcher;
        notification.when = System.currentTimeMillis();
        notification.contentView = remoteViews;
        notification.flags = notification.flags | Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR;

        Intent intt = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1, intt, PendingIntent.FLAG_ONE_SHOT);
        notification.contentIntent = pendingIntent;

        return notification;
    }

}
