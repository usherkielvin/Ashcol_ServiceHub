package hans.ph;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.servicehub.adapter.ChatAdapter;
import com.servicehub.model.ChatRequest;
import com.servicehub.model.ChatResponse;
import com.servicehub.model.Message;
import hans.ph.api.ApiClient;
import hans.ph.api.ApiService;
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

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        apiService = ApiClient.getApiService();

        FloatingActionButton fab = findViewById(R.id.fab_chatbot);
        fab.setOnClickListener(v -> showChatbotDialog());

        Button profileButton = findViewById(R.id.button);
        if (profileButton != null) {
            profileButton.setOnClickListener(v -> {
                Intent intent = new Intent(this, ProfileActivity.class);
                String email = getIntent().getStringExtra(EXTRA_EMAIL);
                if (email != null) {
                    intent.putExtra(ProfileActivity.EXTRA_EMAIL, email);
                }
                startActivity(intent);
            });
        }
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

        // Add a welcome message
        messageList.add(new Message("Hello! How can I help you today?", false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        sendButton.setOnClickListener(v -> {
            String messageText = messageEditText.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText, messageList, chatAdapter, recyclerView);
                messageEditText.setText("");
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
            public void onResponse(Call<ChatResponse> call, Response<ChatResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String reply = response.body().getReply();
                    messageList.add(new Message(reply, false));
                    chatAdapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "No error body";
                        Log.e(TAG, "API Error: " + response.code() + " " + errorBody);
                        messageList.add(new Message("Error: " + response.code(), false));
                        chatAdapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    } catch (IOException e) {
                        Log.e(TAG, "Error parsing error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ChatResponse> call, Throwable t) {
                Log.e(TAG, "API Failure: ", t);
                messageList.add(new Message("Failed to connect to the server.", false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.dashboard_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_profile) {
            Intent intent = new Intent(this, ProfileActivity.class);
            intent.putExtra(ProfileActivity.EXTRA_EMAIL, getIntent().getStringExtra(EXTRA_EMAIL));
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
