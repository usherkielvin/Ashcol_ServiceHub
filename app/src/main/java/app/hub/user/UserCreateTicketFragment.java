package app.hub.user;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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

    private EditText titleInput, descriptionInput, addressInput, contactInput;
    private Spinner serviceTypeSpinner;
    private Button createTicketButton;
    private Button mapButton;
    private TokenManager tokenManager;

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
        serviceTypeSpinner = view.findViewById(R.id.spinnerServiceType);
        createTicketButton = view.findViewById(R.id.btnSubmit);
        mapButton = view.findViewById(R.id.btnMap);

        tokenManager = new TokenManager(getContext());

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.service_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        serviceTypeSpinner.setAdapter(adapter);

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
        String serviceType = serviceTypeSpinner != null ? serviceTypeSpinner.getSelectedItem().toString() : "";
        String address = addressInput != null ? addressInput.getText().toString().trim() : "";
        String contact = contactInput != null ? contactInput.getText().toString().trim() : "";

        if (title.isEmpty() || description.isEmpty() || address.isEmpty() || contact.isEmpty()) {
            Toast.makeText(getContext(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        CreateTicketRequest request = new CreateTicketRequest(title, description, serviceType, address, contact);
        ApiService apiService = ApiClient.getApiService();
        String token = tokenManager.getToken();

        if (token == null) {
            Toast.makeText(getContext(), "You are not logged in.", Toast.LENGTH_SHORT).show();
            return;
        }

        Call<CreateTicketResponse> call = apiService.createTicket(token, request);
        call.enqueue(new Callback<CreateTicketResponse>() {
            @Override
            public void onResponse(Call<CreateTicketResponse> call, Response<CreateTicketResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(getContext(), "Ticket created successfully", Toast.LENGTH_SHORT).show();
                    // Clear the input fields
                    if (titleInput != null) titleInput.setText("");
                    if (descriptionInput != null) descriptionInput.setText("");
                    if (addressInput != null) addressInput.setText("");
                    if (contactInput != null) contactInput.setText("");
                    if (serviceTypeSpinner != null) serviceTypeSpinner.setSelection(0);
                } else {
                    Toast.makeText(getContext(), "Failed to create ticket", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CreateTicketResponse> call, Throwable t) {
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
