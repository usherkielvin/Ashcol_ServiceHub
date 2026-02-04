package app.hub.manager;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.EmployeeResponse;
import app.hub.api.TicketListResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Centralized data manager for manager dashboard
 * Loads all data at startup so tabs are instantly ready
 */
public class ManagerDataManager {
    private static final String TAG = "ManagerDataManager";

    // Static data storage
    private static String cachedBranchName = null;
    private static List<EmployeeResponse.Employee> cachedEmployees = null;
    private static List<TicketListResponse.TicketItem> cachedTickets = null;
    private static boolean isDataLoaded = false;
    private static boolean isLoading = false;
    private static long lastLoadTime = 0;

    // Firebase real-time listener
    private static FirebaseManagerListener firebaseListener = null;

    // Cache duration - refresh if data is older than 3 minutes (matches backend
    // cache)
    private static final long CACHE_DURATION = 3 * 60 * 1000; // 3 minutes

    // Observer pattern for data changes
    private static final List<EmployeeDataChangeListener> employeeListeners = new ArrayList<>();

    // Listener interface for employee data changes
    public interface EmployeeDataChangeListener {
        void onEmployeeDataChanged(String branchName, List<EmployeeResponse.Employee> employees);
    }

    // Callbacks for UI updates
    public interface DataLoadCallback {
        void onEmployeesLoaded(String branchName, List<EmployeeResponse.Employee> employees);

        void onTicketsLoaded(List<TicketListResponse.TicketItem> tickets);

        void onLoadComplete();

        void onLoadError(String error);
    }

    /**
     * Register a listener for employee data changes
     */
    public static void registerEmployeeListener(EmployeeDataChangeListener listener) {
        if (listener != null && !employeeListeners.contains(listener)) {
            employeeListeners.add(listener);
            Log.d(TAG, "Employee listener registered. Total listeners: " + employeeListeners.size());
        }
    }

    /**
     * Unregister a listener for employee data changes
     */
    public static void unregisterEmployeeListener(EmployeeDataChangeListener listener) {
        if (listener != null) {
            employeeListeners.remove(listener);
            Log.d(TAG, "Employee listener unregistered. Total listeners: " + employeeListeners.size());
        }
    }

    /**
     * Notify all listeners of employee data changes
     */
    private static void notifyEmployeeListeners() {
        if (cachedBranchName != null && cachedEmployees != null) {
            Log.d(TAG, "Notifying " + employeeListeners.size() + " listeners of employee data change");
            for (EmployeeDataChangeListener listener : employeeListeners) {
                listener.onEmployeeDataChanged(cachedBranchName, new ArrayList<>(cachedEmployees));
            }
        }
    }

    /**
     * Load all manager data at startup
     */
    public static void loadAllData(Context context, DataLoadCallback callback) {
        long currentTime = System.currentTimeMillis();
        boolean isCacheStale = (currentTime - lastLoadTime) > CACHE_DURATION;

        if (isDataLoaded && !isCacheStale) {
            // Data is fresh, return cached data immediately
            if (callback != null) {
                if (cachedEmployees != null) {
                    callback.onEmployeesLoaded(cachedBranchName, cachedEmployees);
                }
                if (cachedTickets != null) {
                    callback.onTicketsLoaded(cachedTickets);
                }
                callback.onLoadComplete();
            }
            Log.d(TAG, "Using fresh cached data");
            return;
        }

        if (isLoading) {
            Log.d(TAG, "Already loading data, skipping");
            return;
        }

        // Data is stale or not loaded, refresh from API
        if (isCacheStale) {
            Log.d(TAG, "Cache is stale, refreshing from API");
        } else {
            Log.d(TAG, "No cached data, loading from API");
        }

        isLoading = true;

        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();

        if (token == null) {
            isLoading = false;
            if (callback != null) {
                callback.onLoadError("Not authenticated");
            }
            return;
        }

        // Load employees and tickets simultaneously
        loadEmployees(token, callback);
        loadTickets(token, callback);
    }

    private static void loadEmployees(String token, DataLoadCallback callback) {
        ApiService apiService = ApiClient.getApiService();
        Call<EmployeeResponse> call = apiService.getEmployees("Bearer " + token);

        call.enqueue(new Callback<EmployeeResponse>() {
            @Override
            public void onResponse(Call<EmployeeResponse> call, Response<EmployeeResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    EmployeeResponse employeeResponse = response.body();

                    if (employeeResponse.isSuccess()) {
                        cachedBranchName = employeeResponse.getBranch() != null ? employeeResponse.getBranch()
                                : "No Branch Assigned";
                        cachedEmployees = employeeResponse.getEmployees() != null
                                ? new ArrayList<>(employeeResponse.getEmployees())
                                : new ArrayList<>();

                        Log.d(TAG, "Employees loaded: " + cachedEmployees.size() + " in branch: " + cachedBranchName);

                        // Notify all registered listeners
                        notifyEmployeeListeners();

                        if (callback != null) {
                            callback.onEmployeesLoaded(cachedBranchName, cachedEmployees);
                        }

                        checkLoadComplete(callback);
                    } else {
                        Log.e(TAG, "Employee API returned success=false");
                        if (callback != null) {
                            callback.onLoadError("Failed to load employees");
                        }
                    }
                } else {
                    Log.e(TAG, "Employee API response not successful");
                    if (callback != null) {
                        callback.onLoadError("Failed to load employees");
                    }
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                Log.e(TAG, "Employee API network error: " + t.getMessage(), t);
                if (callback != null) {
                    callback.onLoadError("Network error loading employees: " + t.getMessage());
                }
            }
        });
    }

