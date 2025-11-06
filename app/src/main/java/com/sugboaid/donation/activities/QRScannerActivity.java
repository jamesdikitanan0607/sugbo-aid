package com.sugboaid.donation.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.sugboaid.donation.R;

import java.util.List;

/**
 * QR Scanner Activity for inventory stock updates
 * Handles camera permissions and QR code scanning functionality
 */
public class QRScannerActivity extends BaseActivity {
    
    public static final String EXTRA_SCAN_RESULT = "scan_result";
    public static final String EXTRA_SCAN_FORMAT = "scan_format";
    
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    
    private DecoratedBarcodeView barcodeView;
    private boolean isScanning = false;
    
    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result != null && result.getText() != null && !result.getText().isEmpty()) {
                handleScanResult(result);
            }
        }

        @Override
        public void possibleResultPoints(List<ResultPoint> resultPoints) {
            // Optional: Handle possible result points for UI feedback
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);
        
        initializeViews();
        setupToolbar();
        setupListeners();
        checkCameraPermission();
    }

    private void initializeViews() {
        barcodeView = findViewById(R.id.barcode_scanner);
        
        // Configure the scanner
        barcodeView.getBarcodeView().setDecoderFactory(new com.journeyapps.barcodescanner.DefaultDecoderFactory());
        barcodeView.initializeFromIntent(getIntent());
        barcodeView.decodeContinuous(callback);
    }

    private void setupToolbar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Scan QR Code");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void initViews() {
        // Views are initialized in initializeViews() method
        // This method is required by BaseActivity
    }

    @Override
    protected void setupListeners() {
        // QR Scanner specific listeners can be added here if needed
        // Currently handled by BarcodeCallback
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted, start scanning
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                         @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start scanning
                startScanning();
            } else {
                // Permission denied
                handleCameraPermissionDenied();
            }
        }
    }

    private void startScanning() {
        if (!isScanning) {
            isScanning = true;
            barcodeView.resume();
            showToast("Point camera at QR code to scan");
        }
    }

    private void stopScanning() {
        if (isScanning) {
            isScanning = false;
            barcodeView.pause();
        }
    }

    private void handleScanResult(BarcodeResult result) {
        try {
            stopScanning();
            
            String scannedText = result.getText();
            String format = result.getBarcodeFormat().toString();
            
            // Validate the scanned QR code format
            if (isValidInventoryQR(scannedText)) {
                // Return the result to the calling activity
                Intent resultIntent = new Intent();
                resultIntent.putExtra(EXTRA_SCAN_RESULT, scannedText);
                resultIntent.putExtra(EXTRA_SCAN_FORMAT, format);
                setResult(RESULT_OK, resultIntent);
                finish();
            } else {
                // Invalid QR code format, show error and continue scanning
                showToast("Invalid inventory QR code format. Please scan a valid inventory QR code.");
                
                // Resume scanning after a short delay
                barcodeView.postDelayed(() -> {
                    if (!isFinishing()) {
                        startScanning();
                    }
                }, 2000);
            }
            
        } catch (Exception e) {
            handleScanError("Error processing QR code: " + e.getMessage());
        }
    }

    private boolean isValidInventoryQR(String qrContent) {
        if (qrContent == null || qrContent.trim().isEmpty()) {
            return false;
        }
        
        try {
            // Expected QR format: "INVENTORY:item_name:quantity:operation"
            // Example: "INVENTORY:Rice:50:ADD" or "INVENTORY:Water:25:REMOVE"
            String[] parts = qrContent.split(":");
            
            if (parts.length != 4) {
                return false;
            }
            
            String prefix = parts[0];
            String itemName = parts[1];
            String quantityStr = parts[2];
            String operation = parts[3];
            
            // Validate prefix
            if (!"INVENTORY".equals(prefix)) {
                return false;
            }
            
            // Validate item name
            if (itemName == null || itemName.trim().isEmpty()) {
                return false;
            }
            
            // Validate quantity
            try {
                int quantity = Integer.parseInt(quantityStr);
                if (quantity <= 0) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
            
            // Validate operation
            if (!"ADD".equals(operation) && !"REMOVE".equals(operation)) {
                return false;
            }
            
            return true;
            
        } catch (Exception e) {
            return false;
        }
    }

    private void handleCameraPermissionDenied() {
        showToast("Camera permission is required to scan QR codes");
        
        // Show explanation dialog
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Camera Permission Required")
                .setMessage("This app needs camera access to scan QR codes for inventory updates. " +
                           "Please grant camera permission in app settings.")
                .setPositiveButton("Settings", (dialog, which) -> {
                    // Open app settings
                    Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.setData(android.net.Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                    finish();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    private void handleScanError(String errorMessage) {
        showToast(errorMessage);
        
        // Show error dialog with retry option
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Scan Error")
                .setMessage(errorMessage + "\n\nWould you like to try again?")
                .setPositiveButton("Retry", (dialog, which) -> {
                    startScanning();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    finish();
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) 
                == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScanning();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_CANCELED);
        super.onBackPressed();
    }

    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}