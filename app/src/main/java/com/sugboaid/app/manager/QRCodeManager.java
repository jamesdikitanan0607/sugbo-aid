package com.sugboaid.app.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.util.HashMap;
import java.util.Map;

public class QRCodeManager {
    private static QRCodeManager instance;

    private QRCodeManager(Context context) {
        // Initialize if needed
    }

    public static synchronized QRCodeManager getInstance(Context context) {
        if (instance == null) {
            instance = new QRCodeManager(context.getApplicationContext());
        }
        return instance;
    }

    public Bitmap generateQRCode(String data, int width, int height) {
        try {
            QRCodeWriter writer = new QRCodeWriter();
            
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
            hints.put(EncodeHintType.MARGIN, 1);
            
            BitMatrix bitMatrix = writer.encode(data, BarcodeFormat.QR_CODE, width, height, hints);
            
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
            
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    bitmap.setPixel(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
                }
            }
            
            return bitmap;
            
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

    public Bitmap generateDonationReceiptQR(String donationId, String receiptId, 
                                          String donorName, double amount, String currency) {
        StringBuilder qrData = new StringBuilder();
        qrData.append("SUGBOAID_RECEIPT\n");
        qrData.append("Donation ID: ").append(donationId).append("\n");
        qrData.append("Receipt ID: ").append(receiptId).append("\n");
        qrData.append("Donor: ").append(donorName).append("\n");
        qrData.append("Amount: ").append(currency).append(" ").append(String.format("%.2f", amount)).append("\n");
        qrData.append("Timestamp: ").append(System.currentTimeMillis()).append("\n");
        qrData.append("Verify at: sugboaid.org/verify");
        
        return generateQRCode(qrData.toString(), 512, 512);
    }

    public Bitmap generateInventoryItemQR(String itemId, String itemName, String category, int stock) {
        StringBuilder qrData = new StringBuilder();
        qrData.append("SUGBOAID_INVENTORY\n");
        qrData.append("Item ID: ").append(itemId).append("\n");
        qrData.append("Name: ").append(itemName).append("\n");
        qrData.append("Category: ").append(category).append("\n");
        qrData.append("Stock: ").append(stock).append("\n");
        qrData.append("Scanned: ").append(System.currentTimeMillis());
        
        return generateQRCode(qrData.toString(), 256, 256);
    }

    public String parseQRData(String qrData) {
        // Simple QR data parser
        if (qrData.startsWith("SUGBOAID_")) {
            return qrData;
        }
        return null;
    }

    public boolean isValidSugboAidQR(String qrData) {
        return qrData != null && (
            qrData.startsWith("SUGBOAID_RECEIPT") ||
            qrData.startsWith("SUGBOAID_DONATION") ||
            qrData.startsWith("SUGBOAID_INVENTORY")
        );
    }

    public String extractDonationIdFromQR(String qrData) {
        if (qrData != null && qrData.contains("Donation ID: ")) {
            String[] lines = qrData.split("\n");
            for (String line : lines) {
                if (line.startsWith("Donation ID: ")) {
                    return line.substring("Donation ID: ".length());
                }
            }
        }
        return null;
    }

    public String extractReceiptIdFromQR(String qrData) {
        if (qrData != null && qrData.contains("Receipt ID: ")) {
            String[] lines = qrData.split("\n");
            for (String line : lines) {
                if (line.startsWith("Receipt ID: ")) {
                    return line.substring("Receipt ID: ".length());
                }
            }
        }
        return null;
    }

    public String extractItemIdFromQR(String qrData) {
        if (qrData != null && qrData.contains("Item ID: ")) {
            String[] lines = qrData.split("\n");
            for (String line : lines) {
                if (line.startsWith("Item ID: ")) {
                    return line.substring("Item ID: ".length());
                }
            }
        }
        return null;
    }
}