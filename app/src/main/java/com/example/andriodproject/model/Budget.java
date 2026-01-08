package com.example.andriodproject.model;

/**
 * Budget model class representing monthly budget limits for categories.
 * Users can set spending limits and receive alerts when approaching limits.
 */
public class Budget {
    private long id;
    private String userEmail; // Foreign key to User
    private long categoryId; // Foreign key to Category
    private double budgetLimit;
    private String month; // Format: yyyy-MM
    private boolean alertEnabled;
    private double alertThreshold; // Percentage (e.g., 50 for 50%)

    // Empty constructor
    public Budget() {
        this.alertEnabled = true;
        this.alertThreshold = 50.0; // Default: alert at 50%
    }

    // Constructor with all fields
    public Budget(long id, String userEmail, long categoryId, double budgetLimit, 
                  String month, boolean alertEnabled, double alertThreshold) {
        this.id = id;
        this.userEmail = userEmail;
        this.categoryId = categoryId;
        this.budgetLimit = budgetLimit;
        this.month = month;
        this.alertEnabled = alertEnabled;
        this.alertThreshold = alertThreshold;
    }

    // Constructor without id (for inserting new budgets)
    public Budget(String userEmail, long categoryId, double budgetLimit, 
                  String month, boolean alertEnabled, double alertThreshold) {
        this.userEmail = userEmail;
        this.categoryId = categoryId;
        this.budgetLimit = budgetLimit;
        this.month = month;
        this.alertEnabled = alertEnabled;
        this.alertThreshold = alertThreshold;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public double getBudgetLimit() {
        return budgetLimit;
    }

    public void setBudgetLimit(double budgetLimit) {
        this.budgetLimit = budgetLimit;
    }

    public String getMonth() {
        return month;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    public boolean isAlertEnabled() {
        return alertEnabled;
    }

    public void setAlertEnabled(boolean alertEnabled) {
        this.alertEnabled = alertEnabled;
    }

    public double getAlertThreshold() {
        return alertThreshold;
    }

    public void setAlertThreshold(double alertThreshold) {
        this.alertThreshold = alertThreshold;
    }

    @Override
    public String toString() {
        return "Budget{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", categoryId=" + categoryId +
                ", budgetLimit=" + budgetLimit +
                ", month='" + month + '\'' +
                ", alertEnabled=" + alertEnabled +
                ", alertThreshold=" + alertThreshold +
                '}';
    }
}
