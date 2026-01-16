package com.example.andriodproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
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
import com.example.andriodproject.adapters.CategoryAdapter;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Category;
import com.example.andriodproject.utils.SharedPrefManager;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

// Handles app settings: theme, default period, category management
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
        switchDarkMode.setChecked(sharedPrefManager.isDarkModeEnabled());

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

        layoutAddIncomeCategory.setOnClickListener(v -> showAddCategoryDialog("INCOME"));
        layoutAddExpenseCategory.setOnClickListener(v -> showAddCategoryDialog("EXPENSE"));
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
        // Get user's custom categories only
        List<Category> allCustomCategories = new ArrayList<>();
        
        for (Category c : dbHelper.getCategoriesByType("INCOME", userEmail)) {
            if (c.getUserEmail() != null) allCustomCategories.add(c);
        }
        for (Category c : dbHelper.getCategoriesByType("EXPENSE", userEmail)) {
            if (c.getUserEmail() != null) allCustomCategories.add(c);
        }

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_manage_categories, null);
        RecyclerView rvCategories = dialogView.findViewById(R.id.rvCategories);
        TextView tvNoCategories = dialogView.findViewById(R.id.tvNoCategories);

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Manage Categories")
                .setView(dialogView)
                .setPositiveButton("Close", null)
                .create();

        if (allCustomCategories.isEmpty()) {
            tvNoCategories.setVisibility(View.VISIBLE);
            rvCategories.setVisibility(View.GONE);
        } else {
            tvNoCategories.setVisibility(View.GONE);
            rvCategories.setVisibility(View.VISIBLE);

            CategoryAdapter adapter = new CategoryAdapter(requireContext(), allCustomCategories);
            rvCategories.setLayoutManager(new LinearLayoutManager(requireContext()));
            rvCategories.setAdapter(adapter);

            adapter.setOnCategoryActionListener(new CategoryAdapter.OnCategoryActionListener() {
                @Override
                public void onEditClick(Category category) {
                    dialog.dismiss();
                    showEditCategoryDialog(category);
                }

                @Override
                public void onDeleteClick(Category category) {
                    new AlertDialog.Builder(requireContext())
                            .setTitle("Delete Category")
                            .setMessage("Delete \"" + category.getName() + "\"?")
                            .setPositiveButton("Delete", (d, w) -> {
                                if (dbHelper.deleteCategory(category.getId())) {
                                    Toast.makeText(requireContext(), "Category deleted", Toast.LENGTH_SHORT).show();
                                    dialog.dismiss();
                                    showManageCategoriesDialog(); // Refresh
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                }
            });
        }

        dialog.show();
    }

    private void showEditCategoryDialog(Category category) {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_add_category, null);
        TextInputLayout tilCategoryName = dialogView.findViewById(R.id.tilCategoryName);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        etCategoryName.setText(category.getName());

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle("Rename Category")
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String newName = etCategoryName.getText().toString().trim();

                if (newName.isEmpty()) {
                    tilCategoryName.setError("Name is required");
                    return;
                }

                category.setName(newName);
                if (dbHelper.updateCategory(category)) {
                    Toast.makeText(requireContext(), "Category updated", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    showManageCategoriesDialog(); // Refresh list
                } else {
                    Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                }
            });
        });

        dialog.show();
    }
}
