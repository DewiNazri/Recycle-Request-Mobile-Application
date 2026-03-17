package com.example.myapplication;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.RecyclableItem;
import com.example.myapplication.model.Request;
import com.example.myapplication.model.User;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RecyclableItemService;
import com.example.myapplication.remote.RequestService;
import com.example.myapplication.sharedPref.SharedPrefManager;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRequest extends AppCompatActivity {

    Spinner itemTypeSpinner;
    EditText addressEditText, notesEditText;
    Button cancelButton, requestButton, logoutButton, dateButton, timeButton;

    List<RecyclableItem> itemList;
    Calendar selectedDateTime = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_request);

        itemTypeSpinner = findViewById(R.id.itemTypeSpinner);
        addressEditText = findViewById(R.id.addressEditText);
        notesEditText = findViewById(R.id.notesEditText);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        cancelButton = findViewById(R.id.cancelButton);
        requestButton = findViewById(R.id.requestButton);
        logoutButton = findViewById(R.id.logoutButton);

        populateItemSpinner();

        // Set initial text
        dateButton.setText("Select Date");
        timeButton.setText("Select Time");

        dateButton.setOnClickListener(v -> showDatePicker());
        timeButton.setOnClickListener(v -> showTimePicker());

        cancelButton.setOnClickListener(v -> finish());

        logoutButton.setOnClickListener(v -> {
            SharedPrefManager spm = new SharedPrefManager(this);
            spm.logout();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
        });

        requestButton.setOnClickListener(v -> validateAndConfirmRequest());
    }

    private void populateItemSpinner() {
        SharedPrefManager spm = new SharedPrefManager(this);
        String token = spm.getUser().getToken();

        RecyclableItemService itemService = ApiUtils.getRecyclableItemService();
        Call<List<RecyclableItem>> call = itemService.getAllItems(token);

        call.enqueue(new Callback<List<RecyclableItem>>() {
            @Override
            public void onResponse(Call<List<RecyclableItem>> call, Response<List<RecyclableItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    itemList = response.body();

                    if (itemList.isEmpty()) {
                        Toast.makeText(AddRequest.this, "No items available for request.", Toast.LENGTH_LONG).show();
                        requestButton.setEnabled(false);
                        return;
                    }

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(AddRequest.this,
                            android.R.layout.simple_spinner_item);

                    for (RecyclableItem item : itemList) {
                        adapter.add(item.getItemName() + " - RM" + String.format("%.2f", item.getPricePerKg()) + " per kg");
                    }

                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    itemTypeSpinner.setAdapter(adapter);
                } else {
                    Toast.makeText(AddRequest.this, "Failed to load item list.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecyclableItem>> call, Throwable t) {
                Toast.makeText(AddRequest.this, "Error loading items: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(Calendar.YEAR, year);
                    selectedDateTime.set(Calendar.MONTH, month);
                    selectedDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    dateButton.setText(sdf.format(selectedDateTime.getTime()));
                },
                selectedDateTime.get(Calendar.YEAR),
                selectedDateTime.get(Calendar.MONTH),
                selectedDateTime.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }

    private void showTimePicker() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(
                this,
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);

                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                    timeButton.setText(sdf.format(selectedDateTime.getTime()));
                },
                selectedDateTime.get(Calendar.HOUR_OF_DAY),
                selectedDateTime.get(Calendar.MINUTE),
                true
        );
        timePickerDialog.show();
    }

    private void validateAndConfirmRequest() {
        int selectedPosition = itemTypeSpinner.getSelectedItemPosition();
        String address = addressEditText.getText().toString().trim();
        String notes = notesEditText.getText().toString().trim();
        String date = dateButton.getText().toString().trim();
        String time = timeButton.getText().toString().trim();

        if (itemList == null || itemList.isEmpty()) {
            Toast.makeText(this, "No item selected.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedPosition == Spinner.INVALID_POSITION) {
            Toast.makeText(this, "Please select a valid item type.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (address.isEmpty()) {
            Toast.makeText(this, "Please enter address.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (date.equals("Select Date")) {
            Toast.makeText(this, "Please select a date.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (time.equals("Select Time")) {
            Toast.makeText(this, "Please select a time.", Toast.LENGTH_SHORT).show();
            return;
        }

        if (notes.length() > 255) {
            Toast.makeText(this, "Notes cannot exceed 255 characters.", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Submit Request")
                .setMessage("Are you sure you want to submit this recycling request?")
                .setPositiveButton("Yes", (dialog, which) -> submitRequest(selectedPosition, address, notes))
                .setNegativeButton("No", null)
                .show();
    }

    private void submitRequest(int selectedPosition, String address, String notes) {
        RecyclableItem selectedItem = itemList.get(selectedPosition);
        int itemId = selectedItem.getItemId();

        // Combine selected date and time
        String dateTime = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                .format(selectedDateTime.getTime());

        SharedPrefManager spm = new SharedPrefManager(this);
        User user = spm.getUser();
        String token = user.getToken();
        int userId = user.getId();

        // Default values
        String weight = "0"; // default until collected
        String totalPrice = "0"; // default until collected
        String status = "pending";

        RequestService requestService = ApiUtils.getRequestService();
        Call<Request> call = requestService.addRequest(
                token, userId, itemId, address, dateTime, status, weight, totalPrice, notes
        );

        call.enqueue(new Callback<Request>() {
            @Override
            public void onResponse(Call<Request> call, Response<Request> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(AddRequest.this, "Request submitted successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(AddRequest.this, MainPageCustomer.class));
                    finish();
                } else {
                    Toast.makeText(AddRequest.this, "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Request> call, Throwable t) {
                Toast.makeText(AddRequest.this, "Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}
