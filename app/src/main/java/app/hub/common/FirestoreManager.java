package app.hub.common;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.MetadataChanges;

import app.hub.api.TicketListResponse;
import app.hub.api.UserResponse;
import app.hub.util.TokenManager;

public class FirestoreManager {
    private static final String TAG = "FirestoreManager";
    private FirebaseFirestore db;
    private TokenManager tokenManager;
    private ListenerRegistration userProfileListener;

    public interface UserProfileListener {
        void onProfileUpdated(UserResponse.Data profile);

        void onError(Exception e);
    }

    public FirestoreManager(Context context) {
        db = FirebaseFirestore.getInstance();
        tokenManager = new TokenManager(context);
    }

    public void listenToUserProfile(UserProfileListener listener) {
        // We use the numeric ID from the backend as the document ID in Firestore
        // Need to make sure we have the ID available. For now, we might need to rely on
        // what we have.
        // However, ProfileController uses user->id as the document ID.
        // We might not have the numeric ID readily available in TokenManager if we
        // haven't stored it explicitly.
        // But we do have 'user_id' probably if we checked login response.

        // Strategy: Use email to query if ID is not available, OR rely on the fact that
        // we should store ID.
        // Let's assume for a moment we might need to query by email first or store the
        // ID.
        // Checking TokenManager, we have getUserId() which returns email.
        // So we might need to change how we identify the document.

        // Wait, the backend uses user->id (int).
        // TokenManager.getUserId() returns email currently.
        // We need to support fetching by email OR store the integer ID.

        // For now, let's try to query by email since that's unique and safe.
        String email = tokenManager.getEmail();
        if (email == null) {
            listener.onError(new Exception("No user email found"));
            return;
        }

        userProfileListener = db.collection("users")
                .whereEqualTo("email", email)
                .addSnapshotListener(MetadataChanges.INCLUDE, (snapshots, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Listen failed: " + error);
                        listener.onError(error);
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        DocumentSnapshot document = snapshots.getDocuments().get(0);
                        if (document.exists()) {
                            UserResponse.Data profile = document.toObject(UserResponse.Data.class);
                            listener.onProfileUpdated(profile);
                        }
                    }
                });
    }

    public void stopListening() {
        if (userProfileListener != null) {
            userProfileListener.remove();
            userProfileListener = null;
        }
    }

    public interface TicketListListener {
        void onTicketsUpdated(java.util.List<app.hub.api.TicketListResponse.TicketItem> tickets);

        void onError(Exception e);
    }

    private ListenerRegistration ticketListener;

    public void listenToMyTickets(TicketListListener listener) {
        String userId = tokenManager.getUserId(); // This is email currently as per previous steps
        if (userId == null) {
            listener.onError(new Exception("No user found"));
            return;
        }

        // We need to query tickets where customerId matches.
        // Note: The backend syncs ticket with 'customerId' field.
        // We need to match this with what we have.
        // Backend 'customerId' is likely an int ID.
        // TokenManager stores email.
        // Ideally we should use the int ID.
        // For now, let's assume we can query by email if the backend syncs it,
        // OR we rely on the fact that we might have skipped syncing email to tickets.
        // Let's check what we plan to sync in TicketController.

        // In the plan: 'customerId' => $ticket->customer_id
        // We probably need to fetch the integer ID from the profile first or valid
        // login response.
        // Since we don't have it easily in TokenManager without an extra call or
        // storage,
        // let's try to query by something else or Update TokenManager to store int ID.

        // Actually, let's update TokenManager to store ID if possible.
        // But for this step, let's assume we sync 'customerEmail' to ticket in
        // Firestore for easier querying from Android.

        ticketListener = db.collection("tickets")
                .whereEqualTo("customerEmail", userId) // Query by email
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        listener.onError(error);
                        return;
                    }

                    if (snapshots != null) {
                        java.util.List<app.hub.api.TicketListResponse.TicketItem> tickets = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            app.hub.api.TicketListResponse.TicketItem ticket = doc
                                    .toObject(app.hub.api.TicketListResponse.TicketItem.class);
                            tickets.add(ticket);
                        }
                        listener.onTicketsUpdated(tickets);
                    }
                });
    }

    public void stopTicketListening() {
        if (ticketListener != null) {
            ticketListener.remove();
            ticketListener = null;
        }
    }

    public interface BranchListListener {
        void onBranchesUpdated(java.util.List<Branch> branches);

        void onError(Exception e);
    }

    public static class Branch {
        public int id;
        public String name;
        public String location;
        public String address;
        public double latitude;
        public double longitude;
        public boolean isActive;

        public Branch() {
        }
    }

    private ListenerRegistration branchListener;

    public void listenToBranches(BranchListListener listener) {
        branchListener = db.collection("branches")
                .whereEqualTo("isActive", true)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null) {
                        listener.onError(error);
                        return;
                    }

                    if (snapshots != null) {
                        java.util.List<Branch> branches = new java.util.ArrayList<>();
                        for (DocumentSnapshot doc : snapshots) {
                            Branch branch = doc.toObject(Branch.class);
                            branches.add(branch);
                        }
                        listener.onBranchesUpdated(branches);
                    }
                });
    }

    public void stopBranchListening() {
        if (branchListener != null) {
            branchListener.remove();
            branchListener = null;
        }
    }
}
