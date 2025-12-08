package com.sugboaid.donation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sugboaid.models.User;
import com.sugboaid.repositories.DonationRepository;
import com.sugboaid.repositories.UserRepository;
import com.sugboaid.utils.SharedPreferencesHelper;

import java.util.List;

public class AdminViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final DonationRepository donationRepository;

    private LiveData<Integer> totalUsers;
    private LiveData<Integer> activeUsers;
    private LiveData<Double> systemTotalDonations;
    private LiveData<Integer> pendingApprovals;
    private LiveData<List<User>> userList;

    private final MutableLiveData<Boolean> isAdmin;
    private final MutableLiveData<String> errorMessage;

    public AdminViewModel(@NonNull Application application) {
        super(application);
        userRepository = UserRepository.getInstance(application);
        donationRepository = DonationRepository.getInstance(application);
        isAdmin = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();

        checkAdminStatus();
        initSystemStats();
    }

    private void checkAdminStatus() {
        try {
            String role = SharedPreferencesHelper.getInstance(getApplication()).getUserRole();
            isAdmin.setValue(User.ROLE_ADMIN.equals(role));
        } catch (Exception e) {
            isAdmin.setValue(false);
        }
    }

    private void initSystemStats() {
        totalUsers = userRepository.getTotalUserCount();
        activeUsers = userRepository.getActiveUserCount();
        systemTotalDonations = donationRepository.getTotalDonations();
        pendingApprovals = donationRepository.getPendingApprovalCount();
        userList = userRepository.getAllUsersLive();
    }

    public void approveDonation(String donationId) {
        try {
            donationRepository.approveDonation(donationId);
        } catch (Exception ignored) { }
    }

    public void updateUserRole(String userId, String newRole) {
        try {
            userRepository.updateUserRole(userId, newRole);
        } catch (Exception ignored) { }
    }

    public LiveData<Boolean> isAdmin() { return isAdmin; }
    public LiveData<Integer> getTotalUsers() { return totalUsers; }
    public LiveData<Integer> getActiveUsers() { return activeUsers; }
    public LiveData<Double> getSystemTotalDonations() { return systemTotalDonations; }
    public LiveData<Integer> getPendingApprovals() { return pendingApprovals; }
    public LiveData<List<User>> getUserList() { return userList; }
    public LiveData<String> getErrorMessage() { return errorMessage; }

    public void refreshData() {
        checkAdminStatus();
        initSystemStats();
    }
}
