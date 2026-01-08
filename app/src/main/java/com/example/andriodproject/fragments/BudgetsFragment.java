package com.example.andriodproject.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodproject.MainActivity;
import com.example.andriodproject.R;
import com.example.andriodproject.adapters.BudgetAdapter;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Budget;
import com.example.andriodproject.model.Category;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * BudgetsFragment - Handles budget management with alerts
 */
public class BudgetsFragment extends Fragment implements BudgetAdapter.OnBudgetClickListener {

    private TextView tvCurrentMonth, tvTotalBudget, tvTotalSpent, tvRemaining, tvEmpty;
    private ImageButton btnPrevMonth, btnNextMonth;
    private RecyclerView rvBudgets;
    private FloatingActionButton fabAdd;

    private DataBaseHelper dbHelper;
    private String userEmail;
    private BudgetAdapter adapter;
    private List<Budget> budgets;
    private List<Category> expenseCategories;

    private Calendar currentMonth;
    private SimpleDateFormat monthDisplayFormat;
    private SimpleDateFormat monthDbFormat;
    private NumberFormat currencyFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budgets, container, false);

        dbHelper = new DataBaseHelper(requireContext());
        userEmail = ((MainActivity) requireActivity()).getCurrentUserEmail();

        currentMonth = Calendar.getInstance();
        monthDisplayFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
        monthDbFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

        initViews(view);
        setupRecyclerView();
        loadData();
        setupClickListeners();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadData();
    }

    private void initViews(View view) {
        tvCurrentMonth = view.findViewById(R.id.tvCurrentMonth);
        tvTotalBudget = view.findViewById(R.id.tvTotalBudget);
        tvTotalSpent = view.findViewById(R.id.tvTotalSpent);
        tvRemaining = view.findViewById(R.id.tvRemaining);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        btnPrevMonth = view.findViewById(R.id.btnPrevMonth);
        btnNextMonth = view.findViewById(R.id.btnNextMonth);
        rvBudgets = view.findViewById(R.id.rvBudgets);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        budgets = new ArrayList<>();
        adapter = new BudgetAdapter(requireContext(), budgets, dbHelper);
        adapter.setOnBudgetClickListener(this);
        rvBudgets.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgets.setAdapter(adapter);
    }

    private void loadData() {
        // Update month display
        tvCurrentMonth.setText(monthDisplayFormat.format(currentMonth.getTime()));

        String month = monthDbFormat.format(currentMonth.getTime());

        // Load budgets for current month
        budgets.clear();
        budgets.addAll(dbHelper.getBudgetsByMonth(userEmail, month));
        adapter.notifyDataSetChanged();

        // Calculate totals
        double totalBudget = 0;
        double totalSpent = 0;

        for (Budget budget : budgets) {
            totalBudget += budget.getBudgetLimit();
            totalSpent += dbHelper.getSpendingForCategory(userEmail, budget.getCategoryId(), month);
        }

        double remaining = totalBudget - totalSpent;

        tvTotalBudget.setText(currencyFormat.format(totalBudget));
        tvTotalSpent.setText(currencyFormat.format(totalSpent));
        tvRemaining.setText(currencyFormat.format(remaining));

        // Show/hide empty state
        if (budgets.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvBudgets.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvBudgets.setVisibility(View.VISIBLE);
        }

        // Load expense categories
        expenseCategories = dbHelper.getCategoriesByType("EXPENSE", userEmail);
    }

    private void setupClickListeners() {
        btnPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            loadData();
        });

        btnNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            loadData();
        });

        fabAdd.setOnClickListener(v -> showAddBudgetDialog(null));
    }

    private void showAddBudgetDialog(Budget editBudget) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null);

        TextInputLayout tilCategory = dialogView.findViewById(R.id.tilCategory);
        TextInputLayout tilBudgetLimit = dialogView.findViewById(R.id.tilBudgetLimit);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        TextInputEditText etBudgetLimit = dialogView.findViewById(R.id.etBudgetLimit);
        SwitchMaterial switchAlert = dialogView.findViewById(R.id.switchAlert);
        LinearLayout layoutThreshold = dialogView.findViewById(R.id.layoutThreshold);
        TextView tvThresholdValue = dialogView.findViewById(R.id.tvThresholdValue);
        Slider sliderThreshold = dialogView.findViewById(R.id.sliderThreshold);

        // Setup category dropdown
        List<String> categoryNames = new ArrayList<>();
        for (Category c : expenseCategories) {
            categoryNames.add(c.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);

        // Setup alert switch
        switchAlert.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutThreshold.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        // Setup threshold slider
        sliderThreshold.addOnChangeListener((slider, value, fromUser) -> {
            tvThresholdValue.setText((int) value + "%");
        });

        // Pre-fill if editing
        String dialogTitle = "Add Budget";
        if (editBudget != null) {
            dialogTitle = "Edit Budget";
            Category cat = dbHelper.getCategoryById(editBudget.getCategoryId());
            if (cat != null) actvCategory.setText(cat.getName(), false);
            etBudgetLimit.setText(String.valueOf(editBudget.getBudgetLimit()));
            switchAlert.setChecked(editBudget.isAlertEnabled());
            sliderThreshold.setValue((float) editBudget.getAlertThreshold());
            tvThresholdValue.setText((int) editBudget.getAlertThreshold() + "%");
            
            // Disable category selection when editing
            actvCategory.setEnabled(false);
        }

        AlertDialog dialog = new AlertDialog.Builder(requireContext())
                .setTitle(dialogTitle)
                .setView(dialogView)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                // Validate inputs
                String categoryName = actvCategory.getText().toString().trim();
                String limitStr = etBudgetLimit.getText().toString().trim();

                boolean isValid = true;

                if (categoryName.isEmpty()) {
                    tilCategory.setError("Category is required");
                    isValid = false;
                } else {
                    tilCategory.setError(null);
                }

                if (limitStr.isEmpty()) {
                    tilBudgetLimit.setError("Budget limit is required");
                    isValid = false;
                } else {
                    tilBudgetLimit.setError(null);
                }

                if (!isValid) return;

                // Find category ID
                long categoryId = -1;
                for (Category c : expenseCategories) {
                    if (c.getName().equals(categoryName)) {
                        categoryId = c.getId();
                        break;
                    }
                }

                if (categoryId == -1) {
                    tilCategory.setError("Please select a valid category");
                    return;
                }

                double budgetLimit = Double.parseDouble(limitStr);
                boolean alertEnabled = switchAlert.isChecked();
                double alertThreshold = sliderThreshold.getValue();
                String month = monthDbFormat.format(currentMonth.getTime());

                // Check if budget already exists for this category and month
                if (editBudget == null) {
                    Budget existing = dbHelper.getBudgetByCategoryAndMonth(userEmail, categoryId, month);
                    if (existing != null) {
                        tilCategory.setError("Budget already exists for this category");
                        return;
                    }
                }

                if (editBudget != null) {
                    // Update existing budget
                    editBudget.setBudgetLimit(budgetLimit);
                    editBudget.setAlertEnabled(alertEnabled);
                    editBudget.setAlertThreshold(alertThreshold);

                    if (dbHelper.updateBudget(editBudget)) {
                        Toast.makeText(requireContext(), "Budget updated", Toast.LENGTH_SHORT).show();
                        loadData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Create new budget
                    Budget budget = new Budget(userEmail, categoryId, budgetLimit, month, alertEnabled, alertThreshold);

                    if (dbHelper.insertBudget(budget) != -1) {
                        Toast.makeText(requireContext(), "Budget added", Toast.LENGTH_SHORT).show();
                        loadData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add budget", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onBudgetClick(Budget budget) {
        showAddBudgetDialog(budget);
    }

    @Override
    public void onBudgetLongClick(Budget budget) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Budget")
                .setMessage("Are you sure you want to delete this budget?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteBudget(budget.getId())) {
                        Toast.makeText(requireContext(), "Budget deleted", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
