package app.hub.manager;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.DashboardStatsResponse;
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
    private static DashboardStatsResponse.Stats cachedDashboardStats = null;
    private static List<DashboardStatsResponse.RecentTicket> cachedRecentTickets = null;
    private static boolean isDataLoaded = false;
    private static boolean isLoading = false;
    private static long lastLoadTime = 0;
    private static final List<DataLoadCallback> activeCallbacks = new ArrayList<>();

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

        void onDashboardStatsLoaded(DashboardStatsResponse.Stats stats,
                List<DashboardStatsResponse.RecentTicket> recentTickets);

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
                if (cachedDashboardStats != null) {
                    callback.onDashboardStatsLoaded(cachedDashboardStats, cachedRecentTickets);
                }
                callback.onLoadComplete();
            }
            Log.d(TAG, "Using fresh cached data");
            return;
        }

        if (isLoading) {
            Log.d(TAG, "Already loading data, queuing callback");
            if (callback != null && !activeCallbacks.contains(callback)) {
                activeCallbacks.add(callback);
            }
            return;
        }

        // Data is stale or not loaded, refresh from API
        if (isCacheStale) {
            Log.d(TAG, "Cache is stale, refreshing from API");
        } else {
            Log.d(TAG, "No cached data, loading from API");
        }

        isLoading = true;
        if (callback != null && !activeCallbacks.contains(callback)) {
            activeCallbacks.add(callback);
        }

        TokenManager tokenManager = new TokenManager(context);
        String token = tokenManager.getToken();

        if (token == null) {
            isLoading = false;
            notifyLoadError("Not authenticated");
            return;
        }

        // Load components
        loadEmployees(token, null);
        loadTickets(token, null);
        loadDashboardStats(token, null);
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

                        // Notify active callbacks
                        for (DataLoadCallback cb : new ArrayList<>(activeCallbacks)) {
                            cb.onEmployeesLoaded(cachedBranchName, cachedEmployees);
                        }
                        if (callback != null) {
                            callback.onEmployeesLoaded(cachedBranchName, cachedEmployees);
                        }

                        checkLoadComplete();
                    } else {
                        Log.e(TAG, "Employee API returned success=false");
                        notifyLoadError("Failed to load employees");
                    }
                } else {
                    Log.e(TAG, "Employee API response not successful");
                    notifyLoadError("Failed to load employees");
                }
            }

            @Override
            public void onFailure(Call<EmployeeResponse> call, Throwable t) {
                Log.e(TAG, "Employee API network error: " + t.getMessage(), t);
                notifyLoadError("Network error loading employees: " + t.getMessage());
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

                        for (DataLoadCallback cb : new ArrayList<>(activeCallbacks)) {
                            cb.onTicketsLoaded(cachedTickets);
                        }
                        if (callback != null) {
                            callback.onTicketsLoaded(cachedTickets);
                        }

                        checkLoadComplete();
                    } else {
                        Log.e(TAG, "Ticket API returned success=false");
                        notifyLoadError("Failed to load tickets");
                    }
                } else {
                    Log.e(TAG, "Ticket API response not successful");
                    notifyLoadError("Failed to load tickets");
                }
            }

            @Override
            public void onFailure(Call<TicketListResponse> call, Throwable t) {
                Log.e(TAG, "Ticket API network error: " + t.getMessage(), t);
                notifyLoadError("Network error loading tickets: " + t.getMessage());
            }
        });
    }

    private static void loadDashboardStats(String token, DataLoadCallback callback) {
        ApiService apiService = ApiClient.getApiService();
        Call<DashboardStatsResponse> call = apiService.getManagerDashboard("Bearer " + token);

        call.enqueue(new Callback<DashboardStatsResponse>() {
            @Override
            public void onResponse(Call<DashboardStatsResponse> call, Response<DashboardStatsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    DashboardStatsResponse dashboardResponse = response.body();

                    if (dashboardResponse.isSuccess()) {
                        cachedDashboardStats = dashboardResponse.getStats();
                        cachedRecentTickets = dashboardResponse.getRecentTickets();

                        Log.d(TAG, "Dashboard stats loaded: Total tickets = " +
                                (cachedDashboardStats != null ? cachedDashboardStats.getTotalTickets() : 0));

                        for (DataLoadCallback cb : new ArrayList<>(activeCallbacks)) {
                            cb.onDashboardStatsLoaded(cachedDashboardStats, cachedRecentTickets);
                        }
                        if (callback != null) {
                            callback.onDashboardStatsLoaded(cachedDashboardStats, cachedRecentTickets);
                        }

                        checkLoadComplete();
                    } else {
                        Log.e(TAG, "Dashboard API returned success=false");
                        notifyLoadError("Failed to load dashboard stats");
                    }
                } else {
                    Log.e(TAG, "Dashboard API response not successful");
                    notifyLoadError("Failed to load dashboard stats");
                }
            }

            @Override
            public void onFailure(Call<DashboardStatsResponse> call, Throwable t) {
                Log.e(TAG, "Dashboard API network error: " + t.getMessage(), t);
                notifyLoadError("Network error loading dashboard: " + t.getMessage());
            }
        });
    }

    private static void checkLoadComplete() {
        boolean employeesReady = cachedEmployees != null;
        boolean ticketsReady = cachedTickets != null;
        boolean statsReady = cachedDashboardStats != null;

        if (employeesReady && ticketsReady && statsReady) {
            isDataLoaded = true;
            isLoading = false;
            lastLoadTime = System.currentTimeMillis();
            Log.d(TAG, "All data loaded successfully");

            for (DataLoadCallback cb : new ArrayList<>(activeCallbacks)) {
                cb.onLoadComplete();
            }
            activeCallbacks.clear();
        }
    }

    private static void notifyLoadError(String error) {
        for (DataLoadCallback cb : new ArrayList<>(activeCallbacks)) {
            cb.onLoadError(error);
        }
        isLoading = false;
        activeCallbacks.clear();
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

    public static DashboardStatsResponse.Stats getCachedDashboardStats() {
        return cachedDashboardStats;
    }

    public static List<DashboardStatsResponse.RecentTicket> getCachedRecentTickets() {
        return cachedRecentTickets != null ? new ArrayList<>(cachedRecentTickets) : new ArrayList<>();
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
