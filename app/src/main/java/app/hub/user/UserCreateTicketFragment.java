package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import app.hub.R;
import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.CreateTicketRequest;
import app.hub.api.CreateTicketResponse;
import app.hub.map.MapSelectionActivity;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserCreateTicketFragment extends Fragment {

    private static final SimpleDateFormat DATE_FORMAT_API = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat DATE_FORMAT_DISPLAY = new SimpleDateFormat("MMM dd, yyyy",
            Locale.getDefault());

    private EditText titleInput, descriptionInput, addressInput, contactInput, dateInput;
    private Button createTicketButton;
    private Button mapButton;
    private TokenManager tokenManager;
    private Long selectedDateMillis = null;

    public UserCreateTicketFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_create_ticket, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize views - based on fragment_user_create_ticket.xml
        titleInput = view.findViewById(R.id.etTitle);
        descriptionInput = view.findViewById(R.id.etDescription);
        addressInput = view.findViewById(R.id.etLocation);
        contactInput = view.findViewById(R.id.etContact);
        dateInput = view.findViewById(R.id.etDate);
        createTicketButton = view.findViewById(R.id.btnSubmit);
        mapButton = view.findViewById(R.id.btnMap);

        tokenManager = new TokenManager(getContext());

        // Date picker - open when date field is clicked
        if (dateInput != null) {
            dateInput.setOnClickListener(v -> showDatePicker());
            dateInput.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus)
                    showDatePicker();
            });
        }

        // Set up map button click listener
        if (mapButton != null) {
            mapButton.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), MapSelectionActivity.class);
                startActivityForResult(intent, 1001); // Use a request code for result
            });
        }

        if (createTicketButton != null) {
            createTicketButton.setOnClickListener(v -> createTicket());
        }
    }

    private void showDatePicker() {
        long today = MaterialDatePicker.todayInUtcMilliseconds();
        Calendar maxCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        maxCal.add(Calendar.YEAR, 2);

        MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
        builder.setTitleText("Select preferred service date");
        builder.setSelection(selectedDateMillis != null ? selectedDateMillis : today);
        builder.setCalendarConstraints(new CalendarConstraints.Builder()
                .setStart(today)
                .setEnd(maxCal.getTimeInMillis())
                .build());

        MaterialDatePicker<Long> picker = builder.build();
        picker.addOnPositiveButtonClickListener(selection -> {
            selectedDateMillis = selection;
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(selection);
            if (dateInput != null)
                dateInput.setText(DATE_FORMAT_DISPLAY.format(cal.getTime()));
        });
        picker.show(getChildFragmentManager(), "DATE_PICKER");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == getActivity().RESULT_OK && data != null) {
            double latitude = data.getDoubleExtra("latitude", 0.0);
            double longitude = data.getDoubleExtra("longitude", 0.0);
            String address = data.getStringExtra("address");

            // Set the selected address to the location field
            if (addressInput != null && address != null) {
                addressInput.setText(address);
            }

            Toast.makeText(getContext(), "Location selected: " + address, Toast.LENGTH_SHORT).show();
        }
    }

    private void createTicket() {
        String title = titleInput != null ? titleInput.getText().toString().trim() : "";
        String description = descriptionInput != null ? descriptionInput.getText().toString().trim() : "";
        String serviceType = "General Service"; // Default service type since spinner is not available
        String address = addressInput != null ? addressInput.getText().toString().trim() : "";
        String contact = contactInput != null ? contactInput.getText().toString().trim() : "";

        if (title.isEmpty() || description.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        String preferredDate = null;
        if (selectedDateMillis != null) {
            Calendar cal = Calendar.getInstance(TimeZone.getDefault());
            cal.setTimeInMillis(selectedDateMillis);
            preferredDate = DATE_FORMAT_API.format(cal.getTime());
        }

        CreateTicketRequest request = new CreateTicketRequest(title, description, serviceType, address, contact,
                preferredDate);
        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        createTicketButton.setEnabled(false);
        createTicketButton.setText("Creating...");

        Call<CreateTicketResponse> call = apiService.createTicket("Bearer " + token, request);
        call.enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                // Reset button state
                createTicketButton.setEnabled(true);
                createTicketButton.setText("Submit");

                if (response.isSuccessful() && response.body() != null) {
                    CreateTicketResponse ticketResponse = response.body();

                    // Clear form fields
                    titleInput.setText("");
                    descriptionInput.setText("");
                    addressInput.setText("");
                    contactInput.setText("");

                    // Navigate to confirmation screen
                    Intent intent = new Intent(getActivity(), TicketConfirmationActivity.class);
                    intent.putExtra("ticket_id", ticketResponse.getTicketId());
                    intent.putExtra("status", ticketResponse.getStatus());
                    startActivity(intent);

                    // Close this fragment/activity
                    if (getActivity() != null) {
                        getActivity().finish();
                    }

                    // Clear form
                    clearForm();

                    Toast.makeText(getContext(), "Ticket created successfully!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Failed to create ticket. Please try again.", Toast.LENGTH_SHORT)
                            .show();
                }
            }

            @Override
            public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                // Reset button state
                createTicketButton.setEnabled(true);
                createTicketButton.setText("Submit");

                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearForm() {
        if (titleInput != null)
            titleInput.setText("");
        if (descriptionInput != null)
            descriptionInput.setText("");
        if (addressInput != null)
            addressInput.setText("");
        if (contactInput != null)
            contactInput.setText("");
        if (dateInput != null)
            dateInput.setText("");
        selectedDateMillis = null;
    }
}
