package com.sugboaid.app.data.repository;

import android.content.Context;
import com.google.gson.reflect.TypeToken;
import com.sugboaid.app.data.SharedPrefHelper;
import com.sugboaid.app.data.model.Donation;
import com.sugboaid.app.data.model.InventoryItem;
import com.sugboaid.app.data.model.User;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataRepository {
    private static final String KEY_USERS = "users";
    private static final String KEY_DONATIONS = "donations";
    private static final String KEY_INVENTORY = "inventory";
    private static final String KEY_CURRENT_USER = "current_user";

    private SharedPrefHelper prefHelper;
    private static DataRepository instance;

    private DataRepository(Context context) {
        prefHelper = new SharedPrefHelper(context);
    }

    public static synchronized DataRepository getInstance(Context context) {
        if (instance == null) {
            instance = new DataRepository(context.getApplicationContext());
        }
        return instance;
    }

    // User operations
    public void saveUser(User user) {
        List<User> users = getUsers();
        boolean updated = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(user.getId())) {
                users.set(i, user);
                updated = true;
                break;
            }
        }
        if (!updated) {
            if (user.getId() == null) {
                user.setId(UUID.randomUUID().toString());
            }
            users.add(user);
        }
        Type type = new TypeToken<List<User>>(){}.getType();
        prefHelper.saveList(KEY_USERS, users);
    }

    public List<User> getUsers() {
        Type type = new TypeToken<List<User>>(){}.getType();
        return prefHelper.getList(KEY_USERS, type);
    }

    public User getUserById(String id) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getId().equals(id)) {
                return user;
            }
        }
        return null;
    }

    public User getUserByEmail(String email) {
        List<User> users = getUsers();
        for (User user : users) {
            if (user.getEmail().equals(email)) {
                return user;
            }
        }
        return null;
    }

    public void setCurrentUser(User user) {
        prefHelper.saveObject(KEY_CURRENT_USER, user);
    }

    public User getCurrentUser() {
        return prefHelper.getObject(KEY_CURRENT_USER, User.class);
    }

    public void clearCurrentUser() {
        prefHelper.remove(KEY_CURRENT_USER);
    }

    // Donation operations
    public void saveDonation(Donation donation) {
        List<Donation> donations = getDonations();
        boolean updated = false;
        for (int i = 0; i < donations.size(); i++) {
            if (donations.get(i).getId().equals(donation.getId())) {
                donations.set(i, donation);
                updated = true;
                break;
            }
        }
        if (!updated) {
            if (donation.getId() == null) {
                donation.setId(UUID.randomUUID().toString());
            }
            donations.add(donation);
        }
        prefHelper.saveList(KEY_DONATIONS, donations);
    }

    public List<Donation> getDonations() {
        Type type = new TypeToken<List<Donation>>(){}.getType();
        return prefHelper.getList(KEY_DONATIONS, type);
    }

    public Donation getDonationById(String id) {
        List<Donation> donations = getDonations();
        for (Donation donation : donations) {
            if (donation.getId().equals(id)) {
                return donation;
            }
        }
        return null;
    }

    public List<Donation> getDonationsByDonor(String donorId) {
        List<Donation> donations = getDonations();
        List<Donation> result = new ArrayList<>();
        for (Donation donation : donations) {
            if (donation.getDonorId().equals(donorId)) {
                result.add(donation);
            }
        }
        return result;
    }

    // Inventory operations
    public void saveInventoryItem(InventoryItem item) {
        List<InventoryItem> items = getInventoryItems();
        boolean updated = false;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                updated = true;
                break;
            }
        }
        if (!updated) {
            if (item.getId() == null) {
                item.setId(UUID.randomUUID().toString());
            }
            items.add(item);
        }
        prefHelper.saveList(KEY_INVENTORY, items);
    }

    public List<InventoryItem> getInventoryItems() {
        Type type = new TypeToken<List<InventoryItem>>(){}.getType();
        return prefHelper.getList(KEY_INVENTORY, type);
    }

    public InventoryItem getInventoryItemById(String id) {
        List<InventoryItem> items = getInventoryItems();
        for (InventoryItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    public List<InventoryItem> getLowStockItems() {
        List<InventoryItem> items = getInventoryItems();
        List<InventoryItem> result = new ArrayList<>();
        for (InventoryItem item : items) {
            if (item.isLowStock()) {
                result.add(item);
            }
        }
        return result;
    }

    // Clear all data
    public void clearAllData() {
        prefHelper.clear();
    }
}