package com.example.andriodproject.fragments;

import android.app.DatePickerDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodproject.MainActivity;
import com.example.andriodproject.R;
import com.example.andriodproject.adapters.BudgetAlertAdapter;
import com.example.andriodproject.adapters.TransactionAdapter;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Budget;
import com.example.andriodproject.model.Category;
import com.example.andriodproject.model.Transaction;
import com.example.andriodproject.utils.SharedPrefManager;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * HomeFragment - Dashboard showing financial summary, balance, recent transactions
 */
public class HomeFragment extends Fragment {

    private TextView tvBalance, tvTotalIncome, tvTotalExpense;
    private MaterialButton btnDaily, btnWeekly, btnMonthly, btnCustom;
    private RecyclerView rvBudgetAlerts, rvRecentTransactions;
    private TextView tvNoBudgetAlerts, tvNoTransactions, tvSeeAll, tvNoChartData;
    private TextView tvNoIncomeExpenseData, tvNoBarChartData;
    private PieChart pieChartExpenses, pieChartIncomeExpense;
    private BarChart barChartMonthlyExpenses;
    private MaterialButton btnGenerateReport;

    private DataBaseHelper dbHelper;
    private SharedPrefManager sharedPrefManager;
    private String userEmail;
    private String currentPeriod = "monthly";
    
    // Custom date range
    private String customStartDate = null;
    private String customEndDate = null;

    private TransactionAdapter transactionAdapter;
    private BudgetAlertAdapter budgetAlertAdapter;
    private List<Transaction> recentTransactions;
    private List<Budget> budgetAlerts;

    private NumberFormat currencyFormat;
    private SimpleDateFormat dateFormat;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Initialize helpers
        dbHelper = new DataBaseHelper(requireContext());
        sharedPrefManager = SharedPrefManager.getInstance(requireContext());
        userEmail = ((MainActivity) requireActivity()).getCurrentUserEmail();

        // Initialize formatters
        currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        // Initialize views
        initViews(view);

        // Setup RecyclerViews
        setupRecyclerViews();

        // Set default period from preferences
        currentPeriod = sharedPrefManager.getDefaultPeriod();
        updatePeriodButtons();

        // Setup click listeners
        setupClickListeners();

