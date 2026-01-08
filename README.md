# Personal Finance Manager

A comprehensive Android application designed to help users take control of their personal finances. This app provides intuitive tools for tracking income and expenses, setting budgets, and visualizing financial data through interactive charts and detailed reports.

## Overview

Personal Finance Manager is built as part of the ENCS5150 course project. It demonstrates practical implementation of Android development concepts including SQLite database management, SharedPreferences for session handling, Navigation Drawer architecture, and data visualization using MPAndroidChart.

The application empowers users to make informed financial decisions by providing a clear picture of their spending habits, income sources, and budget adherence.

## Features

### User Authentication
- Secure user registration with email validation
- Login system with password visibility toggle
- "Remember Me" functionality for convenient access
- Session management using SharedPreferences

### Dashboard
- Real-time financial summary showing total income, expenses, and balance
- Period-based filtering (Daily, Weekly, Monthly, Custom date range)
- Visual representation of expenses by category using pie charts
- Income vs Expenses comparison chart
- Monthly expense trends displayed in bar chart format
- Budget alerts when spending approaches or exceeds set limits
- Quick access to recent transactions

### Income Management
- Add, edit, and delete income entries
- Categorize income by source (Salary, Freelance, Investments, etc.)
- Date selection for accurate record keeping
- Optional notes for each transaction
- Filterable list view of all income records

### Expense Tracking
- Comprehensive expense logging with category assignment
- Support for both default and custom expense categories
- Date and amount tracking with validation
- Search and filter functionality
- Detailed transaction history

### Budget Management
- Set monthly budgets for specific expense categories
- Configurable alert thresholds (percentage-based)
- Visual progress indicators showing budget utilization
- Automatic alerts when approaching budget limits
- Month-by-month budget tracking

### Categories
- Pre-defined categories for common income and expense types
- Ability to create custom categories
- Category management (add, edit, delete user-created categories)
- Separate handling for income and expense categories

### Reports and Analytics
- Generate detailed financial reports for any period
- Category-wise breakdown of income and expenses
- Percentage analysis of spending patterns
- Budget status overview
- Share reports via email, messaging, or other apps

### Settings and Preferences
- Dark mode support with system theme integration
- Default period selection for dashboard view
- Currency display preferences
- User profile management

## Technical Architecture

### Project Structure
```
app/
├── src/main/
│   ├── java/com/example/andriodproject/
│   │   ├── activities/
│   │   │   ├── LoginActivity.java
│   │   │   └── SignupActivity.java
│   │   ├── adapters/
│   │   │   ├── BudgetAdapter.java
│   │   │   ├── BudgetAlertAdapter.java
│   │   │   ├── CategoryAdapter.java
│   │   │   └── TransactionAdapter.java
│   │   ├── database/
│   │   │   └── DataBaseHelper.java
│   │   ├── fragments/
│   │   │   ├── BudgetsFragment.java
│   │   │   ├── ExpensesFragment.java
│   │   │   ├── HomeFragment.java
│   │   │   ├── IncomeFragment.java
│   │   │   ├── ProfileFragment.java
│   │   │   └── SettingsFragment.java
│   │   ├── model/
│   │   │   ├── Budget.java
│   │   │   ├── Category.java
│   │   │   ├── Transaction.java
│   │   │   └── User.java
│   │   ├── utils/
│   │   │   └── SharedPrefManager.java
│   │   └── MainActivity.java
│   └── res/
│       ├── drawable/
│       ├── layout/
│       ├── menu/
│       ├── values/
│       └── values-night/
```

### Technologies Used
- **Language**: Java 11
- **Minimum SDK**: API 26 (Android 8.0 Oreo)
- **Target SDK**: API 36
- **Database**: SQLite with SQLiteOpenHelper
- **UI Components**: Material Design Components
- **Charts**: MPAndroidChart v3.1.0
- **Architecture**: Single Activity with Multiple Fragments

### Database Schema

The application uses SQLite with the following tables:

**Users Table**
- Stores user credentials and profile information
- Supports secure authentication

**Transactions Table**
- Records all income and expense entries
- Links to categories and users
- Stores amount, date, type, and notes

**Categories Table**
- Contains both default and user-created categories
- Separated by type (INCOME/EXPENSE)

**Budgets Table**
- Monthly budget limits per category
- Alert threshold configuration
- User-specific budget tracking

## Installation

### Prerequisites
- Android Studio Arctic Fox or later
- JDK 11 or higher
- Android device or emulator running API 26+

### Setup Instructions

1. Clone the repository
   ```
   git clone [repository-url]
   ```

2. Open the project in Android Studio
   - Launch Android Studio
   - Select "Open an existing project"
   - Navigate to the cloned directory

3. Sync Gradle files
   - Android Studio should automatically prompt for Gradle sync
   - If not, click "Sync Project with Gradle Files"

4. Build the project
   - Select Build > Make Project from the menu
   - Resolve any dependency issues if prompted

5. Run the application
   - Connect an Android device or start an emulator
   - Click the Run button or press Shift+F10

## Usage Guide

### Getting Started

1. **Create an Account**: Launch the app and tap "Sign Up" to create a new account with your email and password.

2. **Log In**: Use your credentials to access the app. Enable "Remember Me" for quicker access next time.

3. **Explore the Dashboard**: The home screen displays your financial overview. Use the period selector to view different time ranges.

### Recording Transactions

1. Navigate to "Income" or "Expenses" from the navigation drawer
2. Tap the floating action button (+) to add a new entry
3. Fill in the amount, select a category, choose the date, and add optional notes
4. Save the transaction

### Setting Budgets

1. Go to "Budgets" from the navigation drawer
2. Tap the add button to create a new budget
3. Select an expense category and set the monthly limit
4. Configure the alert threshold percentage
5. Save to start tracking

### Generating Reports

1. From the dashboard, scroll to find the "Generate Detailed Report" button
2. The report will include data for the currently selected period
3. Use the Share option to send the report via your preferred method

### Customizing the App

1. Access "Settings" from the navigation drawer
2. Toggle dark mode based on your preference
3. Set your default viewing period for the dashboard

## Contributing

Contributions to improve the application are welcome. Please follow these guidelines:

1. Fork the repository
2. Create a feature branch
3. Make your changes with clear commit messages
4. Test thoroughly on multiple device configurations
5. Submit a pull request with a detailed description

## License

This project is developed for educational purposes as part of the ENCS5150 course curriculum.

## Acknowledgments

- MPAndroidChart library for providing excellent charting capabilities
- Material Design Components for consistent and modern UI elements
- The Android development community for valuable resources and documentation
