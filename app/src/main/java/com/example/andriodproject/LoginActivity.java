package com.example.andriodproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.utils.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * LoginActivity - Handles user login with email and password
 * Features: Email validation, Remember Me functionality, Navigation to SignUp
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilEmail, tilPassword;
    private TextInputEditText etEmail, etPassword;
    private CheckBox cbRememberMe;
    private MaterialButton btnLogin;
    private TextView tvSignUp;

    private DataBaseHelper dbHelper;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize database and shared preferences
        dbHelper = new DataBaseHelper(this);
        sharedPrefManager = SharedPrefManager.getInstance(this);

        // Check if user is already logged in
        if (sharedPrefManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Initialize views
        initViews();

        // Load saved email if remember me was checked
        loadSavedEmail();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initViews() {
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
    }

    /**
     * Load saved email if remember me was previously checked
     */
    private void loadSavedEmail() {
        String savedEmail = sharedPrefManager.getSavedEmail();
        if (savedEmail != null) {
            etEmail.setText(savedEmail);
            cbRememberMe.setChecked(true);
        }
    }

    /**
     * Set click listeners for buttons and links
     */
    private void setClickListeners() {
        // Login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptLogin();
            }
        });

        // Sign up link click - using explicit Intent
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * Attempt to login with provided credentials
     */
    private void attemptLogin() {
        // Clear previous errors
        tilEmail.setError(null);
        tilPassword.setError(null);

        // Get input values
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate inputs
        boolean isValid = true;

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        }

        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Authenticate user
        if (dbHelper.authenticateUser(email, password)) {
            // Login successful
            boolean rememberMe = cbRememberMe.isChecked();
            sharedPrefManager.saveUserSession(email, rememberMe);
            
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
            navigateToMain();
        } else {
            // Login failed
            Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show();
            tilPassword.setError("Invalid credentials");
        }
    }

    /**
     * Navigate to main activity
     */
    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
