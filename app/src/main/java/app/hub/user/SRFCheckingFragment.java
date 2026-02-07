package app.hub.user;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;

import app.hub.R;

public class SRFCheckingFragment extends Fragment {
    
    private TextView tvFullname, tvPhoneNumber, tvAddress, tvLandmark;
    private TextView tvServiceType, tvSpecificService, tvUnitType, tvOtherDetails;
    private TextView tvPrefDate;
    private TextView tvSummaryItemName, tvSummaryItemPrice, tvSummaryTotal;
    private TextView tvAttachmentPlaceholder;
    private View layoutAttachment;
    private androidx.viewpager2.widget.ViewPager2 imageViewPager;
    private LinearLayout imageIndicatorContainer;
    private MaterialButton btnBack, btnConfirmed;
    
    // Data from previous screen
    private String fullName, phoneNumber, address, landmark;
    private String serviceType, specificService, unitType, otherDetails;
    private String preferredDate;
    private Uri imageUri1, imageUri2;
    private java.util.ArrayList<Uri> imageUris = new java.util.ArrayList<>();
    
    public static SRFCheckingFragment newInstance(
            String fullName, String phoneNumber, String address, String landmark,
            String serviceType, String specificService, String unitType, String otherDetails,
            String preferredDate, Uri imageUri1, Uri imageUri2) {
        
        SRFCheckingFragment fragment = new SRFCheckingFragment();
        Bundle args = new Bundle();
        args.putString("fullName", fullName);
        args.putString("phoneNumber", phoneNumber);
        args.putString("address", address);
        args.putString("landmark", landmark);
        args.putString("serviceType", serviceType);
        args.putString("specificService", specificService);
        args.putString("unitType", unitType);
        args.putString("otherDetails", otherDetails);
        args.putString("preferredDate", preferredDate);
        if (imageUri1 != null) {
            args.putString("imageUri1", imageUri1.toString());
        }
        if (imageUri2 != null) {
            args.putString("imageUri2", imageUri2.toString());
        }
        fragment.setArguments(args);
        return fragment;
    }
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            fullName = getArguments().getString("fullName");
            phoneNumber = getArguments().getString("phoneNumber");
            address = getArguments().getString("address");
            landmark = getArguments().getString("landmark");
            serviceType = getArguments().getString("serviceType");
            specificService = getArguments().getString("specificService");
            unitType = getArguments().getString("unitType");
            otherDetails = getArguments().getString("otherDetails");
            preferredDate = getArguments().getString("preferredDate");
            
            String imageUri1String = getArguments().getString("imageUri1");
            String imageUri2String = getArguments().getString("imageUri2");
            
