package com.example.andriodproject.model;

/**
 * Category model class representing income or expense categories.
 * Default categories are provided, and users can add custom categories.
 */
public class Category {
    private long id;
    private String name;
    private String type; // "INCOME" or "EXPENSE"
    private String userEmail; // null for default categories, user email for custom

    // Empty constructor
    public Category() {
    }

    // Constructor with all fields
    public Category(long id, String name, String type, String userEmail) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }

    // Constructor without id (for inserting new categories)
    public Category(String name, String type, String userEmail) {
        this.name = name;
        this.type = type;
        this.userEmail = userEmail;
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return name;
    }
}