    private static void loadTickets(String token, DataLoadCallback callback) {
        ApiService apiService = ApiClient.getApiService();
        Call<TicketListResponse> call = apiService.getManagerTickets("Bearer " + token);

        call.enqueue(new Callback<TicketListResponse>() {
            @Override
            public void onResponse(Call<TicketListResponse> call, Response<TicketListResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    TicketListResponse ticketResponse = response.body();

                    if (ticketResponse.isSuccess()) {
                        cachedTickets = new ArrayList<>(ticketResponse.getTickets());

                        Log.d(TAG, "Tickets loaded: " + cachedTickets.size());

                        if (callback != null) {
                            callback.onTicketsLoaded(cachedTickets);
                        }

                        checkLoadComplete(callback);
                    } else {
                        Log.e(TAG, "Ticket API returned success=false");
                        if (callback != null) {
                            callback.onLoadError("Failed to load tickets");
                        }
                    }
                } else {
                    Log.e(TAG, "Ticket API response not successful");
                    if (callback != null) {
                        callback.onLoadError("Failed to load tickets");
                    }
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                Log.e(TAG, "Ticket API network error: " + t.getMessage(), t);
                if (callback != null) {
                    callback.onLoadError("Network error loading tickets: " + t.getMessage());
                }
            }
        });
    }

    private static void checkLoadComplete(DataLoadCallback callback) {
        // Check if both employees and tickets are loaded
        // Note: We mark as complete even if one fails, so UI can still show partial
        // data
        boolean employeesReady = cachedEmployees != null;
        boolean ticketsReady = cachedTickets != null;

        if (employeesReady && ticketsReady) {
            isDataLoaded = true;
            isLoading = false;
            lastLoadTime = System.currentTimeMillis(); // Update timestamp
            Log.d(TAG, "All data loaded successfully");

            if (callback != null) {
                callback.onLoadComplete();
            }
        } else if (!isLoading) {
            // If we're not loading anymore but data is incomplete, still mark as complete
            // to prevent infinite waiting
            isLoading = false;
            if (callback != null) {
                callback.onLoadComplete();
            }
        }
    }

    // Getter methods for cached data
    public static String getCachedBranchName() {
        return cachedBranchName;
    }

    public static List<EmployeeResponse.Employee> getCachedEmployees() {
        return cachedEmployees != null ? new ArrayList<>(cachedEmployees) : new ArrayList<>();
    }

    public static List<TicketListResponse.TicketItem> getCachedTickets() {
        return cachedTickets != null ? new ArrayList<>(cachedTickets) : new ArrayList<>();
    }

    public static boolean isDataLoaded() {
        return isDataLoaded;
    }

    // Clear cache methods
    public static void clearEmployeeCache() {
        cachedEmployees = null;
        cachedBranchName = null;
        Log.d(TAG, "Employee cache cleared");
    }

    public static void clearTicketCache() {
        cachedTickets = null;
        Log.d(TAG, "Ticket cache cleared");
    }

    public static void clearAllCache() {
        cachedBranchName = null;
        cachedEmployees = null;
        cachedTickets = null;
        isDataLoaded = false;
        isLoading = false;
        lastLoadTime = 0;
        Log.d(TAG, "All cache cleared");
    }

    /**
     * Refresh specific data type
     */
    public static void refreshEmployees(Context context, DataLoadCallback callback) {
        Log.d(TAG, "Refreshing employees data");
        clearEmployeeCache();
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        if (token != null) {
            isLoading = true;
            loadEmployees(token, callback);
        }
    }

    public static void refreshTickets(Context context, DataLoadCallback callback) {
        clearTicketCache();
        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();
        if (token != null) {
            loadTickets(token, callback);
        }
    }

    /**
     * Force refresh all data (ignores cache)
     */
    public static void forceRefreshAllData(Context context, DataLoadCallback callback) {
        Log.d(TAG, "Force refreshing all data");
        clearAllCache();
        loadAllData(context, callback);
    }

    // Firebase real-time sync methods
    public static void startFirebaseListeners(Context context) {
        if (firebaseListener == null) {
            firebaseListener = new FirebaseManagerListener(context);
        }
        firebaseListener.startListening();
    }

    public static void stopFirebaseListeners() {
        if (firebaseListener != null) {
            firebaseListener.stopListening();
        }
    }
}
