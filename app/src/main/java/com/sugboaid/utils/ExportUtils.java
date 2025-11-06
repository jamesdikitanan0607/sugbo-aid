package com.sugboaid.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import androidx.core.content.FileProvider;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.sugboaid.models.Transaction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Utility class for exporting reports to PDF and CSV formats
 */
public class ExportUtils {
    
    private static final String AUTHORITY = "com.sugboaid.donation.fileprovider";
    private static final String EXPORT_FOLDER = "SugboAid_Reports";
    
    /**
     * Export transactions to PDF format
     */
    public static Intent exportToPDF(Context context, List<Transaction> transactions, String filterType) throws Exception {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("No transactions to export");
        }
        
        // Create export directory
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_FOLDER);
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            throw new IOException("Failed to create export directory");
        }
        
        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("SugboAid_Report_%s_%s.pdf", filterType, timestamp);
        File pdfFile = new File(exportDir, filename);
        
        try {
            // Create PDF document
            PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile));
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);
            
            // Add title
            document.add(new Paragraph("SugboAid Transaction Report")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(18)
                    .setBold());
            
            // Add report info
            document.add(new Paragraph(String.format("Report Type: %s", getFilterDisplayName(filterType)))
                    .setFontSize(12));
            document.add(new Paragraph(String.format("Generated: %s", 
                    new SimpleDateFormat("MMMM dd, yyyy 'at' HH:mm", Locale.getDefault()).format(new Date())))
                    .setFontSize(12));
            document.add(new Paragraph(String.format("Total Transactions: %d", transactions.size()))
                    .setFontSize(12));
            
            // Calculate total value
            double totalValue = 0.0;
            for (Transaction transaction : transactions) {
                String amount = transaction.getAmount();
                if (amount != null && amount.contains("₱")) {
                    try {
                        String numericAmount = amount.replace("₱", "").replace(",", "").trim();
                        totalValue += Double.parseDouble(numericAmount);
                    } catch (NumberFormatException e) {
                        // Skip invalid amounts
                    }
                }
            }
            
            document.add(new Paragraph(String.format("Total Value: ₱%.2f", totalValue))
                    .setFontSize(12));
            document.add(new Paragraph("\n"));
            
            // Create table
            Table table = new Table(new float[]{2, 3, 2, 2, 3, 2, 2});
            table.setWidth(100);
            
            // Add headers
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Donor").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Type").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Amount").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Date").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Campaign").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Status").setBold()));
            
            // Add data rows
            for (Transaction transaction : transactions) {
                table.addCell(new Cell().add(new Paragraph(transaction.getId() != null ? transaction.getId() : "")));
                table.addCell(new Cell().add(new Paragraph(transaction.getDonor() != null ? transaction.getDonor() : "Anonymous")));
                table.addCell(new Cell().add(new Paragraph(getTransactionTypeText(transaction.getType()))));
                table.addCell(new Cell().add(new Paragraph(transaction.getAmount() != null ? transaction.getAmount() : "")));
                table.addCell(new Cell().add(new Paragraph(transaction.getFormattedDate() != null ? transaction.getFormattedDate() : "")));
                table.addCell(new Cell().add(new Paragraph(transaction.getCampaign() != null ? transaction.getCampaign() : "General")));
                table.addCell(new Cell().add(new Paragraph(transaction.isVerified() ? "Verified" : "Pending")));
            }
            
            document.add(table);
            
            // Add footer
            document.add(new Paragraph("\n"));
            document.add(new Paragraph("Generated by SugboAid Donation Management System")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));
            document.add(new Paragraph("For more information, visit sugboaid.org")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setFontSize(10));
            
            document.close();
            
            // Create sharing intent
            Uri fileUri = FileProvider.getUriForFile(context, AUTHORITY, pdfFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/pdf");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SugboAid Transaction Report - " + filterType);
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("SugboAid transaction report (%s) with %d transactions.", 
                    getFilterDisplayName(filterType), transactions.size()));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            return Intent.createChooser(shareIntent, "Export PDF Report");
            
        } catch (Exception e) {
            // Clean up file if creation failed
            if (pdfFile.exists()) {
                pdfFile.delete();
            }
            throw new Exception("Failed to create PDF: " + e.getMessage(), e);
        }
    }
    
    /**
     * Export transactions to CSV format
     */
    public static Intent exportToCSV(Context context, List<Transaction> transactions, String filterType) throws Exception {
        if (transactions == null || transactions.isEmpty()) {
            throw new IllegalArgumentException("No transactions to export");
        }
        
        // Create export directory
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_FOLDER);
        if (!exportDir.exists() && !exportDir.mkdirs()) {
            throw new IOException("Failed to create export directory");
        }
        
        // Generate filename with timestamp
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String filename = String.format("SugboAid_Report_%s_%s.csv", filterType, timestamp);
        File csvFile = new File(exportDir, filename);
        
        try {
            FileOutputStream fos = new FileOutputStream(csvFile);
            
            // Write CSV header
            StringBuilder csv = new StringBuilder();
            csv.append("ID,Donor,Type,Amount,Date,Campaign,Verified,Description,Receipt ID\n");
            
            // Write data rows
            for (Transaction transaction : transactions) {
                csv.append(escapeCSV(transaction.getId())).append(",");
                csv.append(escapeCSV(transaction.getDonor())).append(",");
                csv.append(escapeCSV(getTransactionTypeText(transaction.getType()))).append(",");
                csv.append(escapeCSV(transaction.getAmount())).append(",");
                csv.append(escapeCSV(transaction.getFormattedDate())).append(",");
                csv.append(escapeCSV(transaction.getCampaign())).append(",");
                csv.append(transaction.isVerified() ? "Yes" : "No").append(",");
                csv.append(escapeCSV(transaction.getDescription())).append(",");
                csv.append(escapeCSV(transaction.getReceiptId())).append("\n");
            }
            
            fos.write(csv.toString().getBytes());
            fos.close();
            
            // Create sharing intent
            Uri fileUri = FileProvider.getUriForFile(context, AUTHORITY, csvFile);
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/csv");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "SugboAid Transaction Report CSV - " + filterType);
            shareIntent.putExtra(Intent.EXTRA_TEXT, String.format("SugboAid transaction report (%s) with %d transactions in CSV format.", 
                    getFilterDisplayName(filterType), transactions.size()));
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            return Intent.createChooser(shareIntent, "Export CSV Report");
            
        } catch (Exception e) {
            // Clean up file if creation failed
            if (csvFile.exists()) {
                csvFile.delete();
            }
            throw new Exception("Failed to create CSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Get display name for filter type
     */
    private static String getFilterDisplayName(String filterType) {
        switch (filterType.toLowerCase()) {
            case "all":
                return "All Transactions";
            case "cash":
                return "Cash Donations";
            case "goods":
                return "Goods Donations";
            case "distribution":
                return "Distributions";
            default:
                return "Filtered Transactions";
        }
    }
    
    /**
     * Get transaction type display text
     */
    private static String getTransactionTypeText(com.sugboaid.models.TransactionType type) {
        switch (type) {
            case DONATION:
                return "Donation";
            case DISTRIBUTION:
                return "Distribution";
            case INVENTORY_UPDATE:
                return "Inventory Update";
            case TRANSFER:
                return "Transfer";
            default:
                return "Transaction";
        }
    }
    
    /**
     * Escape CSV values
     */
    private static String escapeCSV(String value) {
        if (value == null) return "";
        
        // Escape quotes and wrap in quotes if contains comma, quote, or newline
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        
        return value;
    }
    
    /**
     * Check if external storage is available for writing
     */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }
    
    /**
     * Get export directory path for user reference
     */
    public static String getExportDirectoryPath(Context context) {
        File exportDir = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), EXPORT_FOLDER);
        return exportDir.getAbsolutePath();
    }
}