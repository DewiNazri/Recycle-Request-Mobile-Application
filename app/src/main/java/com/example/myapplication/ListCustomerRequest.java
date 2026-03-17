package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Request;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RequestService;
import com.example.myapplication.sharedPref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListCustomerRequest extends AppCompatActivity {

    private RequestService requestService;
    private LinearLayout requestsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_customer_request);

        requestsContainer = findViewById(R.id.requestsContainer);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ListCustomerRequest.this, MainPageAdmin.class);
            startActivity(intent);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        requestsContainer.removeAllViews(); // Clear old list
        updateRequestList(); // Reload from server
    }

    private void updateRequestList() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        String token = spm.getUser().getToken();

        requestService = ApiUtils.getRequestService();

        requestService.getAllRequests(token).enqueue(new Callback<List<Request>>() {
            @Override
            public void onResponse(Call<List<Request>> call, Response<List<Request>> response) {

                if (response.isSuccessful()) {
                    List<Request> requests = response.body();

                    if (requests == null || requests.isEmpty()) {
                        showNoRequestMessage();
                        return;
                    }

                    for (Request req : requests) {
                        addRequestCard(
                                String.valueOf(req.getRequestId()),
                                req.getItem() != null ? req.getItem().getItemName() : "N/A",
                                String.format("%.2f", req.getTotalPrice()),
                                req.getStatus()
                        );
                    }
                } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), "Invalid session. Please login again.", Toast.LENGTH_LONG).show();
                    clearSessionAndRedirect();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                    Log.e("MyApp", response.toString());
                }
            }

            @Override
            public void onFailure(Call<List<Request>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Error connecting to the server.", Toast.LENGTH_LONG).show();
                Log.e("MyApp", t.toString());
            }
        });
    }

    private void showNoRequestMessage() {
        TextView noRequestText = new TextView(this);
        noRequestText.setText("❗ No customer requests have been made yet.");
        noRequestText.setTextSize(18);
        noRequestText.setTextColor(Color.GRAY);
        noRequestText.setGravity(Gravity.CENTER);
        noRequestText.setPadding(32, 64, 32, 64);

        requestsContainer.addView(noRequestText);
    }

    private void addRequestCard(String requestId, String itemName, String price, String status) {
        View cardView = getLayoutInflater().inflate(R.layout.customer_request_card, requestsContainer, false);

        ((TextView) cardView.findViewById(R.id.tvRequestId)).setText("Request ID: " + requestId);
        ((TextView) cardView.findViewById(R.id.tvItemName)).setText("Item: " + itemName);
        ((TextView) cardView.findViewById(R.id.tvPrice)).setText("Price: RM" + price);

        TextView statusText = cardView.findViewById(R.id.tvStatus);
        String formattedStatus = status.equalsIgnoreCase("pending") ? "Pending" : status;
        statusText.setText(formattedStatus);

        // Set status color
        if (status.equalsIgnoreCase("Pending")) {
            statusText.setTextColor(Color.parseColor("#FFA500")); // Orange
        } else if (status.equalsIgnoreCase("Completed")) {
            statusText.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else if (status.equalsIgnoreCase("Cancelled")) {
            statusText.setTextColor(Color.parseColor("#FF0000")); // Red
        } else {
            statusText.setTextColor(Color.parseColor("#000000")); // Default
        }

        cardView.findViewById(R.id.btnView).setOnClickListener(v -> {
            doViewDetails(Integer.parseInt(requestId));
        });

        requestsContainer.addView(cardView);
    }

    private void doViewDetails(int requestId) {
        Log.d("MyApp", "Viewing details for request ID: " + requestId);
        Intent intent = new Intent(getApplicationContext(), AdminRequest.class);
        intent.putExtra("REQUEST_ID", requestId);
        startActivity(intent);
    }

    public void clearSessionAndRedirect() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        finish();
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    public void logoutClicked(View view) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        Toast.makeText(getApplicationContext(),
                "You have successfully logged out.",
                Toast.LENGTH_LONG).show();

        finish();

        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }
}
