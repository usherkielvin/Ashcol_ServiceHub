package app.hub.util;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import app.hub.R;
import app.hub.user.DashboardActivity;

/**
 * Helper class for creating and showing local notifications
 */
public class NotificationHelper {
    
    private static final String CHANNEL_ID = "ashcol_payment_channel";
    private static final String CHANNEL_NAME = "Payment Notifications";
    private static final String CHANNEL_DESC = "Notifications for payment requests";
    
    /**
     * Create notification channel (required for Android 8.0+)
     */
    public static void createNotificationChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.enableLights(true);
            
            NotificationManager notificationManager = 
                    context.getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * Show payment request notification
     */
    public static void showPaymentRequestNotification(Context context, String ticketId, String serviceType, double amount, int customerId) {
        // Create notification channel first
        createNotificationChannel(context);
        
        // Create intent to open payment selection activity directly
        Intent intent = new Intent(context, app.hub.user.PaymentSelectionActivity.class);
        intent.putExtra("ticket_id", ticketId);
        intent.putExtra("service_type", serviceType);
        intent.putExtra("amount", amount);
        intent.putExtra("customer_id", customerId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        
        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                (int) System.currentTimeMillis(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        
        // Build notification
        String title = "Payment Required";
        String message = String.format("Your service is complete! Please pay ₱%.2f to finish.", amount);
        
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setVibrate(new long[]{0, 500, 200, 500})
                .addAction(R.drawable.ic_wallet, "Pay Now", pendingIntent);
        
        // Show notification
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        try {
            notificationManager.notify(ticketId.hashCode(), builder.build());
        } catch (SecurityException e) {
            android.util.Log.e("NotificationHelper", "Permission denied for notification", e);
        }
    }
    
    /**
     * Cancel notification for a specific ticket
     */
    public static void cancelNotification(Context context, String ticketId) {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.cancel(ticketId.hashCode());
    }
}
