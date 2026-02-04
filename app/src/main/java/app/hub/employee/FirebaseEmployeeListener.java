package app.hub.employee;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import app.hub.util.TokenManager;

/**
 * Firebase real-time listener for employee data.
 * Listens to Firestore changes for tickets assigned to this employee.
 */
public class FirebaseEmployeeListener {
    private static final String TAG = "FirebaseEmployeeListener";

    private FirebaseFirestore firestore;
    private TokenManager tokenManager;
    private Context context;
    private ListenerRegistration ticketListener;
    private boolean isListening = false;

    public interface OnScheduleChangeListener {
        void onScheduleChanged();

        void onError(String error);
    }

    private OnScheduleChangeListener listener;

    public FirebaseEmployeeListener(Context context) {
        this.context = context.getApplicationContext();
        this.firestore = FirebaseFirestore.getInstance();
        this.tokenManager = new TokenManager(context);
    }

    public void setOnScheduleChangeListener(OnScheduleChangeListener listener) {
        this.listener = listener;
    }

    public void startListening() {
        if (isListening)
            return;

        String employeeName = tokenManager.getName();
        if (employeeName == null || employeeName.isEmpty()) {
            Log.w(TAG, "No employee name found, cannot start listener");
            return;
        }

        Log.i(TAG, "Starting Firebase listener for employee: " + employeeName);
        isListening = true;

        // Listen to tickets where assigned_staff matches the employee name
        ticketListener = firestore.collection("tickets")
                .whereEqualTo("assigned_staff", employeeName)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Firestore listener error", error);
                        if (listener != null)
                            listener.onError(error.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        Log.d(TAG, "Received schedule update from Firestore");
                        if (listener != null)
                            listener.onScheduleChanged();
                    }
                });
    }

    public void stopListening() {
        if (!isListening)
            return;

        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }
        isListening = false;
        Log.i(TAG, "Stopped Firebase listener");
    }

    public boolean isListening() {
        return isListening;
    }
}
