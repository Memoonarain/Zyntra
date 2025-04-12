package com.example.zyntra;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class FcmPushNotification {

    private static final String ACCESS_TOKEN_URL = "https://4b961520-14fd-4a35-91c4-8520bdb0827a-00-12w4rxt3zkyn6.pike.replit.dev/token";
    private static final String FCM_URL = "https://fcm.googleapis.com/v1/projects/zyntra-24/messages:send";

    /**
     * Call this method to save the notification to Firestore and then send push.
     */
    public static void saveAndPushNotification(Context context, NotificationModel notificationModel) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        String notificationId = db.collection("notifications")
                .document(notificationModel.getReceiverId())
                .collection("user_notifications")
                .document()
                .getId();

        Map<String, Object> notificationMap = new HashMap<>();
        notificationMap.put("title", notificationModel.getTitle());
        notificationMap.put("message", notificationModel.getBody());
        notificationMap.put("senderId", notificationModel.getSenderId());
        notificationMap.put("receiverId", notificationModel.getReceiverId());
        notificationMap.put("postId", notificationModel.getPostId());
        notificationMap.put("type", notificationModel.getType()); // like, comment, follow, etc.
        notificationMap.put("timestamp", System.currentTimeMillis());
        notificationMap.put("seen", false);

        db.collection("notifications")
                .document(notificationModel.getReceiverId())
                .collection("user_notifications")
                .document(notificationId)
                .set(notificationMap)
                .addOnSuccessListener(aVoid -> {
                    Log.d("NotificationManager", "Notification saved to Firestore");

                    // Prepare FCM push payload
                    Map<String, String> pushData = new HashMap<>();
                    pushData.put("postId", notificationModel.getPostId());
                    pushData.put("type", notificationModel.getType());
                    pushData.put("senderId", notificationModel.getSenderId());
                    pushData.put("timestamp", String.valueOf(System.currentTimeMillis()));

                    sendPushNotification(
                            notificationModel.getUserToken(),
                            notificationModel.getTitle(),
                            notificationModel.getBody(),
                            pushData
                    );
                })
                .addOnFailureListener(e -> {
                    Log.e("NotificationManager", "Failed to save notification to Firestore", e);
                });
    }

    /**
     * Sends FCM after token is fetched.
     */
    public static void sendPushNotification(String userToken, String title, String body, Map<String, String> data) {
        new FetchAccessTokenTask(userToken, title, body, data).execute();
    }

    /**
     * Task to fetch access token then send the notification.
     */
    private static class FetchAccessTokenTask extends AsyncTask<Void, Void, String> {
        private final String userToken;
        private final String title;
        private final String body;
        private final Map<String, String> data;

        public FetchAccessTokenTask(String userToken, String title, String body, Map<String, String> data) {
            this.userToken = userToken;
            this.title = title;
            this.body = body;
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... voids) {
            OkHttpClient client = new OkHttpClient();

            Request request = new Request.Builder()
                    .url(ACCESS_TOKEN_URL)
                    .get()
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    JSONObject jsonResponse = new JSONObject(response.body().string());
                    return jsonResponse.getString("access_token");
                } else {
                    Log.e("FCM Error", "Failed to fetch access token: " + response.message());
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String accessToken) {
            if (accessToken != null) {
                sendPushNotificationWithToken(userToken, title, body, data, accessToken);
            } else {
                Log.e("FCM Error", "Access token was null, notification not sent.");
            }
        }
    }

    /**
     * Builds and sends the actual FCM payload.
     */
    public static void sendPushNotificationWithToken(String userToken, String title, String body, Map<String, String> data, String accessToken) {
        OkHttpClient client = new OkHttpClient();

        JSONObject message = new JSONObject();
        try {
            JSONObject notification = new JSONObject();
            notification.put("title", title);
            notification.put("body", body);

            JSONObject androidConfig = new JSONObject();
            androidConfig.put("priority", "high");

            JSONObject messagePayload = new JSONObject();
            messagePayload.put("token", userToken);
            messagePayload.put("notification", notification);
            messagePayload.put("android", androidConfig);

            // Data payload
            if (data != null && !data.isEmpty()) {
                JSONObject dataObject = new JSONObject();
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    dataObject.put(entry.getKey(), entry.getValue());
                }
                messagePayload.put("data", dataObject);
            }

            message.put("message", messagePayload);

        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody bodyRequest = RequestBody.create(
                message.toString(), MediaType.parse("application/json; charset=utf-8"));

        Request request = new Request.Builder()
                .url(FCM_URL)
                .post(bodyRequest)
                .addHeader("Authorization", "Bearer " + accessToken)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("FCM Error", "Notification sending failed", e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    Log.i("FCM Success", "Notification sent successfully!");
                } else {
                    Log.e("FCM Error", "Failed to send notification: " + response.message());
                    Log.e("FCM Error", "Response body: " + response.body().string());
                }
            }
        });
    }
}