        // Load data
        loadData();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh user email in case it changed
        userEmail = ((MainActivity) requireActivity()).getCurrentUserEmail();
        // Re-setup charts for theme changes
        setupPieChart();
        setupIncomeExpensePieChart();
        setupBarChart();
        // Load fresh data
        loadData();
        android.util.Log.d("HomeFragment", "onResume called - data refreshed");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden && isAdded()) {
            // Fragment is now visible, refresh data
            setupPieChart();
            setupIncomeExpensePieChart();
            setupBarChart();
            loadData();
            android.util.Log.d("HomeFragment", "onHiddenChanged - data refreshed");
        }
    }

    private void initViews(View view) {
        tvBalance = view.findViewById(R.id.tvBalance);
        tvTotalIncome = view.findViewById(R.id.tvTotalIncome);
        tvTotalExpense = view.findViewById(R.id.tvTotalExpense);
        btnDaily = view.findViewById(R.id.btnDaily);
        btnWeekly = view.findViewById(R.id.btnWeekly);
        btnMonthly = view.findViewById(R.id.btnMonthly);
        btnCustom = view.findViewById(R.id.btnCustom);
        rvBudgetAlerts = view.findViewById(R.id.rvBudgetAlerts);
        rvRecentTransactions = view.findViewById(R.id.rvRecentTransactions);
        tvNoBudgetAlerts = view.findViewById(R.id.tvNoBudgetAlerts);
        tvNoTransactions = view.findViewById(R.id.tvNoTransactions);
        tvSeeAll = view.findViewById(R.id.tvSeeAll);
        pieChartExpenses = view.findViewById(R.id.pieChartExpenses);
        tvNoChartData = view.findViewById(R.id.tvNoChartData);
        
        // New charts
        pieChartIncomeExpense = view.findViewById(R.id.pieChartIncomeExpense);
        tvNoIncomeExpenseData = view.findViewById(R.id.tvNoIncomeExpenseData);
        barChartMonthlyExpenses = view.findViewById(R.id.barChartMonthlyExpenses);
        tvNoBarChartData = view.findViewById(R.id.tvNoBarChartData);
        btnGenerateReport = view.findViewById(R.id.btnGenerateReport);

        // Setup Charts
        setupPieChart();
        setupIncomeExpensePieChart();
        setupBarChart();
    }

    private void setupRecyclerViews() {
        // Recent Transactions RecyclerView
        recentTransactions = new ArrayList<>();
        transactionAdapter = new TransactionAdapter(requireContext(), recentTransactions, dbHelper);
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvRecentTransactions.setAdapter(transactionAdapter);

        // Budget Alerts RecyclerView
        budgetAlerts = new ArrayList<>();
        budgetAlertAdapter = new BudgetAlertAdapter(requireContext(), budgetAlerts, dbHelper);
        rvBudgetAlerts.setLayoutManager(new LinearLayoutManager(requireContext()));
        rvBudgetAlerts.setAdapter(budgetAlertAdapter);
    }

    private void setupClickListeners() {
        btnDaily.setOnClickListener(v -> {
            currentPeriod = "daily";
            updatePeriodButtons();
            loadData();
        });

        btnWeekly.setOnClickListener(v -> {
            currentPeriod = "weekly";
            updatePeriodButtons();
            loadData();
        });

        btnMonthly.setOnClickListener(v -> {
            currentPeriod = "monthly";
            updatePeriodButtons();
            loadData();
        });

        btnCustom.setOnClickListener(v -> {
            showCustomDateRangeDialog();
        });

        tvSeeAll.setOnClickListener(v -> {
            // Navigate to expenses fragment
            MainActivity mainActivity = (MainActivity) requireActivity();
            NavigationView navView = mainActivity.findViewById(R.id.nav_view);
            mainActivity.onNavigationItemSelected(navView.getMenu().findItem(R.id.nav_expenses));
        });

        btnGenerateReport.setOnClickListener(v -> {
            generateDetailedReport();
        });
    }

    private void showCustomDateRangeDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_custom_date_range, null);
        
        TextInputEditText etStartDate = dialogView.findViewById(R.id.etStartDate);
        TextInputEditText etEndDate = dialogView.findViewById(R.id.etEndDate);
        
        final Calendar startCalendar = Calendar.getInstance();
        final Calendar endCalendar = Calendar.getInstance();
        
        // Set default dates (last 30 days)
        startCalendar.add(Calendar.DAY_OF_YEAR, -30);
        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
        
        // Start date picker
        etStartDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        startCalendar.set(year, month, dayOfMonth);
                        etStartDate.setText(dateFormat.format(startCalendar.getTime()));
                    },
                    startCalendar.get(Calendar.YEAR),
                    startCalendar.get(Calendar.MONTH),
                    startCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        
        // End date picker
        etEndDate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                    (view, year, month, dayOfMonth) -> {
                        endCalendar.set(year, month, dayOfMonth);
                        etEndDate.setText(dateFormat.format(endCalendar.getTime()));
                    },
                    endCalendar.get(Calendar.YEAR),
                    endCalendar.get(Calendar.MONTH),
                    endCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Select Date Range")
                .setView(dialogView)
                .setPositiveButton("Apply", (dialog, which) -> {
                    customStartDate = etStartDate.getText().toString();
                    customEndDate = etEndDate.getText().toString();
                    
                    // Validate dates
                    if (customStartDate.compareTo(customEndDate) > 0) {
                        android.widget.Toast.makeText(requireContext(), 
                                "Start date must be before end date", 
                                android.widget.Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    currentPeriod = "custom";
                    updatePeriodButtons();
                    loadData();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updatePeriodButtons() {
        // Reset all buttons
        btnDaily.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        btnWeekly.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        btnMonthly.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));
        btnCustom.setBackgroundColor(getResources().getColor(android.R.color.transparent, null));

        btnDaily.setTextColor(getResources().getColor(R.color.text_secondary, null));
        btnWeekly.setTextColor(getResources().getColor(R.color.text_secondary, null));
        btnMonthly.setTextColor(getResources().getColor(R.color.text_secondary, null));
        btnCustom.setTextColor(getResources().getColor(R.color.text_secondary, null));

        // Highlight selected button
        MaterialButton selectedBtn;
        switch (currentPeriod) {
            case "daily":
                selectedBtn = btnDaily;
                break;
            case "weekly":
                selectedBtn = btnWeekly;
                break;
            case "custom":
                selectedBtn = btnCustom;
                break;
            default:
                selectedBtn = btnMonthly;
                break;
        }
        selectedBtn.setBackgroundColor(getResources().getColor(R.color.primary_light, null));
        selectedBtn.setTextColor(getResources().getColor(R.color.primary, null));
    }

    private void loadData() {
        // Calculate date range based on period
        Calendar calendar = Calendar.getInstance();
        String endDate = dateFormat.format(calendar.getTime());
        String startDate;

        switch (currentPeriod) {
            case "daily":
                startDate = endDate;
                break;
            case "weekly":
                calendar.add(Calendar.DAY_OF_YEAR, -6);
                startDate = dateFormat.format(calendar.getTime());
                break;
            case "custom":
                if (customStartDate != null && customEndDate != null) {
                    startDate = customStartDate;
                    endDate = customEndDate;
                } else {
                    // Default to monthly if custom dates not set
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    startDate = dateFormat.format(calendar.getTime());
                }
                break;
            default: // monthly
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                break;
        }

        // Debug log for dates
        android.util.Log.d("HomeFragment", "Period: " + currentPeriod + ", Start: " + startDate + ", End: " + endDate);

        // Load financial summary
        double totalIncome = dbHelper.getTotalIncome(userEmail, startDate, endDate);
        double totalExpense = dbHelper.getTotalExpense(userEmail, startDate, endDate);
        double balance = totalIncome - totalExpense;

        android.util.Log.d("HomeFragment", "Income: " + totalIncome + ", Expense: " + totalExpense);

        tvBalance.setText(currencyFormat.format(balance));
        tvTotalIncome.setText(currencyFormat.format(totalIncome));
        tvTotalExpense.setText(currencyFormat.format(totalExpense));

        // Load recent transactions (limit to 5)
        List<Transaction> allTransactions = dbHelper.getTransactionsByDateRange(userEmail, startDate, endDate);
        recentTransactions.clear();
        int limit = Math.min(allTransactions.size(), 5);
        for (int i = 0; i < limit; i++) {
            recentTransactions.add(allTransactions.get(i));
        }
        transactionAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (recentTransactions.isEmpty()) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvRecentTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvRecentTransactions.setVisibility(View.VISIBLE);
        }

        // Load expense pie chart
        loadExpenseChart(startDate, endDate);

        // Load income vs expense pie chart
        loadIncomeExpenseChart(totalIncome, totalExpense);

        // Load monthly bar chart
        loadMonthlyBarChart();

        // Load budget alerts
        loadBudgetAlerts();
    }

    private void loadBudgetAlerts() {
        Calendar calendar = Calendar.getInstance();
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(calendar.getTime());

        List<Budget> allBudgets = dbHelper.getBudgetsByMonth(userEmail, currentMonth);
        budgetAlerts.clear();

        for (Budget budget : allBudgets) {
            if (budget.isAlertEnabled()) {
                double spent = dbHelper.getSpendingForCategory(userEmail, budget.getCategoryId(), currentMonth);
                double threshold = budget.getBudgetLimit() * (budget.getAlertThreshold() / 100);
                
                if (spent >= threshold) {
                    budgetAlerts.add(budget);
                }
            }
        }

        budgetAlertAdapter.notifyDataSetChanged();

        // Show/hide empty state
        if (budgetAlerts.isEmpty()) {
            tvNoBudgetAlerts.setVisibility(View.VISIBLE);
            rvBudgetAlerts.setVisibility(View.GONE);
        } else {
            tvNoBudgetAlerts.setVisibility(View.GONE);
            rvBudgetAlerts.setVisibility(View.VISIBLE);
        }
    }

    private void setupPieChart() {
        pieChartExpenses.setUsePercentValues(true);
        pieChartExpenses.getDescription().setEnabled(false);
        pieChartExpenses.setExtraOffsets(5, 10, 5, 5);
        pieChartExpenses.setDragDecelerationFrictionCoef(0.95f);

        pieChartExpenses.setDrawHoleEnabled(true);
        
        // Get theme-aware colors for dark mode support
        boolean isDarkMode = sharedPrefManager.isDarkModeEnabled();
        int holeColor = isDarkMode ? getResources().getColor(R.color.dark_surface, null) : Color.WHITE;
        int textColor = isDarkMode ? Color.WHITE : getResources().getColor(R.color.text_primary, null);
        
        pieChartExpenses.setHoleColor(holeColor);
        pieChartExpenses.setTransparentCircleColor(holeColor);
        pieChartExpenses.setTransparentCircleAlpha(110);
        pieChartExpenses.setHoleRadius(58f);
        pieChartExpenses.setTransparentCircleRadius(61f);

        pieChartExpenses.setDrawCenterText(true);
        pieChartExpenses.setCenterText("Expenses");
        pieChartExpenses.setCenterTextSize(14f);
        pieChartExpenses.setCenterTextColor(textColor);

        pieChartExpenses.setRotationAngle(0);
        pieChartExpenses.setRotationEnabled(true);
        pieChartExpenses.setHighlightPerTapEnabled(true);

        // Legend setup
        Legend legend = pieChartExpenses.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setXEntrySpace(7f);
        legend.setYEntrySpace(0f);
        legend.setYOffset(0f);
        legend.setTextColor(textColor);

        pieChartExpenses.setEntryLabelColor(Color.WHITE);
        pieChartExpenses.setEntryLabelTextSize(10f);
    }

    private void loadExpenseChart(String startDate, String endDate) {
        // Get expense categories for user
        List<Category> expenseCategories = dbHelper.getCategoriesByType("EXPENSE", userEmail);
        android.util.Log.d("HomeFragment", "Found " + expenseCategories.size() + " expense categories");
        
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        // Chart colors
        int[] chartColors = {
            getResources().getColor(R.color.chart_color_1, null),
            getResources().getColor(R.color.chart_color_2, null),
            getResources().getColor(R.color.chart_color_3, null),
            getResources().getColor(R.color.chart_color_4, null),
            getResources().getColor(R.color.chart_color_5, null),
            getResources().getColor(R.color.chart_color_6, null),
            getResources().getColor(R.color.chart_color_7, null),
            getResources().getColor(R.color.chart_color_8, null)
        };

        int colorIndex = 0;
        double totalExpense = 0;

        for (Category category : expenseCategories) {
            double amount = dbHelper.getSpendingForCategoryInRange(userEmail, category.getId(), startDate, endDate);
            android.util.Log.d("HomeFragment", "Category: " + category.getName() + ", Amount: " + amount);
            if (amount > 0) {
                entries.add(new PieEntry((float) amount, category.getName()));
                colors.add(chartColors[colorIndex % chartColors.length]);
                colorIndex++;
                totalExpense += amount;
            }
        }

        android.util.Log.d("HomeFragment", "Chart entries: " + entries.size() + ", Total expense: " + totalExpense);

        if (entries.isEmpty()) {
            pieChartExpenses.clear();
            pieChartExpenses.setVisibility(View.GONE);
            tvNoChartData.setVisibility(View.VISIBLE);
        } else {
            pieChartExpenses.setVisibility(View.VISIBLE);
            tvNoChartData.setVisibility(View.GONE);

            PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(5f);
            dataSet.setColors(colors);
            dataSet.setDrawValues(true);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter(pieChartExpenses));
            data.setValueTextSize(11f);
            data.setValueTextColor(Color.WHITE);

            pieChartExpenses.setData(data);
            pieChartExpenses.setCenterText("Expenses\n" + currencyFormat.format(totalExpense));
            pieChartExpenses.notifyDataSetChanged();
            pieChartExpenses.invalidate();
            pieChartExpenses.animateY(1000);
            
            android.util.Log.d("HomeFragment", "Chart data set and animated");
        }
    }

    private void setupIncomeExpensePieChart() {
        pieChartIncomeExpense.setUsePercentValues(true);
        pieChartIncomeExpense.getDescription().setEnabled(false);
        pieChartIncomeExpense.setExtraOffsets(5, 10, 5, 5);
        pieChartIncomeExpense.setDragDecelerationFrictionCoef(0.95f);

        pieChartIncomeExpense.setDrawHoleEnabled(true);

        boolean isDarkMode = sharedPrefManager.isDarkModeEnabled();
        int holeColor = isDarkMode ? getResources().getColor(R.color.dark_surface, null) : Color.WHITE;
        int textColor = isDarkMode ? Color.WHITE : getResources().getColor(R.color.text_primary, null);

        pieChartIncomeExpense.setHoleColor(holeColor);
        pieChartIncomeExpense.setTransparentCircleColor(holeColor);
        pieChartIncomeExpense.setTransparentCircleAlpha(110);
        pieChartIncomeExpense.setHoleRadius(58f);
        pieChartIncomeExpense.setTransparentCircleRadius(61f);

        pieChartIncomeExpense.setDrawCenterText(true);
        pieChartIncomeExpense.setCenterText("Balance");
        pieChartIncomeExpense.setCenterTextSize(14f);
        pieChartIncomeExpense.setCenterTextColor(textColor);

        pieChartIncomeExpense.setRotationAngle(0);
        pieChartIncomeExpense.setRotationEnabled(true);
        pieChartIncomeExpense.setHighlightPerTapEnabled(true);

        Legend legend = pieChartIncomeExpense.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setDrawInside(false);
        legend.setTextColor(textColor);

        pieChartIncomeExpense.setEntryLabelColor(Color.WHITE);
        pieChartIncomeExpense.setEntryLabelTextSize(10f);
    }

    private void loadIncomeExpenseChart(double totalIncome, double totalExpense) {
        ArrayList<PieEntry> entries = new ArrayList<>();
        ArrayList<Integer> colors = new ArrayList<>();

        if (totalIncome > 0) {
            entries.add(new PieEntry((float) totalIncome, "Income"));
            colors.add(getResources().getColor(R.color.income_green, null));
        }
        if (totalExpense > 0) {
            entries.add(new PieEntry((float) totalExpense, "Expenses"));
            colors.add(getResources().getColor(R.color.expense_red, null));
        }

        if (entries.isEmpty()) {
            pieChartIncomeExpense.clear();
            pieChartIncomeExpense.setVisibility(View.GONE);
            tvNoIncomeExpenseData.setVisibility(View.VISIBLE);
        } else {
            pieChartIncomeExpense.setVisibility(View.VISIBLE);
            tvNoIncomeExpenseData.setVisibility(View.GONE);

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setSliceSpace(3f);
            dataSet.setSelectionShift(5f);
            dataSet.setColors(colors);
            dataSet.setDrawValues(true);

            PieData data = new PieData(dataSet);
            data.setValueFormatter(new PercentFormatter(pieChartIncomeExpense));
            data.setValueTextSize(12f);
            data.setValueTextColor(Color.WHITE);

            pieChartIncomeExpense.setData(data);
            double balance = totalIncome - totalExpense;
            pieChartIncomeExpense.setCenterText("Balance\n" + currencyFormat.format(balance));
            pieChartIncomeExpense.notifyDataSetChanged();
            pieChartIncomeExpense.invalidate();
            pieChartIncomeExpense.animateY(1000);
        }
    }

    private void setupBarChart() {
        barChartMonthlyExpenses.getDescription().setEnabled(false);
        barChartMonthlyExpenses.setDrawGridBackground(false);
        barChartMonthlyExpenses.setDrawBarShadow(false);
        barChartMonthlyExpenses.setDrawValueAboveBar(true);
        barChartMonthlyExpenses.setPinchZoom(false);
        barChartMonthlyExpenses.setScaleEnabled(false);

        boolean isDarkMode = sharedPrefManager.isDarkModeEnabled();
        int textColor = isDarkMode ? Color.WHITE : getResources().getColor(R.color.text_primary, null);

        // X-axis setup
        XAxis xAxis = barChartMonthlyExpenses.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(textColor);

        // Left Y-axis
        YAxis leftAxis = barChartMonthlyExpenses.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setTextColor(textColor);
        leftAxis.setAxisMinimum(0f);

        // Right Y-axis
        barChartMonthlyExpenses.getAxisRight().setEnabled(false);

        // Legend
        Legend legend = barChartMonthlyExpenses.getLegend();
        legend.setEnabled(false);
    }

    private void loadMonthlyBarChart() {
        ArrayList<BarEntry> entries = new ArrayList<>();
        ArrayList<String> months = new ArrayList<>();
        
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat monthFormat = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat dbMonthFormat = new SimpleDateFormat("yyyy-MM", Locale.getDefault());

        // Get last 6 months of data
        boolean hasData = false;
        for (int i = 5; i >= 0; i--) {
            Calendar tempCal = (Calendar) calendar.clone();
            tempCal.add(Calendar.MONTH, -i);
            
            String monthLabel = monthFormat.format(tempCal.getTime());
            String monthKey = dbMonthFormat.format(tempCal.getTime());
            
            months.add(monthLabel);
            
            // Calculate start and end of month
            tempCal.set(Calendar.DAY_OF_MONTH, 1);
            String startDate = dateFormat.format(tempCal.getTime());
            tempCal.set(Calendar.DAY_OF_MONTH, tempCal.getActualMaximum(Calendar.DAY_OF_MONTH));
            String endDate = dateFormat.format(tempCal.getTime());
            
            double expense = dbHelper.getTotalExpense(userEmail, startDate, endDate);
            entries.add(new BarEntry(5 - i, (float) expense));
            
            if (expense > 0) hasData = true;
        }

        if (!hasData) {
            barChartMonthlyExpenses.clear();
            barChartMonthlyExpenses.setVisibility(View.GONE);
            tvNoBarChartData.setVisibility(View.VISIBLE);
        } else {
            barChartMonthlyExpenses.setVisibility(View.VISIBLE);
            tvNoBarChartData.setVisibility(View.GONE);

            BarDataSet dataSet = new BarDataSet(entries, "Monthly Expenses");
            dataSet.setColor(getResources().getColor(R.color.expense_red, null));
            dataSet.setValueTextSize(10f);
            
            boolean isDarkMode = sharedPrefManager.isDarkModeEnabled();
            int textColor = isDarkMode ? Color.WHITE : getResources().getColor(R.color.text_primary, null);
            dataSet.setValueTextColor(textColor);

            BarData data = new BarData(dataSet);
            data.setBarWidth(0.7f);

            XAxis xAxis = barChartMonthlyExpenses.getXAxis();
            xAxis.setValueFormatter(new IndexAxisValueFormatter(months));
            xAxis.setLabelCount(months.size());

            barChartMonthlyExpenses.setData(data);
            barChartMonthlyExpenses.notifyDataSetChanged();
            barChartMonthlyExpenses.invalidate();
            barChartMonthlyExpenses.animateY(1000);
        }
    }

    private void generateDetailedReport() {
        // Calculate date range based on current period
        Calendar calendar = Calendar.getInstance();
        String endDate = dateFormat.format(calendar.getTime());
        String startDate;
        String periodLabel;

        switch (currentPeriod) {
            case "daily":
                startDate = endDate;
                periodLabel = "Today";
                break;
            case "weekly":
                calendar.add(Calendar.DAY_OF_YEAR, -6);
                startDate = dateFormat.format(calendar.getTime());
                periodLabel = "This Week";
                break;
            case "custom":
                if (customStartDate != null && customEndDate != null) {
                    startDate = customStartDate;
                    endDate = customEndDate;
                    periodLabel = customStartDate + " to " + customEndDate;
                } else {
                    calendar.set(Calendar.DAY_OF_MONTH, 1);
                    startDate = dateFormat.format(calendar.getTime());
                    periodLabel = "This Month";
                }
                break;
            default:
                calendar.set(Calendar.DAY_OF_MONTH, 1);
                startDate = dateFormat.format(calendar.getTime());
                periodLabel = "This Month";
                break;
        }

        // Gather report data
        double totalIncome = dbHelper.getTotalIncome(userEmail, startDate, endDate);
        double totalExpense = dbHelper.getTotalExpense(userEmail, startDate, endDate);
        double balance = totalIncome - totalExpense;
        
        List<Category> incomeCategories = dbHelper.getCategoriesByType("INCOME", userEmail);
        List<Category> expenseCategories = dbHelper.getCategoriesByType("EXPENSE", userEmail);

        // Build report content
        StringBuilder report = new StringBuilder();
        report.append("📊 FINANCIAL REPORT\n");
        report.append("Period: ").append(periodLabel).append("\n\n");
        
        report.append("━━━ SUMMARY ━━━\n");
        report.append("💰 Total Income: ").append(currencyFormat.format(totalIncome)).append("\n");
        report.append("💸 Total Expenses: ").append(currencyFormat.format(totalExpense)).append("\n");
        report.append("📈 Net Balance: ").append(currencyFormat.format(balance)).append("\n\n");

        report.append("━━━ INCOME BY CATEGORY ━━━\n");
        boolean hasIncomeData = false;
        for (Category category : incomeCategories) {
            double amount = dbHelper.getSpendingForCategoryInRange(userEmail, category.getId(), startDate, endDate);
            if (amount > 0) {
                report.append("• ").append(category.getName()).append(": ").append(currencyFormat.format(amount)).append("\n");
                hasIncomeData = true;
            }
        }
        if (!hasIncomeData) report.append("No income recorded\n");
        report.append("\n");

        report.append("━━━ EXPENSES BY CATEGORY ━━━\n");
        boolean hasExpenseData = false;
        for (Category category : expenseCategories) {
            double amount = dbHelper.getSpendingForCategoryInRange(userEmail, category.getId(), startDate, endDate);
            if (amount > 0) {
                double percentage = (totalExpense > 0) ? (amount / totalExpense * 100) : 0;
                report.append("• ").append(category.getName()).append(": ")
                      .append(currencyFormat.format(amount))
                      .append(" (").append(String.format("%.1f%%", percentage)).append(")\n");
                hasExpenseData = true;
            }
        }
        if (!hasExpenseData) report.append("No expenses recorded\n");
        report.append("\n");

        // Check budget status
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Calendar.getInstance().getTime());
        List<Budget> budgets = dbHelper.getBudgetsByMonth(userEmail, currentMonth);
        
        if (!budgets.isEmpty()) {
            report.append("━━━ BUDGET STATUS ━━━\n");
            for (Budget budget : budgets) {
                double spent = dbHelper.getSpendingForCategory(userEmail, budget.getCategoryId(), currentMonth);
                double remaining = budget.getBudgetLimit() - spent;
                String status = remaining >= 0 ? "✅" : "⚠️ OVER";
                String categoryName = dbHelper.getCategoryNameById(budget.getCategoryId());
                report.append("• ").append(categoryName).append(": ")
                      .append(currencyFormat.format(spent)).append(" / ")
                      .append(currencyFormat.format(budget.getBudgetLimit()))
                      .append(" ").append(status).append("\n");
            }
        }

        // Show report dialog
        new AlertDialog.Builder(requireContext())
                .setTitle("Financial Report")
                .setMessage(report.toString())
                .setPositiveButton("Share", (dialog, which) -> {
                    android.content.Intent shareIntent = new android.content.Intent(android.content.Intent.ACTION_SEND);
                    shareIntent.setType("text/plain");
                    shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Financial Report - " + periodLabel);
                    shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, report.toString());
                    startActivity(android.content.Intent.createChooser(shareIntent, "Share Report"));
                })
                .setNegativeButton("Close", null)
                .show();
    }
}
