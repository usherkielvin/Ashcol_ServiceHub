package app.hub.user;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import app.hub.util.TokenManager;

/**
 * Firebase real-time listener for customer tickets
 * Listens to Firestore for ticket updates when managers/techs interact with customer tickets
 */
public class CustomerFirebaseListener {
    private static final String TAG = "CustomerFirebaseListener";

    private FirebaseFirestore firestore;
    private TokenManager tokenManager;
    private Context context;
    private ListenerRegistration ticketListener;
    private boolean isListening = false;

    public interface TicketChangeListener {
        void onTicketUpdated(String ticketId);
        void onTicketStatusChanged(String ticketId, String newStatus);
    }

    private TicketChangeListener changeListener;

    public CustomerFirebaseListener(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.tokenManager = new TokenManager(context);
    }

    public void setChangeListener(TicketChangeListener listener) {
        this.changeListener = listener;
    }

    /**
     * Start listening to Firestore for tickets belonging to this customer
     */
    public void startListening() {
        if (isListening) {
            Log.d(TAG, "Already listening to Firestore");
            return;
        }

        int customerId = tokenManager.getUserIdInt();
        if (customerId <= 0) {
            Log.w(TAG, "Invalid customer ID, cannot start Firebase listener");
            return;
        }

        Log.i(TAG, "Starting Firebase listener for customer ID: " + customerId);
        isListening = true;

        // Listen to tickets belonging to this customer
        ticketListener = firestore.collection("tickets")
                .whereEqualTo("customerId", customerId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Ticket listener error", error);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "No tickets in Firestore for customer: " + customerId);
                        return;
                    }

                    Log.i(TAG, "Received ticket updates from Firestore: " + snapshots.size() + " tickets");

                    // Process changes
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String ticketId = dc.getDocument().getString("ticketId");
                        String status = dc.getDocument().getString("status");

                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(TAG, "New ticket created: " + ticketId + " (" + status + ")");
                                if (changeListener != null) {
                                    changeListener.onTicketUpdated(ticketId);
                                }
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Ticket updated: " + ticketId + " (" + status + ")");
                                if (changeListener != null) {
                                    changeListener.onTicketUpdated(ticketId);
                                    changeListener.onTicketStatusChanged(ticketId, status);
                                }
                                break;
                            case REMOVED:
                                Log.d(TAG, "Ticket removed: " + ticketId);
                                if (changeListener != null) {
                                    changeListener.onTicketUpdated(ticketId);
                                }
                                break;
                        }
                    }
                });
    }

    /**
     * Stop listening to Firestore
     */
    public void stopListening() {
        if (!isListening) {
            return;
        }

        Log.i(TAG, "Stopping Firebase listener");

        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }

        isListening = false;
    }

    /**
     * Check if currently listening
     */
    public boolean isListening() {
        return isListening;
    }
}
