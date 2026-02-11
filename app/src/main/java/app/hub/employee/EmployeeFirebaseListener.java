package app.hub.employee;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import app.hub.util.TokenManager;

/**
 * Firebase real-time listener for technician tickets
 * Listens to Firestore for ticket assignments and updates
 */
public class EmployeeFirebaseListener {
    private static final String TAG = "EmployeeFirebaseListener";

    private FirebaseFirestore firestore;
    private TokenManager tokenManager;
    private Context context;
    private ListenerRegistration ticketListener;
    private boolean isListening = false;

    public interface TicketChangeListener {
        void onTicketAssigned(String ticketId);
        void onTicketUpdated(String ticketId);
        void onTicketRemoved(String ticketId);
    }

    private TicketChangeListener changeListener;

    public EmployeeFirebaseListener(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.tokenManager = new TokenManager(context);
    }

    public void setChangeListener(TicketChangeListener listener) {
        this.changeListener = listener;
    }

    /**
     * Start listening to Firestore for tickets assigned to this technician
     */
    public void startListening() {
        if (isListening) {
            Log.d(TAG, "Already listening to Firestore");
            return;
        }

        int technicianId = tokenManager.getUserIdInt();
        if (technicianId <= 0) {
            Log.w(TAG, "Invalid technician ID, cannot start Firebase listener");
            return;
        }

        Log.i(TAG, "Starting Firebase listener for technician ID: " + technicianId);
        isListening = true;

        // Listen to tickets assigned to this technician
        ticketListener = firestore.collection("tickets")
                .whereEqualTo("assignedTo", technicianId)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Ticket listener error", error);
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "No tickets in Firestore for technician: " + technicianId);
                        return;
                    }

                    Log.i(TAG, "Received ticket updates from Firestore: " + snapshots.size() + " tickets");

                    // Process changes
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String ticketId = dc.getDocument().getString("ticketId");
                        String status = dc.getDocument().getString("status");

                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(TAG, "New ticket assigned: " + ticketId + " (" + status + ")");
                                if (changeListener != null) {
                                    changeListener.onTicketAssigned(ticketId);
                                }
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Ticket updated: " + ticketId + " (" + status + ")");
                                if (changeListener != null) {
                                    changeListener.onTicketUpdated(ticketId);
                                }
                                break;
                            case REMOVED:
                                Log.d(TAG, "Ticket removed: " + ticketId);
                                if (changeListener != null) {
                                    changeListener.onTicketRemoved(ticketId);
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
