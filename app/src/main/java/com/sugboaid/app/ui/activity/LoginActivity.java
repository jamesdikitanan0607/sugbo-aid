package com.sugboaid.app.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.sugboaid.app.R;
import com.sugboaid.app.manager.AuthManager;
import com.sugboaid.app.util.Constants;
import com.sugboaid.app.util.ThemeUtils;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout, roleLayout;
    private TextInputEditText emailInput, passwordInput;
    private AutoCompleteTextView roleInput;
    private MaterialButton loginButton, guestButton;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply theme
        ThemeUtils.applyTheme(this);

        // Check if user is already logged in
        authManager = AuthManager.getInstance(this);
        if (authManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupRoleDropdown();
        setupClickListeners();
    }

    private void initViews() {
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        roleLayout = findViewById(R.id.roleLayout);

        emailInput = findViewById(R.id.emailInput);
        passwordInput = findViewById(R.id.passwordInput);
        roleInput = findViewById(R.id.roleInput);

        loginButton = findViewById(R.id.loginButton);
        guestButton = findViewById(R.id.guestButton);
    }

    private void setupRoleDropdown() {
        String[] roles = {
                Constants.ROLE_DONOR,
                Constants.ROLE_ORGANIZATION,
                Constants.ROLE_VOLUNTEER,
                Constants.ROLE_RECIPIENT
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                roles);
        roleInput.setAdapter(adapter);
        roleInput.setText(Constants.ROLE_DONOR, false);
    }

    private void setupClickListeners() {
        loginButton.setOnClickListener(v -> performLogin());
        guestButton.setOnClickListener(v -> loginAsGuest());
    }

    private void performLogin() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String role = roleInput.getText().toString().trim();

        // Clear previous errors
        emailLayout.setError(null);
        passwordLayout.setError(null);
        roleLayout.setError(null);

        // Validate inputs
        if (email.isEmpty()) {
            emailLayout.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Please enter a valid email");
            return;
        }

        if (password.isEmpty()) {
            passwordLayout.setError("Password is required");
            return;
        }

        if (password.length() < Constants.MIN_PASSWORD_LENGTH) {
            passwordLayout.setError("Password must be at least " + Constants.MIN_PASSWORD_LENGTH + " characters");
            return;
        }

        if (role.isEmpty()) {
            roleLayout.setError("Please select a role");
            return;
        }

        // Show loading state
        loginButton.setEnabled(false);
        loginButton.setText("Logging in...");

        // Perform login
        AuthManager.LoginResult result = authManager.login(email, password, role);

        if (result == AuthManager.LoginResult.SUCCESS) {
            Snackbar.make(findViewById(android.R.id.content), "Login successful!", Snackbar.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            String errorMessage;
            switch (result) {
                case INVALID_ROLE:
                    errorMessage = "Login failed: Account exists with a different role.";
                    break;
                case ACCOUNT_INACTIVE:
                    errorMessage = "Login failed: Account is inactive.";
                    break;
                default:
                    errorMessage = "Login failed. Please try again.";
            }
            Snackbar.make(findViewById(android.R.id.content), errorMessage, Snackbar.LENGTH_LONG).show();
            loginButton.setEnabled(true);
            loginButton.setText("Login");
        }
    }

    private void loginAsGuest() {
        AuthManager.LoginResult result = authManager.login("guest@sugboaid.org", "guest123", Constants.ROLE_GUEST);

        if (result == AuthManager.LoginResult.SUCCESS) {
            Snackbar.make(findViewById(android.R.id.content), "Logged in as Guest", Snackbar.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            Snackbar.make(findViewById(android.R.id.content), "Guest login failed", Snackbar.LENGTH_LONG).show();
        }
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        // Disable back button on login screen
        moveTaskToBack(true);
    }
}