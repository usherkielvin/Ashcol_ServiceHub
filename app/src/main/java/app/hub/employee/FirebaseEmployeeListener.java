package app.hub.employee;

import android.content.Context;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

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
    private final List<ListenerRegistration> ticketListeners = new ArrayList<>();
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
        String employeeEmail = tokenManager.getEmail();

        if ((employeeName == null || employeeName.isEmpty())
                && (employeeEmail == null || employeeEmail.isEmpty())) {
            Log.w(TAG, "No employee identifier found, cannot start listener");
            return;
        }

        Log.i(TAG, "Starting Firebase listener for employee: " + employeeName + " / " + employeeEmail);
        isListening = true;

        if (employeeName != null && !employeeName.isEmpty()) {
            ticketListeners.add(firestore.collection("tickets")
                    .whereEqualTo("assigned_staff", employeeName)
                    .addSnapshotListener((snapshots, error) -> handleSnapshot(snapshots, error)));
        }

        if (employeeEmail != null && !employeeEmail.isEmpty()
                && (employeeName == null || !employeeEmail.equalsIgnoreCase(employeeName))) {
            ticketListeners.add(firestore.collection("tickets")
                    .whereEqualTo("assigned_staff", employeeEmail)
                    .addSnapshotListener((snapshots, error) -> handleSnapshot(snapshots, error)));
            ticketListeners.add(firestore.collection("tickets")
                .whereEqualTo("assigned_staff_email", employeeEmail)
                .addSnapshotListener((snapshots, error) -> handleSnapshot(snapshots, error)));
        }
    }

    private void handleSnapshot(com.google.firebase.firestore.QuerySnapshot snapshots, Exception error) {
        if (error != null) {
            Log.e(TAG, "Firestore listener error", error);
            if (listener != null) {
                listener.onError(error.getMessage());
            }
            return;
        }

        if (snapshots != null) {
            Log.d(TAG, "Received schedule update from Firestore (size=" + snapshots.size() + ")");
            if (listener != null) {
                listener.onScheduleChanged();
            }
        }
    }

    public void stopListening() {
        if (!isListening)
            return;

        for (ListenerRegistration registration : ticketListeners) {
            if (registration != null) {
                registration.remove();
            }
        }
        ticketListeners.clear();
        isListening = false;
        Log.i(TAG, "Stopped Firebase listener");
    }

    public boolean isListening() {
        return isListening;
    }
}
