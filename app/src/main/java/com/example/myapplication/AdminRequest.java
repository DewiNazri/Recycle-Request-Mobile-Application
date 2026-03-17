package com.example.myapplication;

import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
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

public class AdminRequest extends AppCompatActivity {

    TextView textEmail, textAddress, textItem, textNotes, textPrice, textStatus, textDate;
    EditText inputWeight;
    Button btnEditSave, btnBack, btnLogout;
    Spinner statusSpinner;

    private RequestService requestService;
    private Request currentRequest;
    private boolean isEditMode = false;

    private double getPricePerKgForItem(int itemId) {
        switch (itemId) {
            case 1: return 0.20; // Plastic
            case 2: return 0.25; // Paper
            case 3: return 1.25; // Metal
            default: return 0.20;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_request);

        statusSpinner = findViewById(R.id.statusSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                R.array.status_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        statusSpinner.setAdapter(adapter);

        textDate = findViewById(R.id.textDate);
        textEmail = findViewById(R.id.edtUsername);
        textAddress = findViewById(R.id.txtAddress);
        textItem = findViewById(R.id.txtItem);
        inputWeight = findViewById(R.id.txtWeight);
        textNotes = findViewById(R.id.txtNote);
        textPrice = findViewById(R.id.txtPrice);
        textStatus = findViewById(R.id.txtStatus);

        btnEditSave = findViewById(R.id.btnEditSave);
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

        btnEditSave.setOnClickListener(v -> {
            if (!isEditMode) {
                enterEditMode();
            } else {
                saveChanges();
            }
        });

        btnBack.setOnClickListener(v -> finish());

        btnLogout.setOnClickListener(v -> {
            SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
            spm.logout();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
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

                    textEmail.setText(currentRequest.getUser().getUsername());
                    textAddress.setText(currentRequest.getAddress());
                    textItem.setText(currentRequest.getItem().getItemName());
                    inputWeight.setText(String.format("%.2f kg", currentRequest.getWeight()));
                    textNotes.setText(currentRequest.getNotes());

                    // Show price
                    textPrice.setText("RM " + String.format("%.2f", currentRequest.getTotalPrice()));

                    // Format and set status
                    String currentStatus = currentRequest.getStatus().toLowerCase();
                    String displayStatus = currentStatus.substring(0, 1).toUpperCase() + currentStatus.substring(1);
                    textStatus.setText(displayStatus);

                    // Set status color
                    switch (currentStatus) {
                        case "pending":
                            textStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                            break;
                        case "completed":
                            textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                            break;
                        case "cancelled":
                            textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                            break;
                        default:
                            textStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                            break;
                    }

                    // Date formatting
                    String[] parts = currentRequest.getRequestDate().split(" ");
                    if (parts.length == 2) {
                        textDate.setText("Request Date: " + parts[0] + "\nRequest Time: " + parts[1]);
                    } else {
                        textDate.setText(currentRequest.getRequestDate());
                    }
                    textDate.setGravity(Gravity.CENTER);

                    // Disable editing if cancelled
                    if (currentStatus.equals("cancelled")) {
                        btnEditSave.setEnabled(false);
                        btnEditSave.setAlpha(0.5f);
                        statusSpinner.setEnabled(false);
                        inputWeight.setEnabled(false);
                        Toast.makeText(AdminRequest.this, "Request has been cancelled. Editing is not allowed.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(AdminRequest.this, "Failed to get request details", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Request> call, Throwable t) {
                Toast.makeText(AdminRequest.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void enterEditMode() {
        isEditMode = true;
        inputWeight.setEnabled(true);
        statusSpinner.setVisibility(View.VISIBLE);
        btnEditSave.setText("Save");
    }

    private void saveChanges() {
        String weightStr = inputWeight.getText().toString().replace("kg", "").trim();
        if (weightStr.isEmpty()) {
            inputWeight.setError("Enter weight");
            return;
        }

        double weight = Double.parseDouble(weightStr);
        double pricePerKg = getPricePerKgForItem(currentRequest.getItemId());
        double totalPrice = weight * pricePerKg;
        String newStatus = statusSpinner.getSelectedItem().toString();

        currentRequest.setWeight(weight);
        currentRequest.setTotalPrice(totalPrice);
        currentRequest.setStatus(newStatus);

        textPrice.setText("RM " + String.format("%.2f", totalPrice));
        textStatus.setText(newStatus);

        // Update status color again after change
        switch (newStatus.toLowerCase()) {
            case "pending":
                textStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
                break;
            case "completed":
                textStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                break;
            case "cancelled":
                textStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                break;
            default:
                textStatus.setTextColor(getResources().getColor(android.R.color.darker_gray));
                break;
        }

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        String token = spm.getUser().getToken();

        requestService.updateWeightPriceStatus(
                token,
                currentRequest.getRequestId(),
                String.valueOf(weight),
                String.valueOf(totalPrice),
                newStatus
        ).enqueue(new Callback<Request>() {
            @Override
            public void onResponse(Call<Request> call, Response<Request> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AdminRequest.this, "Updated successfully", Toast.LENGTH_SHORT).show();
                    isEditMode = false;
                    inputWeight.setEnabled(false);
                    statusSpinner.setVisibility(View.GONE);
                    btnEditSave.setText("Edit");
                } else {
                    Toast.makeText(AdminRequest.this, "Failed to update", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Request> call, Throwable t) {
                Toast.makeText(AdminRequest.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
