package com.sugboaid.auth.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.sugboaid.donation.R;
import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.donation.databinding.FragmentLoginBinding;
import com.sugboaid.donation.viewmodels.DashboardViewModel;
import com.sugboaid.donation.fragments.BaseFragment;
import com.sugboaid.viewmodels.AuthViewModel;
import com.sugboaid.utils.ValidationUtils;
import com.sugboaid.utils.AccessibilityUtils;
import com.sugboaid.utils.AuthErrorHandler;
import com.sugboaid.utils.AuthLoadingStateManager;
import com.sugboaid.utils.AuthFeedbackManager;

/**
 * LoginFragment for user authentication
 * Handles existing user login with form validation
 */
public class LoginFragment extends BaseFragment {

    private FragmentLoginBinding binding;
    private AuthViewModel authViewModel;
    private NavController navController;
    private AuthLoadingStateManager loadingStateManager;
    private AuthFeedbackManager feedbackManager;
    private boolean loginInitiated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Initialize NavController early so observeData() navigation is safe
        navController = Navigation.findNavController(view);

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Initialize managers
        loadingStateManager = new AuthLoadingStateManager(requireContext());
        feedbackManager = new AuthFeedbackManager(requireContext());

        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    protected void initViews(View view) {
        // Views are initialized through ViewBinding
        clearErrors();
    }

    @Override
    protected void setupListeners() {
        // Login button click listener
        binding.btnLogin.setOnClickListener(v -> {
            animateButtonPress(v);
            loginInitiated = true;
            validateAndLogin();
        });

        // Real-time validation listeners
        setupRealTimeValidation();

        // Text CTA: Don't have an account? Sign up
        try {
            if (binding.tvGoToSignup != null) {
                binding.tvGoToSignup.setOnClickListener(v -> {
                    try {
                        navController.navigate(R.id.action_loginFragment_to_signupFragment);
                    } catch (Exception e) {
                        showToast("Navigation error occurred");
                    }
                });
            }
        } catch (Exception ignored) {
        }
    }


