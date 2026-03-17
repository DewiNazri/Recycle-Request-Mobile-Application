package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

public class ListMyRequest extends AppCompatActivity {

    private RequestService requestService;
    private LinearLayout requestsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_my_request);

        requestsContainer = findViewById(R.id.requestsContainer);

        Button btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(ListMyRequest.this, MainPageCustomer.class);
            startActivity(intent);
            finish();
        });

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
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
        int currentUserId = spm.getUser().getId();

        requestService = ApiUtils.getRequestService();

        requestService.getAllRequests(token).enqueue(new Callback<List<Request>>() {
            @Override
            public void onResponse(Call<List<Request>> call, Response<List<Request>> response) {
                if (response.isSuccessful()) {
                    List<Request> requests = response.body();
                    boolean hasRequest = false;

                    for (Request req : requests) {
                        if (req.getUserId() == currentUserId) {
                            hasRequest = true;
                            addRequestCard(
                                    String.valueOf(req.getRequestId()),
                                    req.getItem() != null ? req.getItem().getItemName() : "N/A",
                                    String.format("%.2f", req.getTotalPrice()),
                                    req.getStatus()
                            );
                        }
                    }

                    if (!hasRequest) {
                        showNoRequestBanner();
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

    private void showNoRequestBanner() {
        TextView noRequestBanner = new TextView(this);
        noRequestBanner.setText("You don't have any request");
        noRequestBanner.setTextColor(Color.GRAY);
        noRequestBanner.setTextSize(18);
        noRequestBanner.setPadding(40, 60, 40, 60);
        noRequestBanner.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

        requestsContainer.addView(noRequestBanner);
    }

    private void addRequestCard(String requestId, String itemName, String price, String status) {
        View cardView = getLayoutInflater().inflate(R.layout.my_request_card, requestsContainer, false);

        ((TextView) cardView.findViewById(R.id.tvRequestId)).setText("Request ID: " + requestId);
        ((TextView) cardView.findViewById(R.id.tvItemName)).setText("Item: " + itemName);
        ((TextView) cardView.findViewById(R.id.tvPrice)).setText("Price: RM" + price);

        String displayStatus = status.substring(0, 1).toUpperCase() + status.substring(1).toLowerCase();
        TextView statusView = cardView.findViewById(R.id.tvStatus);
        statusView.setText(displayStatus);

        if ("Pending".equalsIgnoreCase(status)) {
            statusView.setTextColor(Color.parseColor("#FFA500")); // orange
        } else if ("Completed".equalsIgnoreCase(status)) {
            statusView.setTextColor(Color.parseColor("#4CAF50")); // green
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            statusView.setTextColor(Color.parseColor("#FF0000")); // red
        } else {
            statusView.setTextColor(Color.parseColor("#000000")); // black
        }

        cardView.findViewById(R.id.btnView).setOnClickListener(v -> {
            doViewDetails(Integer.parseInt(requestId));
        });

        requestsContainer.addView(cardView);
    }

    private void doViewDetails(int requestId) {
        Log.d("MyApp", "Viewing details for request ID: " + requestId);
        Intent intent = new Intent(ListMyRequest.this, CustomerRequest.class);
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
}
