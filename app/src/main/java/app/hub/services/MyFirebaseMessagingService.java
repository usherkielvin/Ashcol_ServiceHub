package app.hub.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import app.hub.R;
import app.hub.common.SplashActivity;
import app.hub.util.TokenManager;

/**
 * Firebase Cloud Messaging Service
 * Handles incoming push notifications for real-time updates
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCMService";
    private static final String CHANNEL_ID = "ashcol_notifications";
    private static final String CHANNEL_NAME = "Ashcol Service Hub";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    /**
     * Called when a new FCM token is generated
     * This happens on first app install and when token is refreshed
     */
    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Log.d(TAG, "FCM Token refreshed: " + token);

        // Save token locally
        TokenManager tokenManager = new TokenManager(this);
        tokenManager.saveFCMToken(token);

        // TODO: Send token to backend when user is logged in
        // This will be handled in login/register flows
    }

    /**
     * Called when a message is received
     */
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        Log.d(TAG, "Message received from: " + remoteMessage.getFrom());

        // Check if message contains a notification payload
        RemoteMessage.Notification notification = remoteMessage.getNotification();
        if (notification != null) {
            String title = notification.getTitle();
            String body = notification.getBody();
            Log.d(TAG, "Notification Title: " + title);
            Log.d(TAG, "Notification Body: " + body);

            showNotification(title, body, remoteMessage.getData());
        }

        // Check if message contains a data payload
        if (remoteMessage.getData().size() > 0) {
            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            String notificationType = remoteMessage.getData().get("type");
            String action = remoteMessage.getData().get("action");

            // Handle different notification types
            handleDataPayload(notificationType, action, remoteMessage.getData());

            if (notification == null) {
                String title = "Notification";
                String body = "You have a new update.";
                if ("payment_pending".equals(notificationType)) {
                    title = "Payment Required";
                    body = "Your ticket is ready for payment.";
                }
                showNotification(title, body, remoteMessage.getData());
            }
        }
    }

    /**
     * Handle data payload and trigger appropriate actions
     */
    private void handleDataPayload(String type, String action, java.util.Map<String, String> data) {
        Log.d(TAG, "Handling notification type: " + type + ", action: " + action);

        // Broadcast intent to notify active fragments/activities
        Intent broadcastIntent = new Intent("com.ashcol.FCM_MESSAGE");
        broadcastIntent.putExtra("type", type);
        broadcastIntent.putExtra("action", action);

        // Add all data to broadcast
        for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
            broadcastIntent.putExtra(entry.getKey(), entry.getValue());
        }

        sendBroadcast(broadcastIntent);
    }

    /**
     * Show notification in status bar
     */
    private void showNotification(String title, String body, java.util.Map<String, String> data) {
        Intent intent = new Intent(this, SplashActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Add notification data to intent
        for (java.util.Map.Entry<String, String> entry : data.entrySet()) {
            intent.putExtra(entry.getKey(), entry.getValue());
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification) // You'll need to add this icon
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            notificationManager.notify(0, notificationBuilder.build());
        }
    }

    /**
     * Create notification channel for Android 8.0+
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Notifications for ticket updates and assignments");

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
}
