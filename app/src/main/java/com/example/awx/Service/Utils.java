package com.example.awx.Service;

import android.content.ComponentName;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.awx.R;
import com.example.utils.Pres;

import java.io.UnsupportedEncodingException;
import java.util.UUID;

/**
 * Created by john on 2016/7/23.
 */
public class Utils {
    public static int NOTIFY_ID = 100;
    private static final String ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners";

    public static String getMsgs(SQLiteDatabase sqLiteDatabase, int types) {
        Cursor cursor = sqLiteDatabase.query("sendstrs", null, "types=" + types, null, null, null, "RANDOM() limit 1");
        if (cursor.moveToFirst()) {
            return new String(cursor.getBlob(cursor.getColumnIndex("texts")));
        } else {
            cursor = sqLiteDatabase.query("sendstrs", null, "types=0", null, null, null, "RANDOM() limit 1");
            if (cursor.moveToFirst()) {
                return new String(cursor.getBlob(cursor.getColumnIndex("texts")));
            }
        }
        return null;
    }

    public static void sleep(long paramLong) {
        try {
            Thread.sleep(paramLong);
            return;
        } catch (InterruptedException localInterruptedException) {
            localInterruptedException.printStackTrace();
        }
    }

    public static String getids(Context context) {
        UUID uuid;
        final String id = Pres.getString(context, "my_ids", null);
        if (id != null) {
            uuid = UUID.fromString(id);
        } else {
            final String androidId = Settings.Secure.getString(
                    context.getContentResolver(), Settings.Secure.ANDROID_ID);
            try {
                if (androidId == null ? false : !androidId.trim().equals("")) {
                    uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                } else {
                    final String deviceId = ((TelephonyManager) context
                            .getSystemService(Context.TELEPHONY_SERVICE))
                            .getDeviceId();
                    uuid = deviceId != null ? UUID.nameUUIDFromBytes(deviceId
                            .getBytes("utf8")) : UUID.randomUUID();
                }
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
            Pres.putString(context, "my_ids", uuid.toString());
        }
        return uuid.toString();
    }

    public static void makeText(Context context, String tvString) {
        View layout = LayoutInflater.from(context).inflate(R.layout.my_toast, null);
        TextView text = (TextView) layout.findViewById(R.id.t_text);
        text.setText(tvString);
        Toast toast = new Toast(context);
        toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(layout);
        toast.show();
    }



    public static boolean isListenNotify(Context context) {
        String pkgName = context.getPackageName();
        final String flat = Settings.Secure.getString(context.getContentResolver(),
                ENABLED_NOTIFICATION_LISTENERS);
        if (!TextUtils.isEmpty(flat)) {
            final String[] names = flat.split(":");
            for (int i = 0; i < names.length; i++) {
                final ComponentName cn = ComponentName.unflattenFromString(names[i]);
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.getPackageName())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static void mi(String tags, String str) {
        Log.i("awx", tags + " __ " + str);
    }
}
