package com.example.awx.Service;

import android.accessibilityservice.AccessibilityService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.PixelFormat;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.example.awx.MainActivity;
import com.example.awx.R;
import com.example.utils.Pres;

public class AccessService extends AccessibilityService {

    private NotificationManager notificationManager;
    private Handler handler;
    private WindowManager windowManager;
    private LayoutInflater layoutInflater;
    private MyThread myThread;
    private WindowManager.LayoutParams wLayoutParams;
    private View floatView;


    @Override
    protected void onServiceConnected() {
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        registerReceiver(new MyBroadcastReceiver(), new IntentFilter(getPackageName() + "_sendNotify"));

        boolean running = Pres.getBoolean(getApplicationContext(), "running", false);
        int actionType = Pres.getInt(getApplicationContext(), "actionType", -1);
        final Notification notification = creatNotify(actionType, running);
        startForeground(Utils.NOTIFY_ID, notification);

        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0://更新通知栏目
                        notificationManager.notify(Utils.NOTIFY_ID, creatNotify(msg.getData().getInt("actionType"), msg.getData().getBoolean("running")));
                        break;
                    case 1:
                        buildFloatView(msg.getData().getString("msg"));//常规消息显示
                        break;
                    case 2:
                        try {
                            windowManager.removeView(floatView);
                        } catch (Exception e) {
                        }
                        break;
                    default:
                        break;
                }
            }
        };

        myThread = new MyThread(this, handler);
        myThread.start();

        buildFloatView("闪闪助手准备就绪!点我消失");

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

    private void buildFloatView(String str) {
        if (floatView == null) {
            floatView = layoutInflater.inflate(R.layout.my_toast, null, false);
        }
        TextView floatOne_text = (TextView) floatView.findViewById(R.id.t_text);
        floatOne_text.setText(str);
        floatOne_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handler.sendEmptyMessage(2);
            }
        });
        if (wLayoutParams == null) {
            wLayoutParams = new WindowManager.LayoutParams();
            wLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
            wLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            wLayoutParams.format = PixelFormat.RGBA_8888;
            wLayoutParams.gravity = Gravity.CENTER;
            wLayoutParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
            wLayoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        }
        try {
            windowManager.updateViewLayout(floatView, wLayoutParams);
        } catch (Exception e) {
            windowManager.addView(floatView, wLayoutParams);
        }

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
            remoteViews.setImageViewResource(R.id.n_img_sendFriend, R.drawable.path_friend_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_sendFriend, R.drawable.path_friend);
        }
        if (actionType == ActionType.SendGroup && running) {
            remoteViews.setImageViewResource(R.id.n_img_sendGroup, R.drawable.path_group_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_sendGroup, R.drawable.path_group);
        }
        if (actionType == ActionType.SendNear && running) {
            remoteViews.setImageViewResource(R.id.n_img_sendNear, R.drawable.path_near_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_sendNear, R.drawable.path_near);
        }
        if (actionType == ActionType.AddFriend && running) {
            remoteViews.setImageViewResource(R.id.n_img_addFriend, R.drawable.path_friend_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_addFriend, R.drawable.path_friend);
        }

        if (actionType == ActionType.AddGroup && running) {
            remoteViews.setImageViewResource(R.id.n_img_addgroup, R.drawable.path_group_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_addgroup, R.drawable.path_group);
        }
        if (actionType == ActionType.AutoApply && running) {
            remoteViews.setImageViewResource(R.id.n_img_autoapply, R.drawable.path_chat_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_autoapply, R.drawable.path_chat);
        }
        if (actionType == ActionType.Sche && running) {
            remoteViews.setImageViewResource(R.id.n_img_line, R.drawable.path_sche_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_line, R.drawable.path_sche);
        }
        if (actionType == ActionType.Set && running) {
            remoteViews.setImageViewResource(R.id.n_img_set, R.drawable.path_set_s);
        } else {
            remoteViews.setImageViewResource(R.id.n_img_set, R.drawable.path_set);
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
