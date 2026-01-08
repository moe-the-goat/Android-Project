package com.example.andriodproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodproject.MainActivity;
import com.example.andriodproject.R;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Category;
import com.example.andriodproject.utils.SharedPrefManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.List;

/**
 * SettingsFragment - Handles app settings including theme, default period, and categories
 */
public class SettingsFragment extends Fragment {

    private SwitchMaterial switchDarkMode;
    private RadioGroup rgPeriod;
    private RadioButton rbDaily, rbWeekly, rbMonthly;
    private LinearLayout layoutAddIncomeCategory, layoutAddExpenseCategory, layoutManageCategories;

    private DataBaseHelper dbHelper;
    private SharedPrefManager sharedPrefManager;
    private String userEmail;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        dbHelper = new DataBaseHelper(requireContext());
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        userEmail = ((MainActivity) requireActivity()).getCurrentUserEmail();

        initViews(view);
        loadSettings();
        setupListeners();

        return view;
    }

    private void initViews(View view) {
        switchDarkMode = view.findViewById(R.id.switchDarkMode);
        rgPeriod = view.findViewById(R.id.rgPeriod);
        rbDaily = view.findViewById(R.id.rbDaily);
        rbWeekly = view.findViewById(R.id.rbWeekly);
        rbMonthly = view.findViewById(R.id.rbMonthly);
        layoutAddIncomeCategory = view.findViewById(R.id.layoutAddIncomeCategory);
        layoutAddExpenseCategory = view.findViewById(R.id.layoutAddExpenseCategory);
        layoutManageCategories = view.findViewById(R.id.layoutManageCategories);
    }

    private void loadSettings() {
        // Load dark mode setting
        switchDarkMode.setChecked(sharedPrefManager.isDarkModeEnabled());

        // Load default period setting
        String period = sharedPrefManager.getDefaultPeriod();
        switch (period) {
            case "daily":
                rbDaily.setChecked(true);
                break;
            case "weekly":
                rbWeekly.setChecked(true);
                break;
            default:
                rbMonthly.setChecked(true);
                break;
        }
    }

    private void setupListeners() {
        // Dark mode toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sharedPrefManager.setDarkMode(isChecked);
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        // Period selection
        rgPeriod.setOnCheckedChangeListener((group, checkedId) -> {
            String period;
            if (checkedId == R.id.rbDaily) {
                period = "daily";
            } else if (checkedId == R.id.rbWeekly) {
                period = "weekly";
            } else {
                period = "monthly";
            }
            sharedPrefManager.setDefaultPeriod(period);
            Toast.makeText(requireContext(), "Default period updated", Toast.LENGTH_SHORT).show();
        });

        // Add income category
        layoutAddIncomeCategory.setOnClickListener(v -> showAddCategoryDialog("INCOME"));

        // Add expense category
        layoutAddExpenseCategory.setOnClickListener(v -> showAddCategoryDialog("EXPENSE"));

        // Manage categories
        layoutManageCategories.setOnClickListener(v -> showManageCategoriesDialog());
    }

    private void showAddCategoryDialog(String type) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_category, null);
        TextInputLayout tilCategoryName = dialogView.findViewById(R.id.tilCategoryName);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);

        String title = type.equals("INCOME") ? "Add Income Category" : "Add Expense Category";

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(title)
                .setView(dialogView)
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String name = etCategoryName.getText().toString().trim();

                if (name.isEmpty()) {
                    tilCategoryName.setError("Category name is required");
                    return;
                }

                tilCategoryName.setError(null);

                Category category = new Category(name, type, userEmail);
                if (dbHelper.insertCategory(category) != -1) {
                    Toast.makeText(requireContext(), "Category added", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to add category", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }

    private void showManageCategoriesDialog() {
        // Get user's custom categories
        List<Category> incomeCategories = dbHelper.getCategoriesByType("INCOME", userEmail);
        List<Category> expenseCategories = dbHelper.getCategoriesByType("EXPENSE", userEmail);

        // Filter to show only custom (user-created) categories
        StringBuilder message = new StringBuilder();
        message.append("Custom Income Categories:\n");
        
        int customIncomeCount = 0;
        for (Category c : incomeCategories) {
            if (c.getUserEmail() != null) {
                message.append("• ").append(c.getName()).append("\n");
                customIncomeCount++;
            }
        }
        if (customIncomeCount == 0) {
            message.append("None\n");
        }

        message.append("\nCustom Expense Categories:\n");
        
        int customExpenseCount = 0;
        for (Category c : expenseCategories) {
            if (c.getUserEmail() != null) {
                message.append("• ").append(c.getName()).append("\n");
                customExpenseCount++;
            }
        }
        if (customExpenseCount == 0) {
            message.append("None\n");
        }

        message.append("\nNote: Long press on a custom category in the transaction screen to delete it.");

        new AlertDialog.Builder(requireContext())
                .setTitle("Custom Categories")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }
}
