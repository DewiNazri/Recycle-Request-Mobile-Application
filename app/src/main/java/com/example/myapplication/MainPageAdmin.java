package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Request;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RequestService;
import com.example.myapplication.sharedPref.SharedPrefManager;
import com.google.gson.Gson;

import java.text.DecimalFormat;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPageAdmin extends AppCompatActivity {

    TextView totalRequestText, pendingRequestText, completedRequestText, totalRevenueText;
    Button btnLogout, btnViewRequest, btnListItem, btnAddItem;

    RequestService requestService;
    String apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page_admin);

        // UI references
        totalRequestText = findViewById(R.id.totalRequest);
        pendingRequestText = findViewById(R.id.pendingRequest);
        completedRequestText = findViewById(R.id.completedRequest);
        totalRevenueText = findViewById(R.id.totalRevenue);

        btnLogout = findViewById(R.id.btnLogout);
        btnViewRequest = findViewById(R.id.viewRequestButton);
        btnListItem = findViewById(R.id.listItemButton);
        btnAddItem = findViewById(R.id.addItemButton);

        // Load token
        SharedPrefManager sharedPrefManager = new SharedPrefManager(this);
        if (sharedPrefManager.getUser() != null) {
            apiKey = sharedPrefManager.getUser().getToken();
        } else {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        // Init Retrofit
        requestService = ApiUtils.getRequestService();

        // Load data
        loadSummaryData();

        // Button click actions
        btnLogout.setOnClickListener(v -> logoutClicked());
        btnViewRequest.setOnClickListener(v -> startActivity(new Intent(this, ListCustomerRequest.class)));
        btnListItem.setOnClickListener(v -> startActivity(new Intent(this, ListItem.class)));
        btnAddItem.setOnClickListener(v -> startActivity(new Intent(this, AddItem.class)));
    }

    private void loadSummaryData() {
        if (apiKey == null || apiKey.isEmpty()) {
            Toast.makeText(this, "Invalid token", Toast.LENGTH_SHORT).show();
            return;
        }

        // ⬇️ Only pass raw API key, no "Bearer "
        requestService.getAllRequests(apiKey).enqueue(new Callback<List<Request>>() {
            @Override
            public void onResponse(Call<List<Request>> call, Response<List<Request>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("API_RESPONSE", "Data: " + new Gson().toJson(response.body()));

                    int total = 0;
                    int pending = 0;
                    int completed = 0;
                    double revenue = 0.0;

                    for (Request req : response.body()) {
                        total++;
                        String status = req.getStatus().toLowerCase();
                        if (status.equals("pending")) {
                            pending++;
                        } else if (status.equals("completed")) {
                            completed++;
                            revenue += req.getTotalPrice();
                        }
                    }

                    totalRequestText.setText(String.valueOf(total));
                    pendingRequestText.setText(String.valueOf(pending));
                    completedRequestText.setText(String.valueOf(completed));

                    DecimalFormat formatter = new DecimalFormat("RM#,##0.00");
                    totalRevenueText.setText(formatter.format(revenue));
                } else {
                    try {
                        Log.e("API_ERROR", "Raw: " + response.errorBody().string());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Log.e("API_ERROR", "Code: " + response.code() + " - " + response.message());
                    Toast.makeText(MainPageAdmin.this, "Failed to load request summary", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Request>> call, Throwable t) {
                Log.e("API_FAILURE", "Throwable: " + t.getMessage());
                Toast.makeText(MainPageAdmin.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void logoutClicked() {
        SharedPrefManager spm = new SharedPrefManager(this);
        spm.clear();
        Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, Login.class));
        finish();
    }

    public void addAnnouncementClicked(View view) {
        Intent intent = new Intent(MainPageAdmin.this, AddAnnouncement.class);
        startActivity(intent);
    }
}
