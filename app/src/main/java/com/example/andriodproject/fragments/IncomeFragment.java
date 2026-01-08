package com.example.andriodproject.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import com.example.andriodproject.adapters.TransactionAdapter;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Category;
import com.example.andriodproject.model.Transaction;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * IncomeFragment - Handles income transaction management
 */
public class IncomeFragment extends Fragment implements TransactionAdapter.OnTransactionClickListener {

    private TextView tvSummaryAmount, tvTransactionCount, tvEmpty;
    private RecyclerView rvTransactions;
    private FloatingActionButton fabAdd;

    private DataBaseHelper dbHelper;
    private String userEmail;
    private TransactionAdapter adapter;
    private List<Transaction> transactions;
    private List<Category> categories;

    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_income, container, false);

        dbHelper = new DataBaseHelper(requireContext());
        userEmail = ((MainActivity) requireActivity()).getCurrentUserEmail();
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

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
        tvSummaryAmount = view.findViewById(R.id.tvSummaryAmount);
        tvTransactionCount = view.findViewById(R.id.tvTransactionCount);
        tvEmpty = view.findViewById(R.id.tvEmpty);
        rvTransactions = view.findViewById(R.id.rvTransactions);
        fabAdd = view.findViewById(R.id.fabAdd);
    }

    private void setupRecyclerView() {
        transactions = new ArrayList<>();
        adapter = new TransactionAdapter(requireContext(), transactions, dbHelper);
        adapter.setOnTransactionClickListener(this);
        rvTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvTransactions.setAdapter(adapter);
    }

    private void loadData() {
        // Load income transactions
        transactions.clear();
        transactions.addAll(dbHelper.getTransactionsByType(userEmail, "INCOME"));
        adapter.notifyDataSetChanged();

        // Calculate total
        double total = 0;
        for (Transaction t : transactions) {
            total += t.getAmount();
        }

        tvSummaryAmount.setText(currencyFormat.format(total));
        tvTransactionCount.setText(transactions.size() + " transactions");

        // Show/hide empty state
        if (transactions.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            rvTransactions.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            rvTransactions.setVisibility(View.VISIBLE);
        }

        // Load categories
        categories = dbHelper.getCategoriesByType("INCOME", userEmail);
    }

    private void setupClickListeners() {
        fabAdd.setOnClickListener(v -> showAddTransactionDialog(null));
    }

    private void showAddTransactionDialog(Transaction editTransaction) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_transaction, null);

        TextInputLayout tilAmount = dialogView.findViewById(R.id.tilAmount);
        TextInputLayout tilCategory = dialogView.findViewById(R.id.tilCategory);
        TextInputEditText etAmount = dialogView.findViewById(R.id.etAmount);
        AutoCompleteTextView actvCategory = dialogView.findViewById(R.id.actvCategory);
        TextInputEditText etDate = dialogView.findViewById(R.id.etDate);
        TextInputEditText etDescription = dialogView.findViewById(R.id.etDescription);

        // Setup category dropdown
        List<String> categoryNames = new ArrayList<>();
        for (Category c : categories) {
            categoryNames.add(c.getName());
        }
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, categoryNames);
        actvCategory.setAdapter(categoryAdapter);

        // Setup date picker
        final Calendar calendar = Calendar.getInstance();
        etDate.setText(dateFormat.format(calendar.getTime()));

        etDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        calendar.set(year, month, dayOfMonth);
                        etDate.setText(dateFormat.format(calendar.getTime()));
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        // Pre-fill if editing
        String dialogTitle = "Add Income";
        if (editTransaction != null) {
            dialogTitle = "Edit Income";
            etAmount.setText(String.valueOf(editTransaction.getAmount()));
            Category cat = dbHelper.getCategoryById(editTransaction.getCategoryId());
            if (cat != null) actvCategory.setText(cat.getName(), false);
            etDate.setText(editTransaction.getDate());
            etDescription.setText(editTransaction.getDescription());
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
                String amountStr = etAmount.getText().toString().trim();
                String categoryName = actvCategory.getText().toString().trim();
                String date = etDate.getText().toString().trim();
                String description = etDescription.getText().toString().trim();

                boolean isValid = true;

                if (amountStr.isEmpty()) {
                    tilAmount.setError("Amount is required");
                    isValid = false;
                } else {
                    tilAmount.setError(null);
                }

                if (categoryName.isEmpty()) {
                    tilCategory.setError("Category is required");
                    isValid = false;
                } else {
                    tilCategory.setError(null);
                }

                if (!isValid) return;

                // Find category ID
                long categoryId = -1;
                for (Category c : categories) {
                    if (c.getName().equals(categoryName)) {
                        categoryId = c.getId();
                        break;
                    }
                }

                if (categoryId == -1) {
                    tilCategory.setError("Please select a valid category");
                    return;
                }

                double amount = Double.parseDouble(amountStr);

                if (editTransaction != null) {
                    // Update existing transaction
                    editTransaction.setAmount(amount);
                    editTransaction.setCategoryId(categoryId);
                    editTransaction.setDate(date);
                    editTransaction.setDescription(description);

                    if (dbHelper.updateTransaction(editTransaction)) {
                        Toast.makeText(requireContext(), "Income updated", Toast.LENGTH_SHORT).show();
                        loadData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // Create new transaction
                    Transaction transaction = new Transaction(userEmail, "INCOME", amount, date, categoryId, description);

                    if (dbHelper.insertTransaction(transaction) != -1) {
                        Toast.makeText(requireContext(), "Income added", Toast.LENGTH_SHORT).show();
                        loadData();
                        dialog.dismiss();
                    } else {
                        Toast.makeText(requireContext(), "Failed to add income", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        });

        dialog.show();
    }

    @Override
    public void onTransactionClick(Transaction transaction) {
        showAddTransactionDialog(transaction);
    }

    @Override
    public void onTransactionLongClick(Transaction transaction) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Transaction")
                .setMessage("Are you sure you want to delete this transaction?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (dbHelper.deleteTransaction(transaction.getId())) {
                        Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show();
                        loadData();
                    } else {
                        Toast.makeText(requireContext(), "Failed to delete", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
