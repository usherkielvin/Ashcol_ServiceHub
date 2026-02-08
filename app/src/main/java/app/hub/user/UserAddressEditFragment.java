package app.hub.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import app.hub.R;
import app.hub.common.FirestoreManager;

public class UserAddressEditFragment extends Fragment {

    public UserAddressEditFragment() {
        // Required empty public constructor
    }

    public static UserAddressEditFragment newInstance() {
        return new UserAddressEditFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment (Note: using typo in filename as
        // requested/found)
        return inflater.inflate(R.layout.freagment_userp_address_edit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        android.widget.ImageButton btnBack = view.findViewById(R.id.btnBack);
        com.google.android.material.button.MaterialButton btnSave = view.findViewById(R.id.btnSave);

        EditText etFullName = view.findViewById(R.id.etFullName);
        EditText etPhoneNumber = view.findViewById(R.id.etPhoneNumber);
        EditText etLocationDetails = view.findViewById(R.id.etLocationDetails);
        EditText etPostalCode = view.findViewById(R.id.etPostalCode);
        EditText etStreetDetails = view.findViewById(R.id.etStreetDetails);
        androidx.appcompat.widget.SwitchCompat switchDefault = view.findViewById(R.id.switchDefault);

        Bundle args = getArguments();
        final String addressId = args != null ? args.getString("id") : null;
        if (args != null) {
            etFullName.setText(args.getString("name", ""));
            etPhoneNumber.setText(args.getString("phone", ""));
            etLocationDetails.setText(args.getString("location_details", ""));
            etPostalCode.setText(args.getString("postal_code", ""));
            etStreetDetails.setText(args.getString("street_details", ""));
            switchDefault.setChecked(args.getBoolean("is_default", false));
        }

        btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        btnSave.setOnClickListener(v -> {
            String name = etFullName.getText().toString().trim();
            String phone = etPhoneNumber.getText().toString().trim();
            String location = etLocationDetails.getText().toString().trim();
            String postal = etPostalCode.getText().toString().trim();
            String street = etStreetDetails.getText().toString().trim();
            boolean isDefault = switchDefault.isChecked();

            if (name.isEmpty() || phone.isEmpty() || location.isEmpty()) {
                Toast.makeText(getContext(), "Please fill in required fields.", Toast.LENGTH_SHORT).show();
                return;
            }

            FirestoreManager firestoreManager = new FirestoreManager(requireContext());
            FirestoreManager.UserAddress address = new FirestoreManager.UserAddress();
            address.id = addressId;
            address.name = name;
            address.phone = phone;
            address.locationDetails = location;
            address.postalCode = postal;
            address.streetDetails = street;
            address.isDefault = isDefault;

            btnSave.setEnabled(false);
            firestoreManager.saveUserAddress(address, new FirestoreManager.AddressSaveListener() {
                @Override
                public void onSuccess() {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            Toast.makeText(getContext(), "Address saved.", Toast.LENGTH_SHORT).show();
                            getParentFragmentManager().popBackStack();
                        });
                    }
                }

                @Override
                public void onError(Exception e) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            btnSave.setEnabled(true);
                            Toast.makeText(getContext(), "Failed to save address.", Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });
    }
}
