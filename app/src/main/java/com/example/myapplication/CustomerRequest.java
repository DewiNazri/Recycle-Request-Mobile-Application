package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.Request;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RequestService;
import com.example.myapplication.sharedPref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CustomerRequest extends AppCompatActivity {

    TextView textName, textAddress, textItem, textNotes, textPrice, textStatus, textDate, textWeight;
    Button btnCancel, btnBack, btnLogout;

    private RequestService requestService;
    private Request currentRequest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_request);

        textDate = findViewById(R.id.textDate);
        textAddress = findViewById(R.id.txtAddress);
        textItem = findViewById(R.id.txtItem);
        textWeight = findViewById(R.id.txtWeight);
        textNotes = findViewById(R.id.txtNote);
        textPrice = findViewById(R.id.txtPrice);
        textStatus = findViewById(R.id.txtStatus);

        btnCancel = findViewById(R.id.cancelButton);
        btnBack = findViewById(R.id.backButton);
        btnLogout = findViewById(R.id.btnLogout);

        requestService = ApiUtils.getRequestService();


        int requestId = getIntent().getIntExtra("REQUEST_ID", -1);

        if (requestId != -1) {
            loadOrderDetails(requestId);
        } else {
            Toast.makeText(this, "Invalid request ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        btnCancel.setOnClickListener(v -> cancelRequest());
        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> {
            new SharedPrefManager(getApplicationContext()).logout();
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    private void loadOrderDetails(int requestId) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        String token = spm.getUser().getToken();

        requestService.getRequest(token, requestId).enqueue(new Callback<Request>() {
            @Override
            public void onResponse(Call<Request> call, Response<Request> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentRequest = response.body();

                    textAddress.setText(currentRequest.getAddress());
                    textItem.setText(currentRequest.getItem().getItemName());
                    textWeight.setText(String.format("%.2f kg", currentRequest.getWeight()));
                    textNotes.setText(currentRequest.getNotes());
                    textPrice.setText("RM " + String.format("%.2f", currentRequest.getTotalPrice()));
                    String displayStatus = currentRequest.getStatus().substring(0, 1).toUpperCase() + currentRequest.getStatus().substring(1).toLowerCase();
                    textStatus.setText(displayStatus);

                    String[] parts = currentRequest.getRequestDate().split(" ");
                    if (parts.length == 2) {
                        textDate.setText("PickUp Date: " + parts[0] + "\nPickUp Time: " + parts[1]);
                    } else {
                        textDate.setText(currentRequest.getRequestDate());
                    }
                    textDate.setGravity(Gravity.CENTER);

                    TextView txtStatus = findViewById(R.id.txtStatus);
                    String status = currentRequest.getStatus();

                    if (status.equalsIgnoreCase("Cancelled")) {
                        btnCancel.setEnabled(false);
                        btnCancel.setText("Already Cancelled");
                        txtStatus.setTextColor(Color.parseColor("#FF0000")); // Red
                    } else if (status.equalsIgnoreCase("Completed")) {
                        btnCancel.setEnabled(false);
                        btnCancel.setText("Completed");
                        txtStatus.setTextColor(Color.parseColor("#4CAF50")); // Green
                    } else if (status.equalsIgnoreCase("Pending")) {
                        btnCancel.setEnabled(true);
                        btnCancel.setText("Cancel Request");
                        txtStatus.setTextColor(Color.parseColor("#FFC107")); // Amber
                    } else {
                        btnCancel.setEnabled(false);
                        btnCancel.setText("Cannot Cancel");
                        txtStatus.setTextColor(Color.GRAY); // Default for unknown status
                    }

// Set the status text in the TextView
                    txtStatus.setText(status);


                } else {
                    Toast.makeText(CustomerRequest.this, "Failed to get request details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Request> call, Throwable t) {
                Toast.makeText(CustomerRequest.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void cancelRequest() {
        if (currentRequest == null) return;

        String token = new SharedPrefManager(getApplicationContext()).getUser().getToken();

        requestService.cancelRequest( token,
                        currentRequest.getRequestId(),
                        String.format("%.2f", currentRequest.getWeight()),
                        String.format("%.2f", currentRequest.getTotalPrice()),  // reuse same price
                        "Cancelled")
                .enqueue(new Callback<Request>() {
                    @Override
                    public void onResponse(Call<Request> call, Response<Request> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(CustomerRequest.this, "Request cancelled successfully", Toast.LENGTH_SHORT).show();
                            textStatus.setText("Cancelled");
                            btnCancel.setEnabled(false);
                            btnCancel.setText("Cancelled");
                        } else {
                            Toast.makeText(CustomerRequest.this, "Failed to cancel request", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Request> call, Throwable t) {
                        Toast.makeText(CustomerRequest.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void logoutClicked(View view) {
        // clear the shared preferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();

        // display message
        Toast.makeText(getApplicationContext(),
                "You have successfully logged out.",
                Toast.LENGTH_LONG).show();

        // terminate this MainActivity
        finish();

        // forward to Login Page
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);

    }
}
