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
 * BudgetAdapter - RecyclerView adapter for displaying budgets
 */
public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {

    private Context context;
    private List<Budget> budgets;
    private DataBaseHelper dbHelper;
    private OnBudgetClickListener listener;
    private NumberFormat currencyFormat;

    public interface OnBudgetClickListener {
        void onBudgetClick(Budget budget);
        void onBudgetLongClick(Budget budget);
    }

    public BudgetAdapter(Context context, List<Budget> budgets, DataBaseHelper dbHelper) {
        this.context = context;
        this.budgets = budgets;
        this.dbHelper = dbHelper;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
    }

    public void setOnBudgetClickListener(OnBudgetClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
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
        int percentage = limit > 0 ? (int) ((spent / limit) * 100) : 0;

        holder.tvSpent.setText("Spent: " + currencyFormat.format(spent));
        holder.tvLimit.setText("Limit: " + currencyFormat.format(limit));
        holder.tvPercentage.setText(percentage + "%");
        holder.progressBar.setProgress(Math.min(percentage, 100));

        // Set progress bar color based on percentage
        if (percentage >= 100) {
            holder.progressBar.setProgressTintList(
                    context.getResources().getColorStateList(R.color.expense_red, null));
            holder.tvPercentage.setTextColor(context.getResources().getColor(R.color.expense_red, null));
        } else if (percentage >= budget.getAlertThreshold()) {
            holder.progressBar.setProgressTintList(
                    context.getResources().getColorStateList(R.color.warning, null));
            holder.tvPercentage.setTextColor(context.getResources().getColor(R.color.warning, null));
        } else {
            holder.progressBar.setProgressTintList(
                    context.getResources().getColorStateList(R.color.income_green, null));
            holder.tvPercentage.setTextColor(context.getResources().getColor(R.color.income_green, null));
        }

        // Show alert badge if enabled
        if (budget.isAlertEnabled()) {
            holder.tvAlertBadge.setVisibility(View.VISIBLE);
            holder.tvAlertBadge.setText("Alert at " + (int) budget.getAlertThreshold() + "%");
        } else {
            holder.tvAlertBadge.setVisibility(View.GONE);
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onBudgetClick(budget);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onBudgetLongClick(budget);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return budgets.size();
    }

    public void updateData(List<Budget> newBudgets) {
        this.budgets = newBudgets;
        notifyDataSetChanged();
    }

    static class BudgetViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryName, tvPercentage, tvSpent, tvLimit, tvAlertBadge;
        ProgressBar progressBar;

        public BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvLimit = itemView.findViewById(R.id.tvLimit);
            tvAlertBadge = itemView.findViewById(R.id.tvAlertBadge);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}