    @Override
    protected void observeData() {
        // Observe authentication state
        authViewModel.authState.observe(getViewLifecycleOwner(), authState -> {
            if (authState != null && authState.isAuthenticated() && loginInitiated) {
                // Login successful, navigate directly to main dashboard
                navigateToDashboard();
            }
        });

        // Observe loading state
        authViewModel.isLoading.observe(getViewLifecycleOwner(), isLoading -> {
            showLoading(isLoading);
        });

        // Observe error messages (legacy support)
        authViewModel.errorMessage.observe(getViewLifecycleOwner(), errorMessage -> {
            if (errorMessage != null && !errorMessage.isEmpty()) {
                showError(errorMessage);
                authViewModel.clearErrorMessage();
            }
        });

        // Observe comprehensive error results
        authViewModel.errorResult.observe(getViewLifecycleOwner(), errorResult -> {
            if (errorResult != null) {
                handleErrorResult(errorResult);
                authViewModel.clearErrorResult();
            }
        });

        // Observe success messages
        authViewModel.successMessage.observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null && !successMessage.isEmpty()) {
                showSuccess(successMessage);
                authViewModel.clearSuccessMessage();
            }
        });
    }

    @Override
    protected void refreshData() {
        // Clear form and reset state
        clearForm();
        clearErrors();
    }

    /**
     * Set up real-time validation for form fields
     */
    private void setupRealTimeValidation() {
        // Email field validation
        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateEmailField();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Password field validation
        binding.etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validatePasswordField();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Validate email field in real-time with comprehensive validation
     */
    private void validateEmailField() {
        String email = binding.etEmail.getText().toString().trim();
        
        if (email.isEmpty()) {
            binding.tilEmail.setError(null); // Clear error for empty field during typing
            binding.tilEmail.setHelperText(null);
        } else {
            ValidationUtils.ValidationResult result = authViewModel.validateEmailDetailed(email);
            if (result.isValid()) {
                binding.tilEmail.setError(null);
                binding.tilEmail.setHelperText("Valid email address");
            } else {
                binding.tilEmail.setError(result.getMessage());
                binding.tilEmail.setHelperText(null);
            }
        }
        
        // Update accessibility
        updateFieldAccessibility(binding.tilEmail, "Email");
    }

    /**
     * Validate password field in real-time with comprehensive validation
     */
    private void validatePasswordField() {
        String password = binding.etPassword.getText().toString();
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(null); // Clear error for empty field during typing
            binding.tilPassword.setHelperText(null);
        } else {
            ValidationUtils.ValidationResult result = authViewModel.validatePasswordDetailed(password);
            if (result.isValid()) {
                binding.tilPassword.setError(null);
                binding.tilPassword.setHelperText("Password meets requirements");
            } else {
                binding.tilPassword.setError(result.getMessage());
                binding.tilPassword.setHelperText(null);
            }
        }
        
        // Update accessibility
        updateFieldAccessibility(binding.tilPassword, "Password");
    }

    /**
     * Validate form and attempt login using comprehensive validation
     */
    private void validateAndLogin() {
        // Validate form fields
        boolean isValid = true;
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        // Validate email
        ValidationUtils.ValidationResult emailResult = ValidationUtils.validateEmail(email);
        applyEmailValidation(emailResult);
        if (!emailResult.isValid()) {
            isValid = false;
        }

        // Validate password
        ValidationUtils.ValidationResult passwordResult = ValidationUtils.validatePassword(password);
        applyPasswordValidation(passwordResult);
        if (!passwordResult.isValid()) {
            isValid = false;
        }

        if (isValid) {
            // Attempt login
            authViewModel.login(email, password);
        } else {
            // Show validation errors using AccessibilityUtils
            if (!emailResult.isValid()) {
                binding.tilEmail.setError(emailResult.getMessage());
                AccessibilityUtils.announceForAccessibility(
                    binding.tilEmail, 
                    "Email error: " + emailResult.getMessage()
                );
            }
            if (!passwordResult.isValid()) {
                binding.tilPassword.setError(passwordResult.getMessage());
                AccessibilityUtils.announceForAccessibility(
                    binding.tilPassword, 
                    "Password error: " + passwordResult.getMessage()
                );
            }
        }
    }

    /**
     * Apply email validation result to UI
     */
    private void applyEmailValidation(ValidationUtils.ValidationResult result) {
        if (result != null) {
            if (result.isValid()) {
                binding.tilEmail.setError(null);
                binding.tilEmail.setHelperText("Valid email address");
            } else {
                binding.tilEmail.setError(result.getMessage());
                binding.tilEmail.setHelperText(null);
                animateError(binding.tilEmail);
            }
            updateFieldAccessibility(binding.tilEmail, "Email");
        }
    }

    /**
     * Apply password validation result to UI
     */
    private void applyPasswordValidation(ValidationUtils.ValidationResult result) {
        if (result != null) {
            if (result.isValid()) {
                binding.tilPassword.setError(null);
                binding.tilPassword.setHelperText("Password meets requirements");
            } else {
                binding.tilPassword.setError(result.getMessage());
                binding.tilPassword.setHelperText(null);
                animateError(binding.tilPassword);
            }
            updateFieldAccessibility(binding.tilPassword, "Password");
        }
    }

    /**
     * Navigate to dashboard after successful login
     */
    private void navigateToDashboard() {
        try {
            // Switch to the main graph and show the dashboard using MainActivity helper
            MainActivity activity = (MainActivity) requireActivity();
            activity.switchToMainGraphAndShowDashboard(false);
        } catch (Exception e) {
            showToast("Navigation error occurred");
        }
    }

    /**
     * Show loading state with comprehensive loading management
     */
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            loadingStateManager.showAuthLoading(
                binding.btnLogin,
                getString(R.string.logging_in),
                getString(R.string.login),
                binding.tilEmail,
                binding.tilPassword
            );
        } else {
            loadingStateManager.hideAuthLoading(
                binding.btnLogin,
                binding.tilEmail,
                binding.tilPassword
            );
        }
    }

    /**
     * Handle comprehensive error result
     */
    private void handleErrorResult(AuthErrorHandler.ErrorResult errorResult) {
        AuthErrorHandler.handleError(requireContext(), getView(), errorResult);
    }

    /**
     * Show error message (legacy support)
     */
    private void showError(String message) {
        feedbackManager.showError(getView(), message, null, null);
    }

    /**
     * Show success message with enhanced feedback
     */
    private void showSuccess(String message) {
        feedbackManager.showSuccess(getView(), message);
    }

    /**
     * Clear all form errors
     */
    private void clearErrors() {
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
    }

    /**
     * Clear form fields
     */
    private void clearForm() {
        binding.etEmail.setText("");
        binding.etPassword.setText("");
    }

    /**
     * Update field accessibility properties using AccessibilityUtils
     */
    private void updateFieldAccessibility(com.google.android.material.textfield.TextInputLayout layout, String fieldName) {
        ValidationUtils.ValidationResult result = null;
        
        if (layout.getError() != null) {
            result = ValidationUtils.ValidationResult.error(layout.getError().toString());
        } else {
            result = ValidationUtils.ValidationResult.success();
        }
        
        AccessibilityUtils.updateTextInputLayoutAccessibility(layout, fieldName, result);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        
        // Cleanup managers
        if (loadingStateManager != null) {
            loadingStateManager.cleanup();
        }
        
        binding = null;
    }
}