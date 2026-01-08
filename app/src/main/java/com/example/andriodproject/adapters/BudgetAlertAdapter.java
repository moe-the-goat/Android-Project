package com.example.andriodproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodproject.R;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Budget;
import com.example.andriodproject.model.Category;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * BudgetAlertAdapter - RecyclerView adapter for displaying budget alerts
 */
public class BudgetAlertAdapter extends RecyclerView.Adapter<BudgetAlertAdapter.BudgetAlertViewHolder> {

    private Context context;
    private List<Budget> budgets;
    private DataBaseHelper dbHelper;
    private NumberFormat currencyFormat;

    public BudgetAlertAdapter(Context context, List<Budget> budgets, DataBaseHelper dbHelper) {
        this.context = context;
        this.budgets = budgets;
        this.dbHelper = dbHelper;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    @NonNull
    @Override
    public BudgetAlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget_alert, parent, false);
        return new BudgetAlertViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetAlertViewHolder holder, int position) {
        Budget budget = budgets.get(position);

        // Get category name
        Category category = dbHelper.getCategoryById(budget.getCategoryId());
        String categoryName = category != null ? category.getName() : "Unknown";

        holder.tvCategoryName.setText(categoryName);

        // Get spending for this category
        double spent = dbHelper.getSpendingForCategory(
                budget.getUserEmail(), 
                budget.getCategoryId(), 
                budget.getMonth());

        double limit = budget.getBudgetLimit();
        int percentage = (int) ((spent / limit) * 100);

        holder.tvSpent.setText(currencyFormat.format(spent) + " / " + currencyFormat.format(limit));
        holder.progressBar.setProgress(Math.min(percentage, 100));

        // Set progress bar color based on percentage
        if (percentage >= 100) {
            holder.progressBar.setProgressTintList(
                    context.getResources().getColorStateList(R.color.expense_red, null));
            holder.tvStatus.setText("Over budget!");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.expense_red, null));
        } else if (percentage >= budget.getAlertThreshold()) {
            holder.progressBar.setProgressTintList(
                    context.getResources().getColorStateList(R.color.warning, null));
            holder.tvStatus.setText(percentage + "% used");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.warning, null));
        } else {
            holder.progressBar.setProgressTintList(
                    context.getResources().getColorStateList(R.color.income_green, null));
            holder.tvStatus.setText(percentage + "% used");
            holder.tvStatus.setTextColor(context.getResources().getColor(R.color.income_green, null));
        }
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateData(List<Budget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    static class BudgetAlertViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvSpent, tvStatus;
        ProgressBar progressBar;

        public BudgetAlertViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
