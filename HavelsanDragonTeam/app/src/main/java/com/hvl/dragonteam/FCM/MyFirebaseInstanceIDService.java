package com.hvl.dragonteam.FCM;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.hvl.dragonteam.DataService.NotificationService;
import com.hvl.dragonteam.Model.Enum.LanguageEnum;
import com.hvl.dragonteam.Model.PersonNotification;
import com.hvl.dragonteam.Utilities.Util;

import org.json.JSONException;

public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService {

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    // [START refresh_token]
    @Override
    public void onTokenRefresh() {
        // Get updated InstanceID token.

        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        if(FirebaseAuth.getInstance().getCurrentUser() != null)
            sendRegistrationToServer(refreshedToken);
    }
    // [END refresh_token]

    /**
     * Persist token to third-party servers.
     * <p>
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {//TODO kaydedilmiş ayarları kaybeder
        NotificationService notificationService = new NotificationService();
        PersonNotification personNotification = new PersonNotification(FirebaseAuth.getInstance().getCurrentUser().getUid(),token, true, LanguageEnum.getLocaleEnumValue());
        try {
            notificationService.saveNotification(getApplicationContext(), personNotification, null);
        } catch (JSONException e) {
            e.printStackTrace();
            Util.toastError(getApplicationContext());
        }
    }
}