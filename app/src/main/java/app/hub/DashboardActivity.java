package app.hub;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
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
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        apiService = ApiClient.getApiService();

        FloatingActionButton fab = findViewById(R.id.fab_chatbot);
        setupFab(fab);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new user_Home())
                    .commit();
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.home) {
                selectedFragment = new user_Home();
            } else if (itemId == R.id.my_ticket) {
                selectedFragment = new user_Ticket();
            } else if (itemId == R.id.notificationHistory) {
                selectedFragment = new user_Notification();
            } else if (itemId == R.id.Profile) {
                selectedFragment = new user_Profile();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragmentContainerView, selectedFragment)
                        .setReorderingAllowed(true)
                        .addToBackStack(null)
                        .commit();
                return true;
            }

            return false;
        });

        Button openSheet = findViewById(R.id.servicebtn);
        openSheet.setOnClickListener(v -> {
            BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(DashboardActivity.this);
            View view = getLayoutInflater().inflate(R.layout.service_options, null);

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

    private void setupFab(FloatingActionButton fab) {
        fab.setOnTouchListener(new View.OnTouchListener() {
            private float initialX, initialY;
            private float initialTouchX, initialTouchY;
            private long lastDownTime = 0;

            @Override
            public boolean onTouch(View view, MotionEvent event) {
                ViewGroup parentView = (ViewGroup) view.getParent();
                switch (event.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = view.getX();
                        initialY = view.getY();
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        lastDownTime = event.getDownTime();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        float newX = initialX + (event.getRawX() - initialTouchX);
                        float newY = initialY + (event.getRawY() - initialTouchY);

                        // Stay within parent bounds
                        newX = Math.max(0, Math.min(newX, parentView.getWidth() - view.getWidth()));
                        newY = Math.max(0, Math.min(newY, parentView.getHeight() - view.getHeight()));

                        view.setY(newY);
                        view.setX(newX);
                        return true;

                    case MotionEvent.ACTION_UP:
                        float endX = event.getRawX();
                        float endY = event.getRawY();
                        if (isAClick(initialTouchX, endX, initialTouchY, endY)) {
                            view.performClick(); // This will trigger the OnClickListener
                        } else {
                            float center = parentView.getWidth() / 2f;
                            float finalX = view.getX() < center - view.getWidth() / 2f ? 0 : parentView.getWidth() - view.getWidth();
                            ObjectAnimator.ofFloat(view, "x", view.getX(), finalX).setDuration(200).start();
                        }
                        return true;
                }
                return false;
            }
        });

        fab.setOnClickListener(v -> showChatbotDialog());
    }

    private boolean isAClick(float startX, float endX, float startY, float endY) {
        float differenceX = Math.abs(startX - endX);
        float differenceY = Math.abs(startY - endY);
        return !(differenceX > 200 || differenceY > 200); // Click threshold
    }

    private void showChatbotDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.chatbot_view, null);
        builder.setView(dialogView);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        TextInputEditText messageEditText = dialogView.findViewById(R.id.messageEditText);
        ImageButton sendButton = dialogView.findViewById(R.id.sendButton);

        List<Message> messageList = new ArrayList<>();
        ChatAdapter chatAdapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(chatAdapter);

        messageList.add(new Message("Hello! How can I help you today?", false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        sendButton.setOnClickListener(v -> {
            if (messageEditText.getText() != null) {
                String messageText = messageEditText.getText().toString().trim();
                if (!messageText.isEmpty()) {
                    sendMessage(messageText, messageList, chatAdapter, recyclerView);
                    messageEditText.setText("");
                }
            }
        });

        builder.create().show();
    }

    private void sendMessage(String messageText, List<Message> messageList, ChatAdapter chatAdapter, RecyclerView recyclerView) {
        messageList.add(new Message(messageText, true));
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        recyclerView.scrollToPosition(messageList.size() - 1);

        Call<ChatResponse> call = apiService.sendMessage(new ChatRequest(messageText));
        call.enqueue(new Callback<ChatResponse>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponse> call, @NonNull Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getReply();
                    messageList.add(new Message(reply, false));
                } else {
                    handleApiError(response, messageList);
                }
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API Failure: ", t);
                messageList.add(new Message("Failed to connect to the server.", false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void handleApiError(Response<ChatResponse> response, List<Message> messageList) {
        String errorBody = "No error body";
        try (okhttp3.ResponseBody errorResponseBody = response.errorBody()) {
            if (errorResponseBody != null) {
                errorBody = errorResponseBody.string();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error parsing error body", e);
            errorBody = "Error reading response";
        }
        Log.e(TAG, "API Error: " + response.code() + " " + errorBody);
        messageList.add(new Message("Error: " + response.code(), false));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView, new user_Profile())
                    .setReorderingAllowed(true)
                    .addToBackStack(null)
                    .commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
