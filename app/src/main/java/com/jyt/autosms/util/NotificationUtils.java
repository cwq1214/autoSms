package com.jyt.autosms.util;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.support.v7.app.NotificationCompat;

/**
 * Created by v7 on 2016/9/28.
 */

public class NotificationUtils {

    public void showTextNotification(Context context,String title,String content){
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context);

        builder.setContentTitle(title);

        builder.setContentText(content);

        builder.setAutoCancel(false);

        builder.setDefaults(Notification.DEFAULT_LIGHTS);
    }
}
