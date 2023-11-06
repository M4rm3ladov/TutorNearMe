package com.ren.tutornearme.service;

import static com.ren.tutornearme.util.Common.NOTIFICATION_BODY;
import static com.ren.tutornearme.util.Common.NOTIFICATION_TITLE;

import androidx.annotation.NonNull;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.ren.tutornearme.util.FirebaseMessagingHelper;

import java.util.Map;
import java.util.Random;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        new FirebaseMessagingHelper().updateNotificationToken(this, token, null);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);
        Map<String, String> data = message.getData();
        new FirebaseMessagingHelper().showNotification(
                this,
                new Random().nextInt(),
                data.get(NOTIFICATION_TITLE),
                data.get(NOTIFICATION_BODY),
                null);
    }
}
