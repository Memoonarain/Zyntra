package com.example.zyntra;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Map;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.e("FCM", "Token refreshed: " + token);

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(FirebaseAuth.getInstance().getUid())
                .update("fcmToken", token);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            String title = data.get("title");
            String message = data.get("message");
            String postId = data.get("postId"); // Will be null if not sent

            sendNotification(title, message, postId);
        }
    }

    private void sendNotification(String title, String message, String postId) {
        String channelId = "zyntra_channel";

        // Create notification channel for Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Zyntra Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Zyntra App Notification Channel");

            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        // Intent to open PostActivity if postId exists, otherwise open MainActivity
        Intent intent;
        if (postId != null && !postId.isEmpty()) {
            intent = new Intent(this, PostActivity.class);
            intent.putExtra("postId", postId);
        } else {
            intent = new Intent(this, MainActivity.class); // Fallback
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.icon_notification)
                .setContentTitle(title != null ? title : "Zyntra")
                .setContentText(message != null ? message : "")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify((int) System.currentTimeMillis(), builder.build());
    }
}
