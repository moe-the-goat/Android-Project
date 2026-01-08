package com.example.andriodproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.andriodproject.MainActivity;
import com.example.andriodproject.R;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.regex.Pattern;

/**
 * ProfileFragment - Handles user profile management
 */
public class ProfileFragment extends Fragment {

    private TextView tvName, tvEmail;
    private TextInputLayout tilFirstName, tilLastName, tilCurrentPassword, tilNewPassword, tilConfirmNewPassword;
    private TextInputEditText etFirstName, etLastName, etCurrentPassword, etNewPassword, etConfirmNewPassword;
    private MaterialButton btnSaveProfile, btnChangePassword;

    private DataBaseHelper dbHelper;
    private String userEmail;
    private User currentUser;

    // Password pattern: 6-12 characters, at least one digit, one lowercase, one uppercase
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
            "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z]).{6,12}$"
    );

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        dbHelper = new DataBaseHelper(requireContext());
        userEmail = ((MainActivity) requireActivity()).getCurrentUserEmail();

        initViews(view);
        loadUserData();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        tvName = view.findViewById(R.id.tvName);
        tvEmail = view.findViewById(R.id.tvEmail);
        
        tilFirstName = view.findViewById(R.id.tilFirstName);
        tilLastName = view.findViewById(R.id.tilLastName);
        tilCurrentPassword = view.findViewById(R.id.tilCurrentPassword);
        tilNewPassword = view.findViewById(R.id.tilNewPassword);
        tilConfirmNewPassword = view.findViewById(R.id.tilConfirmNewPassword);
        
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etCurrentPassword = view.findViewById(R.id.etCurrentPassword);
        etNewPassword = view.findViewById(R.id.etNewPassword);
        etConfirmNewPassword = view.findViewById(R.id.etConfirmNewPassword);
        
        btnSaveProfile = view.findViewById(R.id.btnSaveProfile);
        btnChangePassword = view.findViewById(R.id.btnChangePassword);
    }

    private void loadUserData() {
        currentUser = dbHelper.getUser(userEmail);
        
        if (currentUser != null) {
            String fullName = currentUser.getFirstName() + " " + currentUser.getLastName();
            tvName.setText(fullName);
            tvEmail.setText(currentUser.getEmail());
            
            etFirstName.setText(currentUser.getFirstName());
            etLastName.setText(currentUser.getLastName());
        }
    }

    private void setupListeners() {
        btnSaveProfile.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> changePassword());
    }

    private void saveProfile() {
        // Clear previous errors
        tilFirstName.setError(null);
        tilLastName.setError(null);

        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();

        boolean isValid = true;

        if (firstName.isEmpty()) {
            tilFirstName.setError("First name is required");
            isValid = false;
        } else if (firstName.length() < 2) {
            tilFirstName.setError("First name must be at least 2 characters");
            isValid = false;
        }

        if (lastName.isEmpty()) {
            tilLastName.setError("Last name is required");
            isValid = false;
        } else if (lastName.length() < 2) {
            tilLastName.setError("Last name must be at least 2 characters");
            isValid = false;
        }

        if (!isValid) return;

        if (dbHelper.updateUserProfile(userEmail, firstName, lastName)) {
            Toast.makeText(requireContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
            
            // Update displayed name
            String fullName = firstName + " " + lastName;
            tvName.setText(fullName);
            
            // Refresh navigation header in MainActivity
            ((MainActivity) requireActivity()).refreshNavHeader();
        } else {
            Toast.makeText(requireContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
        }
    }

    private void changePassword() {
        // Clear previous errors
        tilCurrentPassword.setError(null);
        tilNewPassword.setError(null);
        tilConfirmNewPassword.setError(null);

        String currentPassword = etCurrentPassword.getText().toString();
        String newPassword = etNewPassword.getText().toString();
        String confirmNewPassword = etConfirmNewPassword.getText().toString();

        boolean isValid = true;

        // Validate current password
        if (currentPassword.isEmpty()) {
            tilCurrentPassword.setError("Current password is required");
            isValid = false;
        } else if (!currentPassword.equals(currentUser.getPassword())) {
            tilCurrentPassword.setError("Current password is incorrect");
            isValid = false;
        }

        // Validate new password
        if (newPassword.isEmpty()) {
            tilNewPassword.setError("New password is required");
            isValid = false;
        } else if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
            tilNewPassword.setError("Password must be 6-12 characters with at least one number, one lowercase, and one uppercase letter");
            isValid = false;
        } else if (newPassword.equals(currentPassword)) {
            tilNewPassword.setError("New password must be different from current password");
            isValid = false;
        }

        // Validate confirm password
        if (confirmNewPassword.isEmpty()) {
            tilConfirmNewPassword.setError("Please confirm your new password");
            isValid = false;
        } else if (!confirmNewPassword.equals(newPassword)) {
            tilConfirmNewPassword.setError("Passwords do not match");
            isValid = false;
        }

        if (!isValid) return;

        if (dbHelper.updateUserPassword(userEmail, newPassword)) {
            Toast.makeText(requireContext(), "Password changed successfully", Toast.LENGTH_SHORT).show();
            
            // Clear password fields
            etCurrentPassword.setText("");
            etNewPassword.setText("");
            etConfirmNewPassword.setText("");
            
            // Update current user object
            currentUser.setPassword(newPassword);
        } else {
            Toast.makeText(requireContext(), "Failed to change password", Toast.LENGTH_SHORT).show();
        }
    }
}
