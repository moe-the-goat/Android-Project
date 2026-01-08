package com.example.andriodproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.andriodproject.database.DataBaseHelper;
import com.example.andriodproject.fragments.BudgetsFragment;
import com.example.andriodproject.fragments.ExpensesFragment;
import com.example.andriodproject.fragments.HomeFragment;
import com.example.andriodproject.fragments.IncomeFragment;
import com.example.andriodproject.fragments.ProfileFragment;
import com.example.andriodproject.fragments.SettingsFragment;
import com.example.andriodproject.model.User;
import com.example.andriodproject.utils.SharedPrefManager;
import com.google.android.material.navigation.NavigationView;

/**
 * MainActivity - Main container activity with Navigation Drawer
 * Hosts all fragments for the app's main functionality
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private DataBaseHelper dbHelper;
    private SharedPrefManager sharedPrefManager;
    private String currentUserEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize shared preferences and apply theme
        sharedPrefManager = SharedPrefManager.getInstance(this);
        applyTheme();
        
        setContentView(R.layout.activity_main);

        // Initialize database
        dbHelper = new DataBaseHelper(this);

        // Get current user email
        currentUserEmail = sharedPrefManager.getUserEmail();
        
        // Check if user is logged in
        if (currentUserEmail == null) {
            navigateToLogin();
            return;
        }

        // Initialize views
        initViews();
        
        // Setup Navigation Drawer
        setupNavigationDrawer();
        
        // Update navigation header with user info
        updateNavHeader();

        // Load default fragment (Home)
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
            navigationView.setCheckedItem(R.id.nav_home);
            setTitle("Home");
        }
    }

    /**
     * Apply theme based on user preference
     */
    private void applyTheme() {
        if (sharedPrefManager.isDarkModeEnabled()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    /**
     * Initialize all views
     */
    private void initViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
    }

    /**
     * Setup Navigation Drawer with ActionBarDrawerToggle
     */
    private void setupNavigationDrawer() {
        // Set toolbar as action bar
        setSupportActionBar(toolbar);

        // Create toggle for drawer
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Update navigation header with user information
     */
    private void updateNavHeader() {
        View headerView = navigationView.getHeaderView(0);
        TextView tvUserName = headerView.findViewById(R.id.tvUserName);
        TextView tvUserEmail = headerView.findViewById(R.id.tvUserEmail);

        User user = dbHelper.getUser(currentUserEmail);
        if (user != null) {
            String fullName = user.getFirstName() + " " + user.getLastName();
            tvUserName.setText(fullName);
            tvUserEmail.setText(user.getEmail());
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;
        String title = "";

        int itemId = item.getItemId();

        if (itemId == R.id.nav_home) {
            fragment = new HomeFragment();
            title = "Home";
        } else if (itemId == R.id.nav_income) {
            fragment = new IncomeFragment();
            title = "Income";
        } else if (itemId == R.id.nav_expenses) {
            fragment = new ExpensesFragment();
            title = "Expenses";
        } else if (itemId == R.id.nav_budgets) {
            fragment = new BudgetsFragment();
            title = "Budgets & Goals";
        } else if (itemId == R.id.nav_profile) {
            fragment = new ProfileFragment();
            title = "Profile";
        } else if (itemId == R.id.nav_settings) {
            fragment = new SettingsFragment();
            title = "Settings";
        } else if (itemId == R.id.nav_logout) {
            logout();
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }

        if (fragment != null) {
            loadFragment(fragment);
            setTitle(title);
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Load a fragment into the container
     */
    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    /**
     * Handle logout
     */
    private void logout() {
        sharedPrefManager.logout();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        navigateToLogin();
    }

    /**
     * Navigate to login activity
     */
    private void navigateToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /**
     * Refresh navigation header (called when profile is updated)
     */
    public void refreshNavHeader() {
        updateNavHeader();
    }

    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return currentUserEmail;
    }
}