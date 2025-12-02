package com.sugboaid.app.util;

import android.content.Context;
import com.sugboaid.app.data.repository.DataRepository;
import com.sugboaid.app.data.model.User;
import com.sugboaid.app.data.model.Donation;
import com.sugboaid.app.data.model.DonationItem;
import com.sugboaid.app.data.model.InventoryItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SampleDataGenerator {
    
    public static void generateSampleData(Context context) {
        DataRepository repository = DataRepository.getInstance(context);
        
        // Check if data already exists
        if (!repository.getUsers().isEmpty()) {
            return; // Data already generated
        }
        
        generateSampleUsers(repository);
        generateSampleDonations(repository);
        generateSampleInventory(repository);
    }
    
    private static void generateSampleUsers(DataRepository repository) {
        // Sample users for different roles
        User donor = new User("user1", "Maria Santos", "maria@example.com", Constants.ROLE_DONOR);
        User organization = new User("user2", "Cebu Relief Org", "org@ceburelief.org", Constants.ROLE_ORGANIZATION);
        User volunteer = new User("user3", "Juan Dela Cruz", "juan@volunteer.org", Constants.ROLE_VOLUNTEER);
        User recipient = new User("user4", "Ana Garcia", "ana@recipient.com", Constants.ROLE_RECIPIENT);
        
        repository.saveUser(donor);
        repository.saveUser(organization);
        repository.saveUser(volunteer);
        repository.saveUser(recipient);
    }
    
    private static void generateSampleDonations(DataRepository repository) {
        // Sample donations
        Donation donation1 = new Donation();
        donation1.setId(UUID.randomUUID().toString());
        donation1.setDonorName("Maria Santos");
        donation1.setDonorId("user1");
        donation1.setOrganizationName("Cebu Relief Org");
        donation1.setOrganizationId("user2");
        donation1.setAmount(5000.0);
        donation1.setType(Constants.DONATION_TYPE_CASH);
        donation1.setCategory(Constants.CATEGORY_EMERGENCY);
        donation1.setStatus(Constants.STATUS_CONFIRMED);
        donation1.setReceiptId("RCP-" + System.currentTimeMillis());
        donation1.setTimestamp(System.currentTimeMillis() - 86400000); // Yesterday
        
        Donation donation2 = new Donation();
        donation2.setId(UUID.randomUUID().toString());
        donation2.setDonorName("Anonymous Donor");
        donation2.setOrganizationName("Cebu Relief Org");
        donation2.setOrganizationId("user2");
        donation2.setAmount(2500.0);
        donation2.setType(Constants.DONATION_TYPE_GOODS);
        donation2.setCategory(Constants.CATEGORY_FOOD);
        donation2.setStatus(Constants.STATUS_CONFIRMED);
        donation2.setReceiptId("RCP-" + (System.currentTimeMillis() - 1000));
        donation2.setTimestamp(System.currentTimeMillis() - 43200000); // 12 hours ago
        
        // Add donation items to goods donation
        List<DonationItem> items = new ArrayList<>();
        items.add(new DonationItem("Rice", Constants.CATEGORY_FOOD, 50, "kg", 45.0));
        items.add(new DonationItem("Canned Goods", Constants.CATEGORY_FOOD, 100, "cans", 25.0));
        donation2.setItems(items);
        
        repository.saveDonation(donation1);
        repository.saveDonation(donation2);
    }
    
    private static void generateSampleInventory(DataRepository repository) {
        // Sample inventory items
        InventoryItem rice = new InventoryItem("Rice", Constants.CATEGORY_FOOD, 500, "kg");
        rice.setId(UUID.randomUUID().toString());
        rice.setMinimumStock(100);
        rice.setMaximumStock(1000);
        rice.setUnitValue(45.0);
        rice.setLocation("Warehouse A");
        rice.setCondition(Constants.CONDITION_GOOD);
        rice.setUpdatedBy("System");
        
        InventoryItem water = new InventoryItem("Bottled Water", Constants.CATEGORY_EMERGENCY, 200, "bottles");
        water.setId(UUID.randomUUID().toString());
        water.setMinimumStock(500);
        water.setMaximumStock(2000);
        water.setUnitValue(15.0);
        water.setLocation("Warehouse A");
        water.setCondition(Constants.CONDITION_NEW);
        water.setUpdatedBy("System");
        
        InventoryItem blankets = new InventoryItem("Emergency Blankets", Constants.CATEGORY_SHELTER, 75, "pieces");
        blankets.setId(UUID.randomUUID().toString());
        blankets.setMinimumStock(50);
        blankets.setMaximumStock(200);
        blankets.setUnitValue(150.0);
        blankets.setLocation("Warehouse B");
        blankets.setCondition(Constants.CONDITION_NEW);
        blankets.setUpdatedBy("System");
        
        InventoryItem medicine = new InventoryItem("First Aid Kits", Constants.CATEGORY_MEDICAL, 25, "kits");
        medicine.setId(UUID.randomUUID().toString());
        medicine.setMinimumStock(30);
        medicine.setMaximumStock(100);
        medicine.setUnitValue(250.0);
        medicine.setLocation("Medical Storage");
        medicine.setCondition(Constants.CONDITION_NEW);
        medicine.setUpdatedBy("System");
        
        repository.saveInventoryItem(rice);
        repository.saveInventoryItem(water);
        repository.saveInventoryItem(blankets);
        repository.saveInventoryItem(medicine);
    }
}