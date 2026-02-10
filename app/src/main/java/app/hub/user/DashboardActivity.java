package app.hub.user;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.servicehub.adapter.ChatAdapter;
import com.servicehub.model.ChatRequest;
import com.servicehub.model.ChatResponse;
import com.servicehub.model.Message;
import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.util.TokenManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "email";
    private static final String TAG = "DashboardActivity";

    private ApiService apiService;
    private TokenManager tokenManager;
    private View navIndicator;
    private BottomNavigationView bottomNavigationView;
    private FloatingActionButton fabChatbot;
    private BroadcastReceiver fcmReceiver;

    public static final String EXTRA_SHOW_MY_TICKETS = "show_my_tickets";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        apiService = ApiClient.getApiService();
        tokenManager = new TokenManager(this);
        navIndicator = findViewById(R.id.navIndicator);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        fabChatbot = findViewById(R.id.fab_chatbot);

        initFcmReceiver();

        setupFab(fabChatbot);
        disableNavigationTooltips(bottomNavigationView);

        if (savedInstanceState == null) {
            // If returning from ticket creation, optionally show My Tickets tab
            boolean showMyTickets = getIntent().getBooleanExtra(EXTRA_SHOW_MY_TICKETS, false);
            Fragment initialFragment = showMyTickets ? new UserTicketsFragment() : new UserHomeFragment();
            int selectedItemId = showMyTickets ? R.id.my_ticket : R.id.homebtn;

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, initialFragment)
                    .commit();

            bottomNavigationView.post(() -> moveIndicatorToItem(selectedItemId, false));
            if (showMyTickets) {
                bottomNavigationView.setSelectedItemId(R.id.my_ticket);
                if (fabChatbot != null) fabChatbot.hide();
            } else if (fabChatbot != null) {
                fabChatbot.show();
            }
            getIntent().removeExtra(EXTRA_SHOW_MY_TICKETS);
        }

        handleNotificationIntent(getIntent());

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            
            // Handle Chatbot visibility based on fragment
            if (fabChatbot != null) {
                if (itemId == R.id.homebtn) {
                    fabChatbot.show();
                } else {
                    fabChatbot.hide();
                }
            }

            if (itemId == R.id.homebtn) {
                selectedFragment = new UserHomeFragment();
            } else if (itemId == R.id.my_ticket) {
                selectedFragment = new UserTicketsFragment();
            } else if (itemId == R.id.activitybtn) {
                selectedFragment = new UserNotificationFragment();
            } else if (itemId == R.id.Profile) {
                selectedFragment = new UserProfileFragment();
            } else if (itemId == R.id.blank) {
                return false; // Middle blank item for FAB
            }

            if (selectedFragment != null) {
                moveIndicatorToItem(itemId, true);
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, selectedFragment)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

            return false;
        });

        FloatingActionButton openSheet = findViewById(R.id.servicebtn);
        openSheet.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DashboardActivity.this);
            View view = getLayoutInflater().inflate(R.layout.uc_service_options, null);

            Button btnCleaning = view.findViewById(R.id.btn_cleaning);
            Button btnRepair = view.findViewById(R.id.btn_repair);
            Button btnInstallation = view.findViewById(R.id.btn_installation);
            Button btnMaintenance = view.findViewById(R.id.btn_maintenance);

            View.OnClickListener serviceClickListener = b -> {
                Button clickedButton = (Button) b;
                Intent intent = new Intent(this, ServiceSelectActivity.class);
                intent.putExtra("serviceType", clickedButton.getText().toString());
                startActivity(intent);
                bottomSheetDialog.dismiss();
            };

            btnCleaning.setOnClickListener(serviceClickListener);
            btnRepair.setOnClickListener(serviceClickListener);
            btnInstallation.setOnClickListener(serviceClickListener);
            btnMaintenance.setOnClickListener(serviceClickListener);

            bottomSheetDialog.setContentView(view);
            bottomSheetDialog.show();
        });
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleShowMyTickets(intent);
        handleNotificationIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (fcmReceiver != null) {
            IntentFilter filter = new IntentFilter("com.ashcol.FCM_MESSAGE");
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(fcmReceiver, filter, android.content.Context.RECEIVER_NOT_EXPORTED);
            } else {
                registerReceiver(fcmReceiver, filter);
            }
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (fcmReceiver != null) {
            unregisterReceiver(fcmReceiver);
        }
    }

    private void handleNotificationIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        String type = intent.getStringExtra("type");
        String ticketId = intent.getStringExtra("ticket_id");

        if (type != null && type.equals("payment_pending") && ticketId != null) {
            if (fabChatbot != null) {
                fabChatbot.hide();
            }
            openPaymentFlow(ticketId);
        }
    }

    private void initFcmReceiver() {
        fcmReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(android.content.Context context, Intent intent) {
                String type = intent.getStringExtra("type");
                String ticketId = intent.getStringExtra("ticket_id");
                if ("payment_pending".equals(type) && ticketId != null) {
                    if (fabChatbot != null) {
                        fabChatbot.hide();
                    }
                    openPaymentFlow(ticketId);
                }
            }
        };
    }

    private void openPaymentFlow(String ticketId) {
        startActivity(UserPaymentActivity.createIntent(this, ticketId, 0, 0.0, null, null));
    }

    private void handleShowMyTickets(Intent intent) {
        if (intent != null && intent.getBooleanExtra(EXTRA_SHOW_MY_TICKETS, false)) {
            intent.removeExtra(EXTRA_SHOW_MY_TICKETS);
            bottomNavigationView.setSelectedItemId(R.id.my_ticket);
            moveIndicatorToItem(R.id.my_ticket, true);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new UserTicketsFragment())
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
            if (fabChatbot != null) fabChatbot.hide();
        }
    }

    private void disableNavigationTooltips(BottomNavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        for (int i = 0; i < menu.size(); i++) {
            MenuItem item = menu.getItem(i);
            View view = navigationView.findViewById(item.getItemId());
            if (view != null) {
                view.setOnLongClickListener(v -> true);
                TooltipCompat.setTooltipText(view, null);
            }
        }
    }

    private void moveIndicatorToItem(int itemId, boolean animate) {
        View itemView = bottomNavigationView.findViewById(itemId);
        if (itemView == null || navIndicator == null) return;

        int itemWidth = itemView.getWidth();
        int indicatorWidth = navIndicator.getWidth();
        float targetX = itemView.getLeft() + (itemWidth / 2f) - (indicatorWidth / 2f);
        float targetY = 0f; 

        if (animate) {
            navIndicator.animate()
                    .translationX(targetX)
                    .translationY(targetY)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        } else {
            navIndicator.setTranslationX(targetX);
            navIndicator.setTranslationY(targetY);
        }
    }

    private void setupFab(FloatingActionButton fab) {
        if (fab == null) return;
//        fab.setOnTouchListener(new View.OnTouchListener() {
//            private float initialX, initialY;
//            private float initialTouchX, initialTouchY;
//
//            @Override
//            public boolean onTouch(View view, MotionEvent event) {
//                ViewGroup parentView = (ViewGroup) view.getParent();
//                switch (event.getActionMasked()) {
//                    case MotionEvent.ACTION_DOWN:
//                        initialX = view.getX();
//                        initialY = view.getY();
//                        initialTouchX = event.getRawX();
//                        initialTouchY = event.getRawY();
//                        return true;
//
//                    case MotionEvent.ACTION_MOVE:
//                        float newX = initialX + (event.getRawX() - initialTouchX);
//                        float newY = initialY + (event.getRawY() - initialTouchY);
//                        newX = Math.max(0, Math.min(newX, parentView.getWidth() - view.getWidth()));
//                        newY = Math.max(0, Math.min(newY, parentView.getHeight() - view.getHeight()));
//                        view.setY(newY);
//                        view.setX(newX);
//                        return true;
//
//                    case MotionEvent.ACTION_UP:
//                        float endX = event.getRawX();
//                        float endY = event.getRawY();
//                        if (isAClick(initialTouchX, endX, initialTouchY, endY)) {
//                            view.performClick();
//                        } else {
//                            float center = parentView.getWidth() / 2f;
//                            float finalX = view.getX() < center - view.getWidth() / 2f ? 0 : parentView.getWidth() - view.getWidth();
//                            ObjectAnimator.ofFloat(view, "x", view.getX(), finalX).setDuration(200).start();
//                        }
//                        return true;
//                }
//                return false;
//            }
//        });

        fab.setOnClickListener(v -> showChatbotDialog());
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > 200 || differenceY > 200);
    }

    private void showChatbotDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.chatbot_view, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        TextInputEditText messageEditText = dialogView.findViewById(R.id.messageEditText);
        com.google.android.material.button.MaterialButton sendButton = dialogView.findViewById(R.id.sendButton);
        View loadingIndicator = dialogView.findViewById(R.id.loadingIndicator);

        List<Message> messageList = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        messageList.add(new Message("Hello! How can I help you today?", false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        // Add TextWatcher to enable/disable send button based on input
        messageEditText.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Enable send button only if there's non-whitespace content
                String text = s.toString().trim();
                sendButton.setEnabled(!text.isEmpty() && !text.matches("\\s+"));
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
                // Not needed
            }
        });

        // Initially disable send button if input is empty
        sendButton.setEnabled(false);

        sendButton.setOnClickListener(v -> {
            if (messageEditText.getText() != null) {
                String messageText = messageEditText.getText().toString().trim();
                // Prevent empty or whitespace-only messages
                if (!messageText.isEmpty() && !messageText.matches("\\s+")) {
                    sendMessage(messageText, messageList, chatAdapter, recyclerView, sendButton, loadingIndicator);
                    messageEditText.setText("");
                }
            }
        });

        builder.create().show();
    }

    private void sendMessage(String messageText, List<Message> messageList, ChatAdapter chatAdapter, RecyclerView recyclerView, com.google.android.material.button.MaterialButton sendButton, View loadingIndicator) {
        messageList.add(new Message(messageText, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        // Show loading indicator and disable send button
        loadingIndicator.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);

        // Check network connectivity
        if (!isNetworkAvailable()) {
            messageList.add(new Message("No internet connection. Please check your network settings.", false));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            loadingIndicator.setVisibility(View.GONE);
            sendButton.setEnabled(true);
            return;
        }

        String token = tokenManager.getToken();
        if (token == null) {
            messageList.add(new Message("Your session has expired. Please log in again.", false));
            chatAdapter.notifyItemInserted(messageList.size() - 1);
            recyclerView.scrollToPosition(messageList.size() - 1);
            loadingIndicator.setVisibility(View.GONE);
            sendButton.setEnabled(true);
            return;
        }

        Call<ChatResponse> call = apiService.sendMessage("Bearer " + token, new ChatRequest(messageText));
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ChatResponse chatResponse = response.body();
                    
                    // Validate response format
                    if (chatResponse.getReply() == null || chatResponse.getReply().isEmpty()) {
                        messageList.add(new Message("Unable to process response. Please try again.", false));
                    } else if (chatResponse.getMethod() == null || 
                               (!chatResponse.getMethod().equals("keyword") && 
                                !chatResponse.getMethod().equals("ai") && 
                                !chatResponse.getMethod().equals("fallback"))) {
                        Log.w(TAG, "Invalid response method: " + chatResponse.getMethod());
                        messageList.add(new Message(chatResponse.getReply(), false));
                    } else {
                        messageList.add(new Message(chatResponse.getReply(), false));
                    }
                } else {
                    handleApiError(response, messageList);
                }
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
                
                // Hide loading indicator and re-enable send button
                loadingIndicator.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API Failure: ", t);
                String errorMessage = "Failed to connect to the server. Please check your internet connection and try again. For immediate assistance, contact support@ashcol.com";
                
                // Check if it's a timeout error
                if (t instanceof java.net.SocketTimeoutException) {
                    errorMessage = "Connection timeout. Please check your internet connection and try again.";
                }
                
                messageList.add(new Message(errorMessage, false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
                
                // Hide loading indicator and re-enable send button
                loadingIndicator.setVisibility(View.GONE);
                sendButton.setEnabled(true);
            }
        });
    }

    private boolean isNetworkAvailable() {
        android.net.ConnectivityManager connectivityManager = 
            (android.net.ConnectivityManager) getSystemService(android.content.Context.CONNECTIVITY_SERVICE);
        
        if (connectivityManager != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                android.net.Network network = connectivityManager.getActiveNetwork();
                if (network == null) return false;
                
                android.net.NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(network);
                return capabilities != null && (
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) ||
                    capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET)
                );
            } else {
                android.net.NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                return networkInfo != null && networkInfo.isConnected();
            }
        }
        return false;
    }

    private void handleApiError(Response<ChatResponse> response, List<Message> messageList) {
        String errorMessage;
        
        switch (response.code()) {
            case 400:
                errorMessage = "Unable to process your message. Please try rephrasing.";
                break;
            case 401:
                errorMessage = "Your session has expired. Please log in again.";
                break;
            case 403:
                errorMessage = "You don't have permission to use the chatbot. Please contact support@ashcol.com";
                break;
            case 404:
                errorMessage = "Chatbot service is temporarily unavailable. Please try again later.";
                break;
            case 500:
                errorMessage = "Something went wrong on our end. Our team has been notified. Please try again later or contact support@ashcol.com";
                break;
            case 503:
                errorMessage = "Chatbot service is temporarily unavailable for maintenance. Please try again later.";
                break;
            default:
                errorMessage = "An error occurred. Please try again or contact support@ashcol.com";
                break;
        }
        
        // Log detailed error for debugging
        String errorBody = "No error body";
        try (okhttp3.ResponseBody errorResponseBody = response.errorBody()) {
            if (errorResponseBody != null) {
                errorBody = errorResponseBody.string();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error parsing error body", e);
        }
        Log.e(TAG, "API Error: " + response.code() + " " + errorBody);
        
        messageList.add(new Message(errorMessage, false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.admin_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.admin_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new UserProfileFragment())
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
