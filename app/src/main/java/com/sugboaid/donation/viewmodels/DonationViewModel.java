package com.sugboaid.donation.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.sugboaid.models.Donation;
import com.sugboaid.repositories.DonationRepository;

/**
 * ViewModel for handling donation-related operations
 * Manages donation submission, validation, and state
 */
public class DonationViewModel extends AndroidViewModel {

    private final DonationRepository donationRepository;
    
    // LiveData for UI state
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<DonationSubmissionResult> donationSubmissionResult = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public DonationViewModel(@NonNull Application application) {
        super(application);
        donationRepository = DonationRepository.getInstance(application);
    }

    /**
     * Submit a new donation
     * @param donation The donation to submit
     */
    public void submitDonation(Donation donation) {
        if (donation == null) {
            donationSubmissionResult.setValue(
                new DonationSubmissionResult(false, "Invalid donation data", null)
            );
            return;
        }

        // Validate donation
        if (!validateDonation(donation)) {
            return;
        }

        isLoading.setValue(true);

        // Simulate async operation (in real app, this might be a network call)
        new Thread(() -> {
            try {
                // Add donation to repository
                donationRepository.addDonation(donation);
                
                // Simulate processing time
                Thread.sleep(1000);
                
                // Post success result on main thread
                donationSubmissionResult.postValue(
                    new DonationSubmissionResult(true, "Donation recorded successfully", donation)
                );
                
            } catch (Exception e) {
                // Post error result on main thread
                donationSubmissionResult.postValue(
                    new DonationSubmissionResult(false, "Failed to record donation: " + e.getMessage(), null)
                );
            } finally {
                isLoading.postValue(false);
            }
        }).start();
    }

    /**
     * Validate donation data
     * @param donation The donation to validate
     * @return true if valid, false otherwise
     */
    private boolean validateDonation(Donation donation) {
        if (donation.getDonorName() == null || donation.getDonorName().trim().isEmpty()) {
            donation.setDonorName("Anonymous");
        }

        if (donation.getType() == null) {
            donationSubmissionResult.setValue(
                new DonationSubmissionResult(false, "Donation type is required", null)
            );
            return false;
        }

        if (donation.getAmount() <= 0) {
            donationSubmissionResult.setValue(
                new DonationSubmissionResult(false, "Donation amount must be greater than zero", null)
            );
            return false;
        }

        return true;
    }

    /**
     * Clear the submission result
     */
    public void clearSubmissionResult() {
        donationSubmissionResult.setValue(null);
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<DonationSubmissionResult> getDonationSubmissionResult() {
        return donationSubmissionResult;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    /**
     * Result class for donation submission
     */
    public static class DonationSubmissionResult {
        private final boolean success;
        private final String message;
        private final Donation donation;

        public DonationSubmissionResult(boolean success, String message, Donation donation) {
            this.success = success;
            this.message = message;
            this.donation = donation;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getErrorMessage() {
            return message;
        }

        public String getMessage() {
            return message;
        }

        public Donation getDonation() {
            return donation;
        }
    }
}