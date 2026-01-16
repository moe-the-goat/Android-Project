package com.example.andriodproject.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.andriodproject.model.Budget;
import com.example.andriodproject.model.Category;
import com.example.andriodproject.model.Transaction;
import com.example.andriodproject.model.User;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "FinanceManager.db";
    private static final int DATABASE_VERSION = 1;

    // Table Names
    private static final String TABLE_USER = "USER";
    private static final String TABLE_TRANSACTION = "TRANSACTIONS";
    private static final String TABLE_CATEGORY = "CATEGORY";
    private static final String TABLE_BUDGET = "BUDGET";

    // User Table Columns
    private static final String USER_EMAIL = "EMAIL";
    private static final String USER_FIRST_NAME = "FIRST_NAME";
    private static final String USER_LAST_NAME = "LAST_NAME";
    private static final String USER_PASSWORD = "PASSWORD";

    // Transaction Table Columns
    private static final String TRANS_ID = "ID";
    private static final String TRANS_USER_EMAIL = "USER_EMAIL";
    private static final String TRANS_TYPE = "TYPE";
    private static final String TRANS_AMOUNT = "AMOUNT";
    private static final String TRANS_DATE = "DATE";
    private static final String TRANS_CATEGORY_ID = "CATEGORY_ID";
    private static final String TRANS_DESCRIPTION = "DESCRIPTION";

    // Category Table Columns
    private static final String CAT_ID = "ID";
    private static final String CAT_NAME = "NAME";
    private static final String CAT_TYPE = "TYPE";
    private static final String CAT_USER_EMAIL = "USER_EMAIL";

    // Budget Table Columns
    private static final String BUDGET_ID = "ID";
    private static final String BUDGET_USER_EMAIL = "USER_EMAIL";
    private static final String BUDGET_CATEGORY_ID = "CATEGORY_ID";
    private static final String BUDGET_LIMIT = "BUDGET_LIMIT";
    private static final String BUDGET_MONTH = "MONTH";
    private static final String BUDGET_ALERT_ENABLED = "ALERT_ENABLED";
    private static final String BUDGET_ALERT_THRESHOLD = "ALERT_THRESHOLD";

    // Create Table Queries
    private static final String CREATE_USER_TABLE = 
        "CREATE TABLE " + TABLE_USER + " (" +
        USER_EMAIL + " TEXT PRIMARY KEY, " +
        USER_FIRST_NAME + " TEXT NOT NULL, " +
        USER_LAST_NAME + " TEXT NOT NULL, " +
        USER_PASSWORD + " TEXT NOT NULL)";

    private static final String CREATE_TRANSACTION_TABLE = 
        "CREATE TABLE " + TABLE_TRANSACTION + " (" +
        TRANS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        TRANS_USER_EMAIL + " TEXT NOT NULL, " +
        TRANS_TYPE + " TEXT NOT NULL, " +
        TRANS_AMOUNT + " REAL NOT NULL, " +
        TRANS_DATE + " TEXT NOT NULL, " +
        TRANS_CATEGORY_ID + " INTEGER NOT NULL, " +
        TRANS_DESCRIPTION + " TEXT, " +
        "FOREIGN KEY(" + TRANS_USER_EMAIL + ") REFERENCES " + TABLE_USER + "(" + USER_EMAIL + "), " +
        "FOREIGN KEY(" + TRANS_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + CAT_ID + "))";

    private static final String CREATE_CATEGORY_TABLE = 
        "CREATE TABLE " + TABLE_CATEGORY + " (" +
        CAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        CAT_NAME + " TEXT NOT NULL, " +
        CAT_TYPE + " TEXT NOT NULL, " +
        CAT_USER_EMAIL + " TEXT)";

    private static final String CREATE_BUDGET_TABLE = 
        "CREATE TABLE " + TABLE_BUDGET + " (" +
        BUDGET_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
        BUDGET_USER_EMAIL + " TEXT NOT NULL, " +
        BUDGET_CATEGORY_ID + " INTEGER NOT NULL, " +
        BUDGET_LIMIT + " REAL NOT NULL, " +
        BUDGET_MONTH + " TEXT NOT NULL, " +
        BUDGET_ALERT_ENABLED + " INTEGER DEFAULT 1, " +
        BUDGET_ALERT_THRESHOLD + " REAL DEFAULT 50.0, " +
        "FOREIGN KEY(" + BUDGET_USER_EMAIL + ") REFERENCES " + TABLE_USER + "(" + USER_EMAIL + "), " +
        "FOREIGN KEY(" + BUDGET_CATEGORY_ID + ") REFERENCES " + TABLE_CATEGORY + "(" + CAT_ID + "))";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USER_TABLE);
        db.execSQL(CREATE_CATEGORY_TABLE);
        db.execSQL(CREATE_TRANSACTION_TABLE);
        db.execSQL(CREATE_BUDGET_TABLE);
        
        // Insert default categories
        insertDefaultCategories(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        onCreate(db);
    }

    /**
     * Insert default categories for income and expenses
     */
    private void insertDefaultCategories(SQLiteDatabase db) {
        // Default Income Categories
        String[] incomeCategories = {"Salary", "Scholarship", "Freelance", "Investments", "Gifts", "Other Income"};
        for (String cat : incomeCategories) {
            ContentValues values = new ContentValues();
            values.put(CAT_NAME, cat);
            values.put(CAT_TYPE, "INCOME");
            values.putNull(CAT_USER_EMAIL);
            db.insert(TABLE_CATEGORY, null, values);
        }

        // Default Expense Categories
        String[] expenseCategories = {"Food", "Bills", "Rent", "Transportation", "Entertainment", 
                                       "Shopping", "Healthcare", "Education", "Groceries", "Other Expense"};
        for (String cat : expenseCategories) {
            ContentValues values = new ContentValues();
            values.put(CAT_NAME, cat);
            values.put(CAT_TYPE, "EXPENSE");
            values.putNull(CAT_USER_EMAIL);
            db.insert(TABLE_CATEGORY, null, values);
        }
    }

    // ===================== USER OPERATIONS =====================

    /**
     * Insert a new user into the database
     */
    public boolean insertUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_EMAIL, user.getEmail());
        values.put(USER_FIRST_NAME, user.getFirstName());
        values.put(USER_LAST_NAME, user.getLastName());
        values.put(USER_PASSWORD, user.getPassword());
        long result = db.insert(TABLE_USER, null, values);
        return result != -1;
    }

    /**
     * Check if user exists by email
     */
    public boolean userExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + USER_EMAIL + " = ?", 
                                    new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Authenticate user with email and password
     */
    public boolean authenticateUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + 
                                    USER_EMAIL + " = ? AND " + USER_PASSWORD + " = ?", 
                                    new String[]{email, password});
        boolean valid = cursor.getCount() > 0;
        cursor.close();
        return valid;
    }

    /**
     * Get user by email
     */
    public User getUser(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USER + " WHERE " + USER_EMAIL + " = ?", 
                                    new String[]{email});
        User user = null;
        if (cursor.moveToFirst()) {
            user = new User();
            user.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(USER_EMAIL)));
            user.setFirstName(cursor.getString(cursor.getColumnIndexOrThrow(USER_FIRST_NAME)));
            user.setLastName(cursor.getString(cursor.getColumnIndexOrThrow(USER_LAST_NAME)));
            user.setPassword(cursor.getString(cursor.getColumnIndexOrThrow(USER_PASSWORD)));
        }
        cursor.close();
        return user;
    }

    /**
     * Update user profile (first name, last name)
     */
    public boolean updateUserProfile(String email, String firstName, String lastName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_FIRST_NAME, firstName);
        values.put(USER_LAST_NAME, lastName);
        int rowsAffected = db.update(TABLE_USER, values, USER_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0;
    }

    /**
     * Update user password
     */
    public boolean updateUserPassword(String email, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_PASSWORD, newPassword);
        int rowsAffected = db.update(TABLE_USER, values, USER_EMAIL + " = ?", new String[]{email});
        return rowsAffected > 0;
    }

    // ===================== CATEGORY OPERATIONS =====================

    /**
     * Insert a new category
     */
    public long insertCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CAT_NAME, category.getName());
        values.put(CAT_TYPE, category.getType());
        values.put(CAT_USER_EMAIL, category.getUserEmail());
        return db.insert(TABLE_CATEGORY, null, values);
    }

    /**
     * Get all categories by type (INCOME or EXPENSE)
     * Returns both default categories (userEmail is null) and user-specific categories
     */
    public List<Category> getCategoriesByType(String type, String userEmail) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_CATEGORY + " WHERE " + CAT_TYPE + " = ? AND (" + 
            CAT_USER_EMAIL + " IS NULL OR " + CAT_USER_EMAIL + " = ?)",
            new String[]{type, userEmail});
        
        while (cursor.moveToNext()) {
            Category category = new Category();
            category.setId(cursor.getLong(cursor.getColumnIndexOrThrow(CAT_ID)));
            category.setName(cursor.getString(cursor.getColumnIndexOrThrow(CAT_NAME)));
            category.setType(cursor.getString(cursor.getColumnIndexOrThrow(CAT_TYPE)));
            int emailIndex = cursor.getColumnIndexOrThrow(CAT_USER_EMAIL);
            category.setUserEmail(cursor.isNull(emailIndex) ? null : cursor.getString(emailIndex));
            categories.add(category);
        }
        cursor.close();
        return categories;
    }

    /**
     * Get category by ID
     */
    public Category getCategoryById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY + " WHERE " + CAT_ID + " = ?", 
                                    new String[]{String.valueOf(id)});
        Category category = null;
        if (cursor.moveToFirst()) {
            category = new Category();
            category.setId(cursor.getLong(cursor.getColumnIndexOrThrow(CAT_ID)));
            category.setName(cursor.getString(cursor.getColumnIndexOrThrow(CAT_NAME)));
            category.setType(cursor.getString(cursor.getColumnIndexOrThrow(CAT_TYPE)));
            int emailIndex = cursor.getColumnIndexOrThrow(CAT_USER_EMAIL);
            category.setUserEmail(cursor.isNull(emailIndex) ? null : cursor.getString(emailIndex));
        }
        cursor.close();
        return category;
    }

    /**
     * Get category name by ID
     */
    public String getCategoryNameById(long id) {
        Category category = getCategoryById(id);
        return category != null ? category.getName() : "Unknown";
    }

    /**
     * Update category
     */
    public boolean updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(CAT_NAME, category.getName());
        int rowsAffected = db.update(TABLE_CATEGORY, values, CAT_ID + " = ?", 
                                     new String[]{String.valueOf(category.getId())});
        return rowsAffected > 0;
    }

    /**
     * Delete category (only user-created categories can be deleted)
     */
    public boolean deleteCategory(long categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_CATEGORY, CAT_ID + " = ? AND " + CAT_USER_EMAIL + " IS NOT NULL", 
                                     new String[]{String.valueOf(categoryId)});
        return rowsAffected > 0;
    }

    // ===================== TRANSACTION OPERATIONS =====================

    /**
     * Insert a new transaction
     */
    public long insertTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRANS_USER_EMAIL, transaction.getUserEmail());
        values.put(TRANS_TYPE, transaction.getType());
        values.put(TRANS_AMOUNT, transaction.getAmount());
        values.put(TRANS_DATE, transaction.getDate());
        values.put(TRANS_CATEGORY_ID, transaction.getCategoryId());
        values.put(TRANS_DESCRIPTION, transaction.getDescription());
        return db.insert(TABLE_TRANSACTION, null, values);
    }

    /**
     * Get all transactions for a user, sorted by date (newest first)
     */
    public List<Transaction> getAllTransactions(String userEmail) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANS_USER_EMAIL + " = ? ORDER BY " + 
            TRANS_DATE + " DESC", new String[]{userEmail});
        
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }
        cursor.close();
        return transactions;
    }

    /**
     * Get transactions by type (INCOME or EXPENSE) for a user
     */
    public List<Transaction> getTransactionsByType(String userEmail, String type) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANS_USER_EMAIL + " = ? AND " + 
            TRANS_TYPE + " = ? ORDER BY " + TRANS_DATE + " DESC", 
            new String[]{userEmail, type});
        
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }
        cursor.close();
        return transactions;
    }

    /**
     * Get transactions within a date range
     */
    public List<Transaction> getTransactionsByDateRange(String userEmail, String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANS_USER_EMAIL + " = ? AND " + 
            TRANS_DATE + " >= ? AND " + TRANS_DATE + " <= ? ORDER BY " + TRANS_DATE + " DESC", 
            new String[]{userEmail, startDate, endDate});
        
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }
        cursor.close();
        return transactions;
    }

    /**
     * Get transactions by type within a date range
     */
    public List<Transaction> getTransactionsByTypeAndDateRange(String userEmail, String type, 
                                                                String startDate, String endDate) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANS_USER_EMAIL + " = ? AND " + 
            TRANS_TYPE + " = ? AND " + TRANS_DATE + " >= ? AND " + TRANS_DATE + " <= ? ORDER BY " + 
            TRANS_DATE + " DESC", 
            new String[]{userEmail, type, startDate, endDate});
        
        while (cursor.moveToNext()) {
            transactions.add(cursorToTransaction(cursor));
        }
        cursor.close();
        return transactions;
    }

    /**
     * Get transaction by ID
     */
    public Transaction getTransactionById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_TRANSACTION + " WHERE " + TRANS_ID + " = ?", 
                                    new String[]{String.valueOf(id)});
        Transaction transaction = null;
        if (cursor.moveToFirst()) {
            transaction = cursorToTransaction(cursor);
        }
        cursor.close();
        return transaction;
    }

    /**
     * Update transaction
     */
    public boolean updateTransaction(Transaction transaction) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TRANS_AMOUNT, transaction.getAmount());
        values.put(TRANS_DATE, transaction.getDate());
        values.put(TRANS_CATEGORY_ID, transaction.getCategoryId());
        values.put(TRANS_DESCRIPTION, transaction.getDescription());
        int rowsAffected = db.update(TABLE_TRANSACTION, values, TRANS_ID + " = ?", 
                                     new String[]{String.valueOf(transaction.getId())});
        return rowsAffected > 0;
    }

    /**
     * Delete transaction
     */
    public boolean deleteTransaction(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_TRANSACTION, TRANS_ID + " = ?", 
                                     new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    /**
     * Get total income for a user within a date range
     */
    public double getTotalIncome(String userEmail, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " WHERE " + 
            TRANS_USER_EMAIL + " = ? AND " + TRANS_TYPE + " = 'INCOME' AND " + 
            TRANS_DATE + " >= ? AND " + TRANS_DATE + " <= ?", 
            new String[]{userEmail, startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Get total expense for a user within a date range
     */
    public double getTotalExpense(String userEmail, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " WHERE " + 
            TRANS_USER_EMAIL + " = ? AND " + TRANS_TYPE + " = 'EXPENSE' AND " + 
            TRANS_DATE + " >= ? AND " + TRANS_DATE + " <= ?", 
            new String[]{userEmail, startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Get expense by category for a user within a date range
     */
    public List<Object[]> getExpenseByCategory(String userEmail, String startDate, String endDate) {
        List<Object[]> categoryExpenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT c." + CAT_NAME + ", SUM(t." + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " t " +
            "INNER JOIN " + TABLE_CATEGORY + " c ON t." + TRANS_CATEGORY_ID + " = c." + CAT_ID + " " +
            "WHERE t." + TRANS_USER_EMAIL + " = ? AND t." + TRANS_TYPE + " = 'EXPENSE' AND " +
            "t." + TRANS_DATE + " >= ? AND t." + TRANS_DATE + " <= ? " +
            "GROUP BY c." + CAT_NAME,
            new String[]{userEmail, startDate, endDate});
        
        while (cursor.moveToNext()) {
            String categoryName = cursor.getString(0);
            double amount = cursor.getDouble(1);
            categoryExpenses.add(new Object[]{categoryName, amount});
        }
        cursor.close();
        return categoryExpenses;
    }

    /**
     * Get income by category for a user within a date range
     */
    public List<Object[]> getIncomeByCategory(String userEmail, String startDate, String endDate) {
        List<Object[]> categoryIncomes = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT c." + CAT_NAME + ", SUM(t." + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " t " +
            "INNER JOIN " + TABLE_CATEGORY + " c ON t." + TRANS_CATEGORY_ID + " = c." + CAT_ID + " " +
            "WHERE t." + TRANS_USER_EMAIL + " = ? AND t." + TRANS_TYPE + " = 'INCOME' AND " +
            "t." + TRANS_DATE + " >= ? AND t." + TRANS_DATE + " <= ? " +
            "GROUP BY c." + CAT_NAME,
            new String[]{userEmail, startDate, endDate});
        
        while (cursor.moveToNext()) {
            String categoryName = cursor.getString(0);
            double amount = cursor.getDouble(1);
            categoryIncomes.add(new Object[]{categoryName, amount});
        }
        cursor.close();
        return categoryIncomes;
    }

    /**
     * Helper method to convert cursor to Transaction object
     */
    private Transaction cursorToTransaction(Cursor cursor) {
        Transaction transaction = new Transaction();
        transaction.setId(cursor.getLong(cursor.getColumnIndexOrThrow(TRANS_ID)));
        transaction.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(TRANS_USER_EMAIL)));
        transaction.setType(cursor.getString(cursor.getColumnIndexOrThrow(TRANS_TYPE)));
        transaction.setAmount(cursor.getDouble(cursor.getColumnIndexOrThrow(TRANS_AMOUNT)));
        transaction.setDate(cursor.getString(cursor.getColumnIndexOrThrow(TRANS_DATE)));
        transaction.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(TRANS_CATEGORY_ID)));
        transaction.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(TRANS_DESCRIPTION)));
        return transaction;
    }

    // ===================== BUDGET OPERATIONS =====================

    /**
     * Insert a new budget
     */
    public long insertBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BUDGET_USER_EMAIL, budget.getUserEmail());
        values.put(BUDGET_CATEGORY_ID, budget.getCategoryId());
        values.put(BUDGET_LIMIT, budget.getBudgetLimit());
        values.put(BUDGET_MONTH, budget.getMonth());
        values.put(BUDGET_ALERT_ENABLED, budget.isAlertEnabled() ? 1 : 0);
        values.put(BUDGET_ALERT_THRESHOLD, budget.getAlertThreshold());
        return db.insert(TABLE_BUDGET, null, values);
    }

    /**
     * Get all budgets for a user
     */
    public List<Budget> getAllBudgets(String userEmail) {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_BUDGET + " WHERE " + BUDGET_USER_EMAIL + " = ?", 
                                    new String[]{userEmail});
        
        while (cursor.moveToNext()) {
            budgets.add(cursorToBudget(cursor));
        }
        cursor.close();
        return budgets;
    }

    /**
     * Get budgets for a specific month
     */
    public List<Budget> getBudgetsByMonth(String userEmail, String month) {
        List<Budget> budgets = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_BUDGET + " WHERE " + BUDGET_USER_EMAIL + " = ? AND " + 
            BUDGET_MONTH + " = ?", 
            new String[]{userEmail, month});
        
        while (cursor.moveToNext()) {
            budgets.add(cursorToBudget(cursor));
        }
        cursor.close();
        return budgets;
    }

    /**
     * Get budget by category and month
     */
    public Budget getBudgetByCategoryAndMonth(String userEmail, long categoryId, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT * FROM " + TABLE_BUDGET + " WHERE " + BUDGET_USER_EMAIL + " = ? AND " + 
            BUDGET_CATEGORY_ID + " = ? AND " + BUDGET_MONTH + " = ?", 
            new String[]{userEmail, String.valueOf(categoryId), month});
        Budget budget = null;
        if (cursor.moveToFirst()) {
            budget = cursorToBudget(cursor);
        }
        cursor.close();
        return budget;
    }

    /**
     * Update budget
     */
    public boolean updateBudget(Budget budget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BUDGET_LIMIT, budget.getBudgetLimit());
        values.put(BUDGET_ALERT_ENABLED, budget.isAlertEnabled() ? 1 : 0);
        values.put(BUDGET_ALERT_THRESHOLD, budget.getAlertThreshold());
        int rowsAffected = db.update(TABLE_BUDGET, values, BUDGET_ID + " = ?", 
                                     new String[]{String.valueOf(budget.getId())});
        return rowsAffected > 0;
    }

    /**
     * Delete budget
     */
    public boolean deleteBudget(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_BUDGET, BUDGET_ID + " = ?", 
                                     new String[]{String.valueOf(id)});
        return rowsAffected > 0;
    }

    /**
     * Get spending for a category in a specific month
     */
    public double getSpendingForCategory(String userEmail, long categoryId, String month) {
        SQLiteDatabase db = this.getReadableDatabase();
        String startDate = month + "-01";
        String endDate = month + "-31";
        Cursor cursor = db.rawQuery(
            "SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " WHERE " + 
            TRANS_USER_EMAIL + " = ? AND " + TRANS_CATEGORY_ID + " = ? AND " +
            TRANS_TYPE + " = 'EXPENSE' AND " + TRANS_DATE + " >= ? AND " + TRANS_DATE + " <= ?", 
            new String[]{userEmail, String.valueOf(categoryId), startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Get spending for a category in a specific date range
     */
    public double getSpendingForCategoryInRange(String userEmail, long categoryId, String startDate, String endDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
            "SELECT SUM(" + TRANS_AMOUNT + ") FROM " + TABLE_TRANSACTION + " WHERE " + 
            TRANS_USER_EMAIL + " = ? AND " + TRANS_CATEGORY_ID + " = ? AND " +
            TRANS_TYPE + " = 'EXPENSE' AND " + TRANS_DATE + " >= ? AND " + TRANS_DATE + " <= ?", 
            new String[]{userEmail, String.valueOf(categoryId), startDate, endDate});
        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        return total;
    }

    /**
     * Helper method to convert cursor to Budget object
     */
    private Budget cursorToBudget(Cursor cursor) {
        Budget budget = new Budget();
        budget.setId(cursor.getLong(cursor.getColumnIndexOrThrow(BUDGET_ID)));
        budget.setUserEmail(cursor.getString(cursor.getColumnIndexOrThrow(BUDGET_USER_EMAIL)));
        budget.setCategoryId(cursor.getLong(cursor.getColumnIndexOrThrow(BUDGET_CATEGORY_ID)));
        budget.setBudgetLimit(cursor.getDouble(cursor.getColumnIndexOrThrow(BUDGET_LIMIT)));
        budget.setMonth(cursor.getString(cursor.getColumnIndexOrThrow(BUDGET_MONTH)));
        budget.setAlertEnabled(cursor.getInt(cursor.getColumnIndexOrThrow(BUDGET_ALERT_ENABLED)) == 1);
        budget.setAlertThreshold(cursor.getDouble(cursor.getColumnIndexOrThrow(BUDGET_ALERT_THRESHOLD)));
        return budget;
    }
}
