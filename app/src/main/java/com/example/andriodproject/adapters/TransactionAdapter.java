package com.example.andriodproject.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.andriodproject.R;
import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.model.Category;
import com.example.andriodproject.model.Transaction;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Adapter for displaying transactions in a RecyclerView
public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<Transaction> transactions;
    private DataBaseHelper dbHelper;
    private OnTransactionClickListener listener;

    private NumberFormat currencyFormat;
    private SimpleDateFormat inputFormat;
    private SimpleDateFormat outputFormat;

    public interface OnTransactionClickListener {
        void onTransactionClick(Transaction transaction);
        void onTransactionLongClick(Transaction transaction);
    }

    public TransactionAdapter(Context context, List<Transaction> transactions, DataBaseHelper dbHelper) {
        this.context = context;
        this.transactions = transactions;
        this.dbHelper = dbHelper;
        this.currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);
        this.inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        this.outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
    }

    public void setOnTransactionClickListener(OnTransactionClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Get category name
        Category category = dbHelper.getCategoryById(transaction.getCategoryId());
        String categoryName = category != null ? category.getName() : "Unknown";

        holder.tvCategory.setText(categoryName);
        holder.tvDescription.setText(transaction.getDescription() != null && !transaction.getDescription().isEmpty() 
                ? transaction.getDescription() : categoryName);

        // Format date
        try {
            Date date = inputFormat.parse(transaction.getDate());
            holder.tvDate.setText(outputFormat.format(date));
        } catch (ParseException e) {
            holder.tvDate.setText(transaction.getDate());
        }

        // Format amount with color based on type
        String amountText = currencyFormat.format(transaction.getAmount());
        if (transaction.getType().equals("INCOME")) {
            holder.tvAmount.setText("+" + amountText);
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.income_green, null));
            holder.ivIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.ivIcon.setColorFilter(context.getResources().getColor(R.color.income_green, null));
        } else {
            holder.tvAmount.setText("-" + amountText);
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.expense_red, null));
            holder.ivIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.ivIcon.setColorFilter(context.getResources().getColor(R.color.expense_red, null));
        }

        // Click listeners
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onTransactionClick(transaction);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (listener != null) {
                listener.onTransactionLongClick(transaction);
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public void updateData(List<Transaction> newTransactions) {
        this.transactions = newTransactions;
        notifyDataSetChanged();
    }

    static class TransactionViewHolder extends RecyclerView.ViewHolder {
        ImageView ivIcon;
        TextView tvCategory, tvDescription, tvDate, tvAmount;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            ivIcon = itemView.findViewById(R.id.ivIcon);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvAmount = itemView.findViewById(R.id.tvAmount);
        }
    }
}