            if (imageUri1String != null) {
                imageUri1 = Uri.parse(imageUri1String);
                imageUris.add(imageUri1);
            }
            if (imageUri2String != null) {
                imageUri2 = Uri.parse(imageUri2String);
                imageUris.add(imageUri2);
            }
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_srf_checking, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initViews(view);
        populateData();
        setupButtons();
    }
    
    private void initViews(View view) {
        tvFullname = view.findViewById(R.id.tvFullname);
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber);
        tvAddress = view.findViewById(R.id.tvAddress);
        tvLandmark = view.findViewById(R.id.tvLandmark);
        
        tvServiceType = view.findViewById(R.id.tvServiceType);
        tvSpecificService = view.findViewById(R.id.tvSpecificService);
        tvUnitType = view.findViewById(R.id.tvUnitType);
        tvOtherDetails = view.findViewById(R.id.tvOtherDetails);
        
        tvPrefDate = view.findViewById(R.id.tvPrefDate);
        
        tvSummaryItemName = view.findViewById(R.id.tvSummaryItemName);
        tvSummaryItemPrice = view.findViewById(R.id.tvSummaryItemPrice);
        tvSummaryTotal = view.findViewById(R.id.tvSummaryTotal);
        
        layoutAttachment = view.findViewById(R.id.layoutAttachment);
        imageViewPager = view.findViewById(R.id.imageViewPager);
        imageIndicatorContainer = view.findViewById(R.id.imageIndicatorContainer);
        tvAttachmentPlaceholder = view.findViewById(R.id.tvAttachmentPlaceholder);
        
        btnBack = view.findViewById(R.id.btnBack);
        btnConfirmed = view.findViewById(R.id.btnConfirmed);
    }
    
    private void populateData() {
        // Customer Information
        tvFullname.setText(fullName != null ? fullName : "N/A");
        tvPhoneNumber.setText(phoneNumber != null ? phoneNumber : "N/A");
        
        // Location Details
        tvAddress.setText(address != null ? address : "N/A");
        tvLandmark.setText(landmark != null && !landmark.isEmpty() ? landmark : "No landmark provided");
        
        // Service Details
        tvServiceType.setText(serviceType != null ? serviceType : "N/A");
        tvSpecificService.setText(specificService != null ? specificService : "N/A");
        tvUnitType.setText(unitType != null ? unitType : "N/A");
        tvOtherDetails.setText(otherDetails != null && !otherDetails.isEmpty() ? otherDetails : "No additional details");
        
        // Schedule
        tvPrefDate.setText(preferredDate != null ? preferredDate : "N/A");
        
        // Summary
        String summaryName = (unitType != null ? unitType + " AC " : "") + (specificService != null ? specificService : "Service");
        tvSummaryItemName.setText(summaryName);
        
        // Note: Price calculation would need to be implemented based on your pricing logic
        tvSummaryItemPrice.setText("Price on request");
        tvSummaryTotal.setText("Price on request");
        
        // Attachment
        if (!imageUris.isEmpty()) {
            layoutAttachment.setVisibility(View.GONE);
            imageViewPager.setVisibility(View.VISIBLE);
            
            // Setup ViewPager with images
            ImagePagerAdapter adapter = new ImagePagerAdapter(imageUris);
            imageViewPager.setAdapter(adapter);
            
            // Show indicators if more than one image
            if (imageUris.size() > 1) {
                imageIndicatorContainer.setVisibility(View.VISIBLE);
                setupIndicators(imageUris.size());
                setCurrentIndicator(0);
                
                imageViewPager.registerOnPageChangeCallback(new androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        setCurrentIndicator(position);
                    }
                });
            } else {
                imageIndicatorContainer.setVisibility(View.GONE);
            }
        } else {
            layoutAttachment.setVisibility(View.VISIBLE);
            imageViewPager.setVisibility(View.GONE);
            imageIndicatorContainer.setVisibility(View.GONE);
        }
    }
    
    private void setupIndicators(int count) {
        imageIndicatorContainer.removeAllViews();
        View[] indicators = new View[count];
        android.widget.LinearLayout.LayoutParams params = new android.widget.LinearLayout.LayoutParams(
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT,
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 0, 8, 0);
        
        for (int i = 0; i < count; i++) {
            indicators[i] = new View(getContext());
            indicators[i].setLayoutParams(params);
            indicators[i].setBackgroundResource(R.drawable.dot_unselected);
            imageIndicatorContainer.addView(indicators[i]);
        }
    }
    
    private void setCurrentIndicator(int position) {
        int childCount = imageIndicatorContainer.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View indicator = imageIndicatorContainer.getChildAt(i);
            if (i == position) {
                indicator.setBackgroundResource(R.drawable.dot_selected);
            } else {
                indicator.setBackgroundResource(R.drawable.dot_unselected);
            }
        }
    }
    
    // Simple adapter for ViewPager2
    private class ImagePagerAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<ImagePagerAdapter.ImageViewHolder> {
        private java.util.ArrayList<Uri> images;
        
        ImagePagerAdapter(java.util.ArrayList<Uri> images) {
            this.images = images;
        }
        
        @NonNull
        @Override
        public ImageViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            android.widget.ImageView imageView = new android.widget.ImageView(parent.getContext());
            imageView.setLayoutParams(new android.view.ViewGroup.LayoutParams(
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
            ));
            imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            return new ImageViewHolder(imageView);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
            holder.imageView.setImageURI(images.get(position));
        }
        
        @Override
        public int getItemCount() {
            return images.size();
        }
        
        class ImageViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
            android.widget.ImageView imageView;
            
            ImageViewHolder(android.widget.ImageView imageView) {
                super(imageView);
                this.imageView = imageView;
            }
        }
    }
    
    private void setupButtons() {
        btnBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
        
        btnConfirmed.setOnClickListener(v -> {
            // Notify parent activity to create the ticket
            if (getActivity() instanceof ServiceSelectActivity) {
                ((ServiceSelectActivity) getActivity()).confirmAndCreateTicket();
            }
        });
    }
}
