package com.sugboaid.app.util;

import android.content.Context;
import android.os.Environment;
import com.google.gson.Gson;
import com.sugboaid.app.data.SharedPrefHelper;
import com.sugboaid.app.data.repository.DataRepository;
import com.sugboaid.app.data.model.Donation;
import com.sugboaid.app.data.model.InventoryItem;
import com.sugboaid.app.data.model.User;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DataRecoveryManager {
    private Context context;
    private DataRepository repository;
    private SharedPrefHelper prefHelper;
    private Gson gson;

    public DataRecoveryManager(Context context) {
        this.context = context;
        this.repository = DataRepository.getInstance(context);
        this.prefHelper = new SharedPrefHelper(context);
        this.gson = new Gson();
    }

    public boolean createBackup() {
        try {
            // Create backup data structure
            Map<String, Object> backupData = new HashMap<>();
            backupData.put("users", repository.getUsers());
            backupData.put("donations", repository.getDonations());
            backupData.put("inventory", repository.getInventoryItems());
            backupData.put("timestamp", System.currentTimeMillis());
            backupData.put("version", "1.0");

            // Create backup file
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
                    .format(new Date());
            String fileName = "sugboaid_backup_" + timestamp + ".json";
            
            File backupDir = new File(context.getExternalFilesDir(null), Constants.BACKUP_FOLDER);
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            
            File backupFile = new File(backupDir, fileName);
            
            try (FileWriter writer = new FileWriter(backupFile)) {
                gson.toJson(backupData, writer);
                return true;
            }
            
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean restoreFromBackup(File backupFile) {
        try {
            try (FileReader reader = new FileReader(backupFile)) {
                Map<String, Object> backupData = gson.fromJson(reader, Map.class);
                
                if (backupData == null) {
                    return false;
                }
                
                // Validate backup data
                if (!backupData.containsKey("version") || !backupData.containsKey("timestamp")) {
                    return false;
                }
                
                // Clear existing data
                repository.clearAllData();
                
                // Restore users
                if (backupData.containsKey("users")) {
                    List<User> users = gson.fromJson(gson.toJson(backupData.get("users")), 
                            com.google.gson.reflect.TypeToken.getParameterized(List.class, User.class).getType());
                    for (User user : users) {
                        repository.saveUser(user);
                    }
                }
                
                // Restore donations
                if (backupData.containsKey("donations")) {
                    List<Donation> donations = gson.fromJson(gson.toJson(backupData.get("donations")), 
                            com.google.gson.reflect.TypeToken.getParameterized(List.class, Donation.class).getType());
                    for (Donation donation : donations) {
                        repository.saveDonation(donation);
                    }
                }
                
                // Restore inventory
                if (backupData.containsKey("inventory")) {
                    List<InventoryItem> items = gson.fromJson(gson.toJson(backupData.get("inventory")), 
                            com.google.gson.reflect.TypeToken.getParameterized(List.class, InventoryItem.class).getType());
                    for (InventoryItem item : items) {
                        repository.saveInventoryItem(item);
                    }
                }
                
                return true;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public File[] getAvailableBackups() {
        File backupDir = new File(context.getExternalFilesDir(null), Constants.BACKUP_FOLDER);
        if (!backupDir.exists()) {
            return new File[0];
        }
        
        File[] backupFiles = backupDir.listFiles((dir, name) -> 
                name.startsWith("sugboaid_backup_") && name.endsWith(".json"));
        
        return backupFiles != null ? backupFiles : new File[0];
    }

    public boolean deleteBackup(File backupFile) {
        return backupFile.exists() && backupFile.delete();
    }

    public long getBackupSize(File backupFile) {
        return backupFile.exists() ? backupFile.length() : 0;
    }

    public Date getBackupDate(File backupFile) {
        String fileName = backupFile.getName();
        // Extract timestamp from filename: sugboaid_backup_yyyyMMdd_HHmmss.json
        if (fileName.startsWith("sugboaid_backup_") && fileName.endsWith(".json")) {
            String timestamp = fileName.substring(16, fileName.length() - 5); // Remove prefix and .json
            try {
                SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                return format.parse(timestamp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new Date(backupFile.lastModified());
    }

    public boolean isBackupValid(File backupFile) {
        try {
            try (FileReader reader = new FileReader(backupFile)) {
                Map<String, Object> backupData = gson.fromJson(reader, Map.class);
                return backupData != null && 
                       backupData.containsKey("version") && 
                       backupData.containsKey("timestamp");
            }
        } catch (Exception e) {
            return false;
        }
    }

    public void scheduleAutoBackup() {
        // Check if auto backup is enabled and create backup if needed
        boolean autoBackupEnabled = prefHelper.getBoolean("auto_backup_enabled", true);
        long lastBackupTime = prefHelper.getLong("last_backup_time", 0);
        long currentTime = System.currentTimeMillis();
        long backupInterval = 7 * 24 * 60 * 60 * 1000L; // 7 days in milliseconds
        
        if (autoBackupEnabled && (currentTime - lastBackupTime) > backupInterval) {
            if (createBackup()) {
                prefHelper.saveLong("last_backup_time", currentTime);
            }
        }
    }

    public void cleanOldBackups(int maxBackups) {
        File[] backups = getAvailableBackups();
        if (backups.length > maxBackups) {
            // Sort by date (oldest first)
            java.util.Arrays.sort(backups, (f1, f2) -> 
                    Long.compare(f1.lastModified(), f2.lastModified()));
            
            // Delete oldest backups
            for (int i = 0; i < backups.length - maxBackups; i++) {
                deleteBackup(backups[i]);
            }
        }
    }
}