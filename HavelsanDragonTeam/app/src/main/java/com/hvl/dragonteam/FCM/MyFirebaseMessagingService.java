package com.hvl.dragonteam.FCM;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.hvl.dragonteam.Activity.ActivityHome;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "SHUTTLE FCM Service";
    String channelId = "notification_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        PendingIntent pendingIntent = null;
        NotificationModel notificationModel;
        if (remoteMessage.getData().get("notificationModel") != null) {
            notificationModel = new Gson().fromJson(remoteMessage.getData().get("notificationModel").toString(), NotificationModel.class);

          /*  String json = new Gson().toJson(notificationModel.getTransportation(), Transportation.class);
            Bundle bundle = new Bundle();
            bundle.putString("OBJ", json);*/

            Intent intent = new Intent(this, ActivityHome.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

            if(notificationModel.getNotificationType() == NotificationTypeEnum.CHAT_MESSAGE_NOTIFICATION.getValue()){
               // Constants.bottomBar.setSelectedItemId(R.id.action_chat);
//               < bundle.putString("DIRECT", "CHAT");
//                intent.putExtras(bundle);>
                pendingIntent = PendingIntent.getActivity(this, 0, intent,  PendingIntent.FLAG_ONE_SHOT);
            }
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId,
                        "Information",
                        NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("Information");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        notificationManager.notify(TAG, (int) System.currentTimeMillis(), mBuilder.build());
    }
}