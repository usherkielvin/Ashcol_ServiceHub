package app.hub.user;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import app.hub.R;
import app.hub.common.FirestoreManager;

public class UserAddressFragment extends Fragment {

    private FirestoreManager firestoreManager;
    private UserAddressAdapter adapter;
    private final List<UserAddressAdapter.AddressItem> items = new ArrayList<>();

    public UserAddressFragment() {
        // Required empty public constructor
    }

    public static UserAddressFragment newInstance() {
        return new UserAddressFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_userp_address, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        RecyclerView rvAddresses = view.findViewById(R.id.rvAddresses);
        com.google.android.material.button.MaterialButton btnAddAddress = view.findViewById(R.id.btnAddAddress);

        adapter = new UserAddressAdapter(items);
        adapter.setOnAddressActionListener(this::openEditAddress);
        rvAddresses.setLayoutManager(new LinearLayoutManager(getContext()));
        rvAddresses.setAdapter(adapter);

        btnAddAddress.setOnClickListener(v -> openEditAddress(null));

        firestoreManager = new FirestoreManager(requireContext());
        listenToAddresses();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (firestoreManager != null) {
            firestoreManager.stopAddressListening();
        }
    }

    private void listenToAddresses() {
        firestoreManager.listenToUserAddresses(new FirestoreManager.UserAddressListener() {
            @Override
            public void onAddressesUpdated(List<FirestoreManager.UserAddress> addresses) {
                List<UserAddressAdapter.AddressItem> updated = new ArrayList<>();
                if (addresses != null) {
                    for (FirestoreManager.UserAddress address : addresses) {
                        updated.add(new UserAddressAdapter.AddressItem(
                                address.id,
                                address.name,
                                address.phone,
                                address.locationDetails,
                                address.postalCode,
                                address.streetDetails,
                                address.isDefault));
                    }
                }
                if (adapter != null) {
                    adapter.setItems(updated);
                }
            }

            @Override
            public void onError(Exception e) {
                android.util.Log.e("UserAddress", "Failed to load addresses", e);
            }
        });
    }

    private void openEditAddress(@Nullable UserAddressAdapter.AddressItem item) {
        UserAddressEditFragment fragment = UserAddressEditFragment.newInstance();
        if (item != null) {
            Bundle args = new Bundle();
            args.putString("id", item.id);
            args.putString("name", item.name);
            args.putString("phone", item.phone);
            args.putString("location_details", item.locationDetails);
            args.putString("postal_code", item.postalCode);
            args.putString("street_details", item.streetDetails);
            args.putBoolean("is_default", item.isDefault);
            fragment.setArguments(args);
        }
        requireActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragmentContainerView, fragment)
                .addToBackStack(null)
                .commit();
    }
}
