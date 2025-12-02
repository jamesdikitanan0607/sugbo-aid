package com.sugboaid.app.manager;

import android.content.Context;
import com.sugboaid.app.data.repository.DataRepository;
import com.sugboaid.app.data.model.Donation;
import com.sugboaid.app.data.model.DonationItem;
import com.sugboaid.app.data.model.User;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class POSManager {
    private DataRepository repository;
    private AuthManager authManager;
    private QRCodeManager qrCodeManager;
    private static POSManager instance;

    private POSManager(Context context) {
        repository = DataRepository.getInstance(context);
        authManager = AuthManager.getInstance(context);
        qrCodeManager = QRCodeManager.getInstance(context);
    }

    public static synchronized POSManager getInstance(Context context) {
        if (instance == null) {
            instance = new POSManager(context.getApplicationContext());
        }
        return instance;
    }

    public Donation createDonation(String donorName, String donorEmail, String type, String category) {
        Donation donation = new Donation();
        donation.setId(UUID.randomUUID().toString());
        donation.setDonorName(donorName);
        donation.setType(type);
        donation.setCategory(category);
        donation.setStatus("PENDING");
        
        User currentUser = authManager.getCurrentUser();
        if (currentUser != null) {
            donation.setOrganizationId(currentUser.getId());
            donation.setOrganizationName(currentUser.getName());
        }
        
        // Create receipt ID
        donation.setReceiptId("RCP-" + System.currentTimeMillis());
        
        return donation;
    }

    public void addItemToDonation(Donation donation, String itemName, String category, 
                                 int quantity, String unit, double unitValue) {
        if (donation.getItems() == null) {
            donation.setItems(new ArrayList<>());
        }
        
        DonationItem item = new DonationItem(itemName, category, quantity, unit, unitValue);
        item.setId(UUID.randomUUID().toString());
        donation.getItems().add(item);
        
        // Update total amount
        updateDonationTotal(donation);
    }

    public void removeItemFromDonation(Donation donation, String itemId) {
        if (donation.getItems() != null) {
            donation.getItems().removeIf(item -> item.getId().equals(itemId));
            updateDonationTotal(donation);
        }
    }

    public void updateDonationItem(Donation donation, String itemId, int newQuantity, double newUnitValue) {
        if (donation.getItems() != null) {
            for (DonationItem item : donation.getItems()) {
                if (item.getId().equals(itemId)) {
                    item.setQuantity(newQuantity);
                    item.setUnitValue(newUnitValue);
                    break;
                }
            }
            updateDonationTotal(donation);
        }
    }

    private void updateDonationTotal(Donation donation) {
        double total = 0.0;
        if (donation.getItems() != null) {
            for (DonationItem item : donation.getItems()) {
                total += item.getTotalValue();
            }
        }
        donation.setAmount(total);
    }

    public Donation processDonation(Donation donation) {
        // Generate QR code for the donation
        String qrData = generateQRData(donation);
        donation.setQrCode(qrData);
        
        // Set status to confirmed
        donation.setStatus("CONFIRMED");
        donation.setTimestamp(System.currentTimeMillis());
        
        // Save to repository
        repository.saveDonation(donation);
        
        return donation;
    }

    private String generateQRData(Donation donation) {
        StringBuilder qrData = new StringBuilder();
        qrData.append("SUGBOAID_DONATION\n");
        qrData.append("ID: ").append(donation.getId()).append("\n");
        qrData.append("Receipt: ").append(donation.getReceiptId()).append("\n");
        qrData.append("Donor: ").append(donation.getDonorName()).append("\n");
        qrData.append("Amount: ").append(donation.getCurrency()).append(" ").append(donation.getAmount()).append("\n");
        qrData.append("Type: ").append(donation.getType()).append("\n");
        qrData.append("Date: ").append(new java.util.Date(donation.getTimestamp()).toString()).append("\n");
        qrData.append("Organization: ").append(donation.getOrganizationName()).append("\n");
        
        return qrData.toString();
    }

    public List<Donation> getDonationHistory() {
        User currentUser = authManager.getCurrentUser();
        if (currentUser == null) return new ArrayList<>();
        
        if (currentUser.getRole().equals("DONOR")) {
            return repository.getDonationsByDonor(currentUser.getId());
        } else {
            return repository.getDonations();
        }
    }

    public Donation getDonationById(String id) {
        return repository.getDonationById(id);
    }

    public void updateDonationStatus(String donationId, String newStatus) {
        Donation donation = repository.getDonationById(donationId);
        if (donation != null) {
            donation.setStatus(newStatus);
            repository.saveDonation(donation);
        }
    }

    public double getTotalDonationsAmount() {
        List<Donation> donations = repository.getDonations();
        double total = 0.0;
        for (Donation donation : donations) {
            if ("CONFIRMED".equals(donation.getStatus())) {
                total += donation.getAmount();
            }
        }
        return total;
    }

    public int getTotalDonationsCount() {
        List<Donation> donations = repository.getDonations();
        int count = 0;
        for (Donation donation : donations) {
            if ("CONFIRMED".equals(donation.getStatus())) {
                count++;
            }
        }
        return count;
    }
}