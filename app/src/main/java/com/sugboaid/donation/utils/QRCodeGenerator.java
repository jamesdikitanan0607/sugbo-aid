package com.sugboaid.donation.utils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

/**
 * Utility class for generating QR codes for inventory operations
 * This can be used for testing and creating sample QR codes
 */
public class QRCodeGenerator {
    
    private static final int DEFAULT_QR_SIZE = 512;
    private static final int QR_COLOR_BLACK = Color.BLACK;
    private static final int QR_COLOR_WHITE = Color.WHITE;
    
    /**
     * Generate QR code bitmap for inventory operations
     * 
     * @param itemName Name of the inventory item
     * @param quantity Quantity to add or remove
     * @param operation Operation type (ADD or REMOVE)
     * @return Bitmap of the generated QR code
     * @throws WriterException if QR code generation fails
     */
    public static Bitmap generateInventoryQRBitmap(String itemName, int quantity, String operation) 
            throws WriterException {
        return generateInventoryQRBitmap(itemName, quantity, operation, DEFAULT_QR_SIZE);
    }
    
    /**
     * Generate QR code bitmap for inventory operations with custom size
     * 
     * @param itemName Name of the inventory item
     * @param quantity Quantity to add or remove
     * @param operation Operation type (ADD or REMOVE)
     * @param size Size of the QR code (width and height in pixels)
     * @return Bitmap of the generated QR code
     * @throws WriterException if QR code generation fails
     */
    public static Bitmap generateInventoryQRBitmap(String itemName, int quantity, String operation, int size) 
            throws WriterException {
        
        // Generate the QR content using QRCodeUtils
        String qrContent = QRCodeUtils.generateInventoryQR(itemName, quantity, operation);
        
        // Create QR code writer
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        
        // Generate bit matrix
        BitMatrix bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, size, size);
        
        // Convert bit matrix to bitmap
        return bitMatrixToBitmap(bitMatrix);
    }
    
    /**
     * Convert BitMatrix to Bitmap
     * 
     * @param bitMatrix The bit matrix from QR code generation
     * @return Bitmap representation of the QR code
     */
    private static Bitmap bitMatrixToBitmap(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? QR_COLOR_BLACK : QR_COLOR_WHITE);
            }
        }
        
        return bitmap;
    }
    
    /**
     * Generate sample QR codes for testing purposes
     * 
     * @return Array of sample QR code contents
     */
    public static String[] generateSampleQRCodes() {
        return new String[] {
            QRCodeUtils.generateInventoryQR("Rice", 50, "ADD"),
            QRCodeUtils.generateInventoryQR("Water", 25, "REMOVE"),
            QRCodeUtils.generateInventoryQR("Medicine", 10, "ADD"),
            QRCodeUtils.generateInventoryQR("Clothes", 15, "REMOVE"),
            QRCodeUtils.generateInventoryQR("Canned Goods", 30, "ADD")
        };
    }
    
    /**
     * Get sample QR code descriptions for testing
     * 
     * @return Array of sample QR code descriptions
     */
    public static String[] getSampleQRDescriptions() {
        return new String[] {
            "Add 50 units of Rice",
            "Remove 25 units of Water", 
            "Add 10 units of Medicine",
            "Remove 15 units of Clothes",
            "Add 30 units of Canned Goods"
        };
    }
}