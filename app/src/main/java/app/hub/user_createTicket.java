package app.hub;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import app.hub.api.ApiClient;
import app.hub.api.ApiService;
import app.hub.api.CreateTicketRequest;
import app.hub.api.CreateTicketResponse;
import app.hub.util.TokenManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class user_createTicket extends Fragment {

    private TextInputEditText titleInput, descriptionInput, addressInput, contactInput;
    private TextInputLayout titleInputLayout, descriptionInputLayout, addressInputLayout, contactInputLayout;
    private Spinner serviceTypeSpinner;
    private Button createTicketButton;
    private TokenManager tokenManager;

    public user_createTicket() {
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

        titleInput = view.findViewById(R.id.titleInput);
        descriptionInput = view.findViewById(R.id.descriptionInput);
        addressInput = view.findViewById(R.id.addressInput);
        contactInput = view.findViewById(R.id.contactInput);
        titleInputLayout = view.findViewById(R.id.titleInputLayout);
        descriptionInputLayout = view.findViewById(R.id.descriptionInputLayout);
        addressInputLayout = view.findViewById(R.id.addressInputLayout);
        contactInputLayout = view.findViewById(R.id.contactInputLayout);
        serviceTypeSpinner = view.findViewById(R.id.serviceTypeSpinner);
        createTicketButton = view.findViewById(R.id.createTicketButton);
        tokenManager = new TokenManager(getContext());

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.service_types, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        serviceTypeSpinner.setAdapter(adapter);

        // Hide the form fields initially
        setFormVisibility(View.GONE);

        serviceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    setFormVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        createTicketButton.setOnClickListener(v -> createTicket());
    }

    private void setFormVisibility(int visibility) {
        titleInputLayout.setVisibility(visibility);
        descriptionInputLayout.setVisibility(visibility);
        addressInputLayout.setVisibility(visibility);
        contactInputLayout.setVisibility(visibility);
        createTicketButton.setVisibility(visibility);
    }

    private void createTicket() {
        String title = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        String serviceType = serviceTypeSpinner.getSelectedItem().toString();
        String address = addressInput.getText().toString().trim();
        String contact = contactInput.getText().toString().trim();

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
                    titleInput.setText("");
                    descriptionInput.setText("");
                    addressInput.setText("");
                    contactInput.setText("");
                    serviceTypeSpinner.setSelection(0);
                    setFormVisibility(View.GONE);
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
