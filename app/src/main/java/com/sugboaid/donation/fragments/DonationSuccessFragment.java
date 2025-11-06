package com.sugboaid.donation.fragments;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.button.MaterialButton;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.sugboaid.donation.R;
import com.sugboaid.models.Donation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Success screen fragment shown after successful donation submission
 * Displays animated checkmark, confetti effects, and QR code receipt
 */
public class DonationSuccessFragment extends BaseFragment {

    private static final String ARG_DONATION = "donation";

    // Views
    private ImageView ivSuccessIcon;
    private TextView tvSuccessTitle;
    private TextView tvSuccessMessage;
    private TextView tvDonorName;
    private TextView tvDonationType;
    private TextView tvDonationAmount;
    private TextView tvDonationDate;
    private TextView tvReceiptId;
    private ImageView ivQrCode;
    private MaterialButton btnBackToDashboard;
    private MaterialButton btnShareReceipt;
    private View confettiContainer;

    private Donation donation;

    public static DonationSuccessFragment newInstance(Donation donation) {
        DonationSuccessFragment fragment = new DonationSuccessFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DONATION, donation);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            donation = (Donation) getArguments().getSerializable(ARG_DONATION);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_donation_success, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Populate donation details
        populateDonationDetails();
        
        // Generate QR code
        generateQRCode();
        
