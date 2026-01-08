package com.example.andriodproject.model;

/**
 * Transaction model class representing an income or expense transaction.
 * Each transaction belongs to a user and has a category.
 */
public class Transaction {
    private long id;
    private String userEmail; // Foreign key to User
    private String type; // "INCOME" or "EXPENSE"
    private double amount;
    private String date; // Format: yyyy-MM-dd
    private long categoryId; // Foreign key to Category
    private String description;

    // Empty constructor
    public Transaction() {
    }

    // Constructor with all fields
    public Transaction(long id, String userEmail, String type, double amount, 
                       String date, long categoryId, String description) {
        this.id = id;
        this.userEmail = userEmail;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.description = description;
    }

    // Constructor without id (for inserting new transactions)
    public Transaction(String userEmail, String type, double amount, 
                       String date, long categoryId, String description) {
        this.userEmail = userEmail;
        this.type = type;
        this.amount = amount;
        this.date = date;
        this.categoryId = categoryId;
        this.description = description;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return "Transaction{" +
                "id=" + id +
                ", userEmail='" + userEmail + '\'' +
                ", type='" + type + '\'' +
                ", amount=" + amount +
                ", date='" + date + '\'' +
                ", categoryId=" + categoryId +
                ", description='" + description + '\'' +
                '}';
    }
}
