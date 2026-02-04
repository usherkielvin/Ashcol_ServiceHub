package app.hub.manager;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import app.hub.api.EmployeeResponse;
import app.hub.api.TicketListResponse;
import app.hub.util.TokenManager;

/**
 * Firebase real-time listener for manager data
 * Listens to Firestore changes and updates ManagerDataManager cache
 */
public class FirebaseManagerListener {
    private static final String TAG = "FirebaseManagerListener";

    private FirebaseFirestore firestore;
    private TokenManager tokenManager;
    private Context context;

    private ListenerRegistration ticketListener;
    private ListenerRegistration employeeListener;

    private boolean isListening = false;

    public interface ConnectionStateListener {
        void onConnected();

        void onDisconnected();

        void onError(String error);
    }

    private ConnectionStateListener connectionStateListener;

    public FirebaseManagerListener(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.tokenManager = new TokenManager(context);
    }

    public void setConnectionStateListener(ConnectionStateListener listener) {
        this.connectionStateListener = listener;
    }

    /**
     * Start listening to Firestore for real-time updates
     */
    public void startListening() {
        if (isListening) {
            Log.d(TAG, "Already listening to Firestore");
            return;
        }

        String branchName = ManagerDataManager.getCachedBranchName();
        if (branchName == null || branchName.isEmpty() || branchName.equals("No Branch Assigned")) {
            Log.w(TAG, "No branch assigned, cannot start Firebase listeners");
            return;
        }

        Log.i(TAG, "Starting Firebase listeners for branch: " + branchName);
        isListening = true;

        // Listen to tickets collection filtered by branch
        startTicketListener(branchName);

        // Note: Employee data doesn't have real-time Firebase sync yet
        // We rely on API + cache for employees
    }

    /**
     * Start listening to ticket changes in Firestore
     */
    private void startTicketListener(String branchName) {
        ticketListener = firestore.collection("tickets")
                .whereEqualTo("branch", branchName)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Ticket listener error", error);
                        if (connectionStateListener != null) {
                            connectionStateListener.onError("Firestore error: " + error.getMessage());
                        }
                        return;
                    }

                    if (snapshots == null || snapshots.isEmpty()) {
                        Log.d(TAG, "No tickets in Firestore for branch: " + branchName);
                        if (connectionStateListener != null) {
                            connectionStateListener.onConnected();
                        }
                        return;
                    }

                    Log.i(TAG, "Received ticket updates from Firestore: " + snapshots.size() + " tickets");

                    // Process changes
                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        String ticketId = dc.getDocument().getString("ticketId");
                        String status = dc.getDocument().getString("status");

                        switch (dc.getType()) {
                            case ADDED:
                                Log.d(TAG, "New ticket: " + ticketId + " (" + status + ")");
                                break;
                            case MODIFIED:
                                Log.d(TAG, "Modified ticket: " + ticketId + " (" + status + ")");
                                break;
                            case REMOVED:
                                Log.d(TAG, "Removed ticket: " + ticketId);
                                break;
                        }
                    }

                    // Refresh ticket cache from API to get complete data
                    // Firebase gives us the signal, API gives us the full data
                    refreshTicketsFromApi();

                    if (connectionStateListener != null) {
                        connectionStateListener.onConnected();
                    }
                });
    }

    /**
     * Refresh tickets from API when Firestore signals a change
     */
    private void refreshTicketsFromApi() {
        Log.d(TAG, "Refreshing tickets from API due to Firestore change");

        ManagerDataManager.refreshTickets(context, new ManagerDataManager.DataLoadCallback() {
            @Override
            public void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees) {
                // Not used in this callback
            }

            @Override
            public void onTicketsLoaded(List<TicketListResponse.TicketItem> tickets) {
                Log.i(TAG, "Tickets refreshed from API: " + tickets.size() + " tickets");
            }

            @Override
            public void onLoadComplete() {
                Log.d(TAG, "Ticket refresh complete");
            }

            @Override
            public void onLoadError(String error) {
                Log.e(TAG, "Failed to refresh tickets from API: " + error);
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

        Log.i(TAG, "Stopping Firebase listeners");

        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }

        if (employeeListener != null) {
            employeeListener.remove();
            employeeListener = null;
        }

        isListening = false;

        if (connectionStateListener != null) {
            connectionStateListener.onDisconnected();
        }
    }

    /**
     * Check if currently listening
     */
    public boolean isListening() {
        return isListening;
    }
}
