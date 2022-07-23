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
import com.hvl.dragonteam.Activity.ActivitySplashScreen;
import com.hvl.dragonteam.Model.Enum.NotificationTypeEnum;
import com.hvl.dragonteam.Model.NotificationModel;
import com.hvl.dragonteam.Model.Training;
import com.hvl.dragonteam.R;
import com.hvl.dragonteam.Utilities.Constants;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "Dragon Team FCM Service";
    String channelId = "notification_channel";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        PendingIntent pendingIntent = null;
        NotificationModel notificationModel;
        if (remoteMessage.getData().get("notificationModel") != null) {
            notificationModel = new Gson().fromJson(remoteMessage.getData().get("notificationModel"), NotificationModel.class);

            Bundle bundle = new Bundle();
            bundle.putString("notificationModel", remoteMessage.getData().get("notificationModel"));
            bundle.putString("DIRECT", "NOTIFICATION");

            Intent intent;

            if(notificationModel.getTraining().getTeamId().equals(Constants.personTeamView.getTeamId())){
                intent = new Intent(this, ActivityHome.class);
            } else {
                intent = new Intent(this, ActivitySplashScreen.class);
            }
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtras(bundle);
            pendingIntent = PendingIntent.getActivity(this, notificationModel.getNotificationType(), intent,  PendingIntent.FLAG_ONE_SHOT);
        }

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.logo_genel)
                        .setContentTitle(remoteMessage.getNotification().getTitle())
                        .setContentText(remoteMessage.getNotification().getBody())
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
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