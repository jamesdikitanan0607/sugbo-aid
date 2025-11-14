package com.sugboaid.auth.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.sugboaid.donation.R;
import com.sugboaid.donation.activities.MainActivity;
import com.sugboaid.donation.databinding.FragmentSignupBinding;
import com.sugboaid.donation.viewmodels.DashboardViewModel;
import com.sugboaid.donation.fragments.BaseFragment;
import com.sugboaid.viewmodels.AuthViewModel;
import com.sugboaid.utils.ValidationUtils;
import com.sugboaid.utils.AccessibilityUtils;
import com.sugboaid.utils.AuthErrorHandler;
import com.sugboaid.utils.AuthLoadingStateManager;
import com.sugboaid.utils.AuthFeedbackManager;

/**
 * SignupFragment for user registration
 * Handles new user account creation with form validation and authentication
 */
public class SignupFragment extends BaseFragment {

    private FragmentSignupBinding binding;
    private AuthViewModel authViewModel;
    private NavController navController;
    private AuthLoadingStateManager loadingStateManager;
    private AuthFeedbackManager feedbackManager;
    private String selectedRole;
    private boolean signupInitiated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignupBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        // Get role from arguments or SharedPreferences
        selectedRole = getSelectedRole();

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);

        // Initialize managers
        loadingStateManager = new AuthLoadingStateManager(requireContext());
        feedbackManager = new AuthFeedbackManager(requireContext());

        super.onViewCreated(view, savedInstanceState);

        // Initialize NavController
        navController = Navigation.findNavController(view);
    }

    @Override
    protected void initViews(View view) {
        // Views are initialized through ViewBinding
        // Set up initial UI state
        clearErrors();
        
        // Display selected role if available
        displaySelectedRole();
    }

    @Override
    protected void setupListeners() {
        // Signup button click listener
        binding.btnSignup.setOnClickListener(v -> {
            animateButtonPress(v);
            signupInitiated = true;
            validateAndSignup();
        });

        // Login link click listener
        binding.tvLoginLink.setOnClickListener(v -> {
            navigateToLogin();
        });

        // Real-time validation listeners
        setupRealTimeValidation();
    }

    @Override
    protected void observeData() {
        // Observe authentication state
        authViewModel.authState.observe(getViewLifecycleOwner(), authState -> {
            if (authState != null && authState.isAuthenticated() && signupInitiated) {
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
        // Name field validation
        binding.etName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateNameField();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

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
                validateConfirmPasswordField(); // Re-validate confirm password when password changes
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Confirm password field validation
        binding.etConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                validateConfirmPasswordField();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Validate name field in real-time with comprehensive validation
     */
    private void validateNameField() {
        String name = binding.etName.getText().toString().trim();
        
        if (name.isEmpty()) {
            binding.tilName.setError(null); // Clear error for empty field during typing
            binding.tilName.setHelperText(null);
        } else {
            ValidationUtils.ValidationResult result = authViewModel.validateNameDetailed(name);
            if (result.isValid()) {
                binding.tilName.setError(null);
                binding.tilName.setHelperText("Valid name");
            } else {
                binding.tilName.setError(result.getMessage());
                binding.tilName.setHelperText(null);
            }
        }
        
        // Update accessibility
        updateFieldAccessibility(binding.tilName, "Name");
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
     * Validate password field in real-time with strength indicator
     */
    private void validatePasswordField() {
        String password = binding.etPassword.getText().toString();
        
        if (password.isEmpty()) {
            binding.tilPassword.setError(null); // Clear error for empty field during typing
            binding.tilPassword.setHelperText(null);
            // Hide password strength indicator if it exists
            if (binding.passwordStrengthIndicator != null) {
                binding.passwordStrengthIndicator.setVisibility(View.GONE);
            }
        } else {
            ValidationUtils.ValidationResult result = authViewModel.validatePasswordDetailed(password);
            ValidationUtils.PasswordStrength strength = authViewModel.getPasswordStrength(password);
            
            if (result.isValid()) {
                binding.tilPassword.setError(null);
                binding.tilPassword.setHelperText(strength.getMessage());
            } else {
                binding.tilPassword.setError(result.getMessage());
                binding.tilPassword.setHelperText(null);
            }
            
            // Update password strength indicator if it exists
            if (binding.passwordStrengthIndicator != null) {
                binding.passwordStrengthIndicator.setVisibility(View.VISIBLE);
                binding.passwordStrengthIndicator.setPasswordStrength(strength);
                
                // Update accessibility for password strength indicator
                AccessibilityUtils.updatePasswordStrengthIndicatorAccessibility(binding.passwordStrengthIndicator, strength);
            }
            
            // Update password field accessibility with strength information
            AccessibilityUtils.updatePasswordStrengthAccessibility(binding.tilPassword, strength);
            
            // Show password requirements if password is weak
            if (strength.getFeedback() != null && !strength.getFeedback().isEmpty()) {
                binding.tilPassword.setHelperText(strength.getFeedback().replace("\n", " "));
            }
        }
        
        // Update accessibility
        updateFieldAccessibility(binding.tilPassword, "Password");
    }

    /**
     * Validate confirm password field in real-time with comprehensive validation
     */
    private void validateConfirmPasswordField() {
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();
        
        if (confirmPassword.isEmpty()) {
            binding.tilConfirmPassword.setError(null); // Clear error for empty field during typing
            binding.tilConfirmPassword.setHelperText(null);
        } else {
            ValidationUtils.ValidationResult result = authViewModel.validatePasswordMatchDetailed(password, confirmPassword);
            if (result.isValid()) {
                binding.tilConfirmPassword.setError(null);
                binding.tilConfirmPassword.setHelperText("Passwords match");
            } else {
                binding.tilConfirmPassword.setError(result.getMessage());
                binding.tilConfirmPassword.setHelperText(null);
            }
        }
        
        // Update accessibility
        updateFieldAccessibility(binding.tilConfirmPassword, "Confirm Password");
    }

    /**
     * Validate form and attempt signup using comprehensive validation
     */
    private void validateAndSignup() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString();
        String confirmPassword = binding.etConfirmPassword.getText().toString();

        // Clear previous errors
        clearErrors();

        // Use comprehensive form validation
        ValidationUtils.FormValidationResult formResult = authViewModel.validateSignupForm(name, email, password, confirmPassword);

        // Apply validation results to UI
        applyNameValidation(formResult.getNameResult());
        applyEmailValidation(formResult.getEmailResult());
        applyPasswordValidation(formResult.getPasswordResult());
        applyConfirmPasswordValidation(formResult.getConfirmPasswordResult());

        if (formResult.isValid()) {
            // Attempt signup with role
            authViewModel.signup(name, email, password, confirmPassword, selectedRole);
        } else {
            // Animate error fields and announce errors for accessibility
            announceValidationErrors(formResult);
        }
    }

    /**
     * Apply name validation result to UI
     */
    private void applyNameValidation(ValidationUtils.ValidationResult result) {
        if (result != null) {
            if (result.isValid()) {
                binding.tilName.setError(null);
                binding.tilName.setHelperText("Valid name");
            } else {
                binding.tilName.setError(result.getMessage());
                binding.tilName.setHelperText(null);
                animateError(binding.tilName);
            }
            updateFieldAccessibility(binding.tilName, "Name");
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
            String password = binding.etPassword.getText().toString();
            ValidationUtils.PasswordStrength strength = authViewModel.getPasswordStrength(password);
            
            if (result.isValid()) {
                binding.tilPassword.setError(null);
                binding.tilPassword.setHelperText(strength.getMessage());
            } else {
                binding.tilPassword.setError(result.getMessage());
                binding.tilPassword.setHelperText(null);
                animateError(binding.tilPassword);
            }
            
            // Update password strength indicator if it exists
            if (binding.passwordStrengthIndicator != null) {
                binding.passwordStrengthIndicator.setPasswordStrength(strength);
            }
            
            updateFieldAccessibility(binding.tilPassword, "Password");
        }
    }

    /**
     * Apply confirm password validation result to UI
     */
    private void applyConfirmPasswordValidation(ValidationUtils.ValidationResult result) {
        if (result != null) {
            if (result.isValid()) {
                binding.tilConfirmPassword.setError(null);
                binding.tilConfirmPassword.setHelperText("Passwords match");
            } else {
                binding.tilConfirmPassword.setError(result.getMessage());
                binding.tilConfirmPassword.setHelperText(null);
                animateError(binding.tilConfirmPassword);
            }
            updateFieldAccessibility(binding.tilConfirmPassword, "Confirm Password");
        }
    }

    /**
     * Announce validation errors for accessibility using AccessibilityUtils
     */
    private void announceValidationErrors(ValidationUtils.FormValidationResult formResult) {
        String announcement = AccessibilityUtils.createFormValidationAnnouncement(formResult);
        
        // Announce for screen readers
        if (getView() != null) {
            AccessibilityUtils.announceForAccessibility(getView(), announcement);
        }
    }

    /**
     * Navigate to login fragment
     */
    private void navigateToLogin() {
        try {
            navController.navigate(R.id.action_signupFragment_to_loginFragment);
        } catch (Exception e) {
            showToast("Navigation error occurred");
        }
    }

    /**
     * Navigate to dashboard after successful registration
     */
    private void navigateToDashboard() {
        try {
            MainActivity activity = (MainActivity) requireActivity();
            activity.switchToMainGraphAndShowDashboard(true);
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
                binding.btnSignup,
                getString(R.string.creating_account),
                getString(R.string.create_account),
                binding.tilName,
                binding.tilEmail,
                binding.tilPassword,
                binding.tilConfirmPassword
            );
        } else {
            loadingStateManager.hideAuthLoading(
                binding.btnSignup,
                binding.tilName,
                binding.tilEmail,
                binding.tilPassword,
                binding.tilConfirmPassword
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
        binding.tilName.setError(null);
        binding.tilEmail.setError(null);
        binding.tilPassword.setError(null);
        binding.tilConfirmPassword.setError(null);
    }

    /**
     * Clear form fields
     */
    private void clearForm() {
        binding.etName.setText("");
        binding.etEmail.setText("");
        binding.etPassword.setText("");
        binding.etConfirmPassword.setText("");
        
        // Hide password strength indicator if it exists
        if (binding.passwordStrengthIndicator != null) {
            binding.passwordStrengthIndicator.setVisibility(View.GONE);
        }
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

    /**
     * Get selected role from arguments or SharedPreferences
     */
    private String getSelectedRole() {
        // First try to get from navigation arguments
        Bundle args = getArguments();
        if (args != null && args.containsKey("role")) {
            return args.getString("role");
        }
        
        // Fallback to SharedPreferences (set by SplashActivity)
        try {
            com.sugboaid.utils.SharedPreferencesHelper prefsHelper = 
                com.sugboaid.utils.SharedPreferencesHelper.getInstance(requireContext());
            String role = prefsHelper.getUserRole();
            return role != null ? role : "Guest";
        } catch (Exception e) {
            return "Guest";
        }
    }

    /**
     * Display selected role in the UI
     */
    private void displaySelectedRole() {
        if (selectedRole != null && binding != null) {
            // You can add a TextView to show the selected role
            // For now, we'll just show it in a toast for confirmation
            if (!"Guest".equals(selectedRole)) {
                showToast("Signing up as: " + selectedRole);
            }
        }
    }
}