        // Start animations
        startSuccessAnimations();
    }

    @Override
    protected void initViews(View view) {
        ivSuccessIcon = view.findViewById(R.id.iv_success_icon);
        tvSuccessTitle = view.findViewById(R.id.tv_success_title);
        tvSuccessMessage = view.findViewById(R.id.tv_success_message);
        tvDonorName = view.findViewById(R.id.tv_donor_name);
        tvDonationType = view.findViewById(R.id.tv_donation_type);
        tvDonationAmount = view.findViewById(R.id.tv_donation_amount);
        tvDonationDate = view.findViewById(R.id.tv_donation_date);
        tvReceiptId = view.findViewById(R.id.tv_receipt_id);
        ivQrCode = view.findViewById(R.id.iv_qr_code);
        btnBackToDashboard = view.findViewById(R.id.btn_back_to_dashboard);
        btnShareReceipt = view.findViewById(R.id.btn_share_receipt);
        confettiContainer = view.findViewById(R.id.confetti_container);
    }

    @Override
    protected void setupListeners() {
        btnBackToDashboard.setOnClickListener(v -> {
            animateButtonPress(btnBackToDashboard);
            navigateBackToDashboard();
        });

        btnShareReceipt.setOnClickListener(v -> {
            animateButtonPress(btnShareReceipt);
            shareReceipt();
        });
    }

    @Override
    protected void observeData() {
        // No data to observe in this fragment
    }

    @Override
    protected void refreshData() {
        // No data to refresh in this fragment
    }

    private void populateDonationDetails() {
        if (donation == null) return;

        // Set donor name
        String donorName = donation.getDonorName();
        if (donorName == null || donorName.trim().isEmpty() || "Anonymous".equals(donorName)) {
            tvDonorName.setText(getString(R.string.anonymous_donor));
        } else {
            tvDonorName.setText(donorName);
        }

        // Set donation type
        tvDonationType.setText(donation.getType().toString().toLowerCase());

        // Set donation amount
        tvDonationAmount.setText(donation.getFormattedAmount());

        // Set donation date
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault());
        tvDonationDate.setText(dateFormat.format(new Date(donation.getTimestamp())));

        // Set receipt ID
        tvReceiptId.setText(generateReceiptId());

        // Set success message based on donation type
        String message = donation.getType().toString().equals("CASH") ?
            getString(R.string.cash_donation_success_message) :
            getString(R.string.goods_donation_success_message);
        tvSuccessMessage.setText(message);
    }

    private void generateQRCode() {
        try {
            String qrContent = generateQRContent();
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(qrContent, BarcodeFormat.QR_CODE, 200, 200);
            ivQrCode.setImageBitmap(bitmap);
        } catch (WriterException e) {
            // Handle QR code generation error
            ivQrCode.setVisibility(View.GONE);
            showToast(getString(R.string.unable_to_generate_qr_code));
        }
    }

    private String generateQRContent() {
        if (donation == null) return "";

        StringBuilder qrContent = new StringBuilder();
        qrContent.append(getString(R.string.sugboaid_donation_receipt)).append("\n");
        qrContent.append("Receipt ID: ").append(generateReceiptId()).append("\n");
        qrContent.append("Donor: ").append(donation.getDonorName()).append("\n");
        qrContent.append("Type: ").append(donation.getType().toString()).append("\n");
        qrContent.append("Amount: ").append(donation.getFormattedAmount()).append("\n");
        qrContent.append("Date: ").append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            .format(new Date(donation.getTimestamp()))).append("\n");
        qrContent.append("Verified: ").append(donation.isVerified() ? "Yes" : "Pending");

        return qrContent.toString();
    }

    private String generateReceiptId() {
        return "RCP-" + System.currentTimeMillis() + "-" + 
               donation.getId().substring(0, Math.min(8, donation.getId().length())).toUpperCase();
    }

    private void startSuccessAnimations() {
        // Hide all views initially
        ivSuccessIcon.setAlpha(0f);
        tvSuccessTitle.setAlpha(0f);
        tvSuccessMessage.setAlpha(0f);
        ivQrCode.setAlpha(0f);
        btnBackToDashboard.setAlpha(0f);
        btnShareReceipt.setAlpha(0f);

        // Scale animations for success icon
        ivSuccessIcon.setScaleX(0f);
        ivSuccessIcon.setScaleY(0f);

        // Start confetti animation
        startConfettiAnimation();

        // Animate success icon with bounce effect
        ivSuccessIcon.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(600)
            .setStartDelay(300)
            .withEndAction(() -> {
                // Bounce effect
                ivSuccessIcon.animate()
                    .scaleX(1.1f)
                    .scaleY(1.1f)
                    .setDuration(200)
                    .withEndAction(() -> {
                        ivSuccessIcon.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(200)
                            .start();
                    })
                    .start();
            })
            .start();

        // Animate title
        tvSuccessTitle.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(600)
            .start();

        // Animate message
        tvSuccessMessage.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(800)
            .start();

        // Animate QR code
        ivQrCode.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(400)
            .setStartDelay(1000)
            .start();

        // Animate buttons
        btnBackToDashboard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(1200)
            .start();

        btnShareReceipt.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setStartDelay(1400)
            .start();
    }

    private void startConfettiAnimation() {
        // Simple confetti animation using view properties
        confettiContainer.setVisibility(View.VISIBLE);
        confettiContainer.setAlpha(1f);
        
        // Fade out confetti after animation
        confettiContainer.animate()
            .alpha(0f)
            .setDuration(3000)
            .setStartDelay(2000)
            .withEndAction(() -> confettiContainer.setVisibility(View.GONE))
            .start();
    }

    protected void animateButtonPress(View button) {
        button.animate()
            .scaleX(0.95f)
            .scaleY(0.95f)
            .setDuration(100)
            .withEndAction(() -> {
                button.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100)
                    .start();
            })
            .start();
    }

    private void navigateBackToDashboard() {
        // Navigate back to dashboard
        if (navController != null) {
            try {
                navController.navigate(R.id.action_success_to_dashboard);
            } catch (Exception e) {
                // Fallback navigation
                navigateBack();
            }
        } else {
            navigateBack();
        }
    }

    private void shareReceipt() {
        if (donation == null) return;

        String shareText = buildShareText();
        
        android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "SugboAid Donation Receipt");
        shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareText);
        
        try {
            startActivity(android.content.Intent.createChooser(shareIntent, getString(R.string.share_receipt)));
        } catch (Exception e) {
            showToast(getString(R.string.unable_to_share_receipt));
        }
    }

    private String buildShareText() {
        StringBuilder shareText = new StringBuilder();
        shareText.append("🎉 ").append(getString(R.string.sugboaid_donation_receipt)).append(" 🎉\n\n");
        shareText.append("Receipt ID: ").append(generateReceiptId()).append("\n");
        shareText.append("Donor: ").append(donation.getDonorName()).append("\n");
        shareText.append("Type: ").append(donation.getType().toString()).append("\n");
        shareText.append("Amount: ").append(donation.getFormattedAmount()).append("\n");
        shareText.append("Date: ").append(new SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            .format(new Date(donation.getTimestamp()))).append("\n\n");
        shareText.append(getString(R.string.thank_you_donation)).append("\n");
        shareText.append(getString(R.string.together_we_rebuild));
        
        return shareText.toString();
    }
}