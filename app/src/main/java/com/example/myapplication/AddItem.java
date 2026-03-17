package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.*;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.model.User;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RecyclableItemService;
import com.example.myapplication.model.RecyclableItem;
import com.example.myapplication.sharedPref.SharedPrefManager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddItem extends AppCompatActivity {

    EditText etItemName, etPrice;
    Button btnAdd, btnBack, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        EdgeToEdge.enable(this);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addItemPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Reference UI elements
        etItemName = findViewById(R.id.etItemName);
        etPrice = findViewById(R.id.etPrice);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);

        // Handle "Add" button click
        btnAdd.setOnClickListener(this::addNewItem);

        // Handle "Back" button
        btnBack.setOnClickListener(v -> finish());

        // Handle "Logout" button
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }

    public void addNewItem(View v) {
        String itemName = etItemName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();

        if (TextUtils.isEmpty(itemName)) {
            etItemName.setError("Item name required");
            return;
        }

        if (TextUtils.isEmpty(priceStr)) {
            etPrice.setError("Price required");
            return;
        }

        double price;
        try {
            price = Double.parseDouble(priceStr);
        } catch (NumberFormatException e) {
            etPrice.setError("Invalid price format");
            return;
        }

        // get user info from SharedPreferences
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();

        RecyclableItemService service = ApiUtils.getRecyclableItemService();
        Call<RecyclableItem> call = service.addItem(user.getToken(), itemName, price);

        call.enqueue(new Callback<RecyclableItem>() {
            @Override
            public void onResponse(Call<RecyclableItem> call, Response<RecyclableItem> response) {
                Log.d("AddItem", "Response: " + response.raw());

                if (response.isSuccessful()) {
                    Toast.makeText(AddItem.this, "Item added successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(AddItem.this, MainPageAdmin.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(AddItem.this, "Failed to add item. Code: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<RecyclableItem> call, Throwable t) {
                Toast.makeText(AddItem.this, "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("AddItem", "Failure: ", t);
            }
        });
    }
}
