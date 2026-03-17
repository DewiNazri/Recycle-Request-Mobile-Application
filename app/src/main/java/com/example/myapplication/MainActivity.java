package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.model.User;
import com.example.myapplication.sharedPref.SharedPrefManager;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if user is logged in
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            // Not logged in → redirect to Login page
            finish(); // Close current activity
            Intent intent = new Intent(this, Login.class);
            startActivity(intent);
        } else {
            // User is logged in → log user info
            User user = spm.getUser();
            Log.d("MainActivity", "Logged in as: " + user.getUsername());
        }

        // Set up logout button
        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());
    }

    private void logoutUser() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.clear(); // Clear session

        // Redirect to login activity
        Intent intent = new Intent(MainActivity.this, Login.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Close MainActivity
    }

    @Override
    public void onBackPressed() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.clear(); // Clear session
        super.onBackPressed(); // Exit activity
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.clear();
    }
}
