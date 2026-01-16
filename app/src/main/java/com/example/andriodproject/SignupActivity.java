package com.example.andriodproject;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * SignupActivity - Handles user registration
 * Features: Form validation, Password requirements (6-12 chars with number, lowercase, uppercase)
 */
public class SignupActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextInputLayout tilFirstName, tilLastName, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etFirstName, etLastName, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnSignUp;
    private TextView tvLogin;

    private DataBaseHelper dbHelper;

    // Password pattern: 6-12 characters, at least one digit, one lowercase, one uppercase
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Initialize database helper
        dbHelper = new DataBaseHelper(this);

        // Initialize views
        initViews();

        // Set click listeners
        setClickListeners();
    }

    /**
     * Initialize all views
     */
    private void initViews() {
        btnBack = findViewById(R.id.btnBack);
        tilFirstName = findViewById(R.id.tilFirstName);
        tilLastName = findViewById(R.id.tilLastName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnSignUp = findViewById(R.id.btnSignUp);
        tvLogin = findViewById(R.id.tvLogin);
    }

    /**
     * Set click listeners for buttons and links
     */
    private void setClickListeners() {
        // Back button click
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Sign up button click
        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                attemptSignUp();
            }
        });

        // Login link click - using explicit Intent
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // Go back to login
            }
        });
    }

    /**
     * Attempt to sign up with provided details
     */
    private void attemptSignUp() {
        // Clear previous errors
        clearErrors();

        // Get input values
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString();
        String confirmPassword = etConfirmPassword.getText().toString();

        // Validate inputs
        boolean isValid = true;

        // Validate first name
        if (firstName.isEmpty()) {
            tilFirstName.setError("First name is required");
            isValid = false;
        } else if (firstName.length() < 3) {
            tilFirstName.setError("First name must be at least 3 characters");
            isValid = false;
        }

        // Validate last name
        if (lastName.isEmpty()) {
            tilLastName.setError("Last name is required");
            isValid = false;
        } else if (lastName.length() < 3) {
            tilLastName.setError("Last name must be at least 3 characters");
            isValid = false;
        }

        // Validate email
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email");
            isValid = false;
        } else if (dbHelper.userExists(email)) {
            tilEmail.setError("This email is already registered");
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            isValid = false;
        } else if (!PASSWORD_PATTERN.matcher(password).matches()) {
            tilPassword.setError("Password must be 6-12 characters with at least one number, one lowercase, and one uppercase letter");
            isValid = false;
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password");
            isValid = false;
        } else if (!confirmPassword.equals(password)) {
            tilConfirmPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) {
            return;
        }

        // Create user
        User user = new User(email, firstName, lastName, password);
        
        if (dbHelper.insertUser(user)) {
            // Sign up successful
            Toast.makeText(this, "Account created successfully! Please login.", Toast.LENGTH_SHORT).show();
            
            // Go back to login with email pre-filled
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            intent.putExtra("email", email);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        } else {
            // Sign up failed
            Toast.makeText(this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Clear all error messages
     */
    private void clearErrors() {
        tilFirstName.setError(null);
        tilLastName.setError(null);
        tilEmail.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }
}
