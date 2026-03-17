package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.model.RecyclableItem;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RecyclableItemService;
import com.example.myapplication.sharedPref.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditItem extends AppCompatActivity {

    private EditText etItemName, etPrice;
    private Button btnAdd, btnBack, btnLogout;
    private int itemId;
    private RecyclableItemService itemService;
    private SharedPrefManager sharedPrefManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_item);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.editItemPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etItemName = findViewById(R.id.etItemName);
        etPrice = findViewById(R.id.etPrice);
        btnAdd = findViewById(R.id.btnAdd);
        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);

        sharedPrefManager = new SharedPrefManager(getApplicationContext());
        itemService = ApiUtils.getRecyclableItemService();

        // Get intent data
        Intent intent = getIntent();
        itemId = intent.getIntExtra("item_id", -1);
        String name = intent.getStringExtra("item_name");
        double price = intent.getDoubleExtra("item_price", 0.0);

        // Set fields
        etItemName.setText(name);
        etPrice.setText(String.valueOf(price));

        // Handle Edit button click
        btnAdd.setOnClickListener(v -> {
            String updatedName = etItemName.getText().toString().trim();
            double updatedPrice;

            try {
                updatedPrice = Double.parseDouble(etPrice.getText().toString().trim());
            } catch (NumberFormatException e) {
                Toast.makeText(EditItem.this, "Invalid price format", Toast.LENGTH_SHORT).show();
                return;
            }

            Log.d("EditItem", "Updated Name: " + updatedName);
            Log.d("EditItem", "Updated Price: " + updatedPrice);
            Log.d("EditItem", "Item ID sent to API: " + itemId);

            // Create the updated object
            RecyclableItem updatedItem = new RecyclableItem(itemId, updatedName, updatedPrice);

            // Get API key from SharedPref
            String apiKey = sharedPrefManager.getUser().getToken();

            // Call the API
            itemService.updateItem(apiKey, itemId, updatedItem).enqueue(new Callback<RecyclableItem>() {
                @Override
                public void onResponse(Call<RecyclableItem> call, Response<RecyclableItem> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(EditItem.this, "Update successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(EditItem.this, ListItem.class);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d("EditItem", "Update failed: " + response.code());
                        Toast.makeText(EditItem.this, "Update failed: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<RecyclableItem> call, Throwable t) {
                    Log.d("EditItem", "Update error: " + t.getMessage());
                    Toast.makeText(EditItem.this, "Update error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });


        btnBack.setOnClickListener(v -> finish());
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
        });
    }
}
