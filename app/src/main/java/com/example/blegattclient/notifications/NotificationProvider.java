package com.example.blegattclient.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

import com.example.blegattclient.R;

public class NotificationProvider {

    public static int NOTIFICATION_ID = 1234 ;
    public static String NOTIFICATION_CHANNEL_ID = "notification-channel-id" ;

    public static void sendLocalNotification(Context context, String title, String message) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context. NOTIFICATION_SERVICE ) ;
        Notification notification = getNotification(context, title, message) ;
        if (android.os.Build.VERSION. SDK_INT >= android.os.Build.VERSION_CODES. O ) {
            int importance = NotificationManager. IMPORTANCE_HIGH ;
            NotificationChannel notificationChannel = new NotificationChannel( NOTIFICATION_CHANNEL_ID , "NOTIFICATION_CHANNEL_NAME" , importance) ;
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel) ;
        }
        assert notificationManager != null;
        notificationManager.notify(NOTIFICATION_ID , notification) ;
    }

    private static Notification getNotification (Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder( context, NOTIFICATION_CHANNEL_ID ) ;
        builder.setContentTitle( title ) ;
        builder.setContentText(message) ;
        builder.setSmallIcon(R.drawable.ic_stat_warning) ;
        builder.setAutoCancel( true ) ;
        builder.setChannelId( NOTIFICATION_CHANNEL_ID ) ;
        return builder.build() ;
    }
}
