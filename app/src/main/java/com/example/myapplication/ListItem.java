package com.example.myapplication;

import static androidx.core.content.ContextCompat.startActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.RecyclableItemAdapter;
import com.example.myapplication.model.RecyclableItem;
import com.example.myapplication.model.User;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.RecyclableItemService;
import com.example.myapplication.sharedPref.SharedPrefManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ListItem extends AppCompatActivity {

    Button btnBack, btnLogout;
    private RecyclableItemService recyclableItemService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_list_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.listItemPage), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btnBack = findViewById(R.id.btnBack);
        btnLogout = findViewById(R.id.btnLogout);

        // Handle "Back" button
        btnBack.setOnClickListener(v -> finish());

        // Handle "Logout" button
        btnLogout.setOnClickListener(v -> {
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
        });

        loadItems();

    }

    private void loadItems() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        User user = spm.getUser();
        String token = user.getToken();

        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "No token found. Please login again.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, Login.class));
            finish();
            return;
        }

        Log.d("TOKEN_CHECK", "Using token: " + token);

        recyclableItemService = ApiUtils.getRecyclableItemService();

        recyclableItemService.getAllItems(token).enqueue(new Callback<List<RecyclableItem>>() {
            @Override
            public void onResponse(Call<List<RecyclableItem>> call, Response<List<RecyclableItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<RecyclableItem> itemList = response.body();

                    RecyclerView recyclerView = findViewById(R.id.recyclerViewItems);
                    recyclerView.setLayoutManager(new LinearLayoutManager(ListItem.this));
                    RecyclableItemAdapter adapter = new RecyclableItemAdapter(ListItem.this, itemList);
                    recyclerView.setAdapter(adapter);

                    Log.d("API_SUCCESS", "Items loaded: " + itemList.size());
                } else if (response.code() == 401) {
                    Toast.makeText(getApplicationContext(), "Session expired. Please login again.", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(ListItem.this, Login.class));
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), "Error: " + response.message(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecyclableItem>> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to connect: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }




    //button Add Item
   public void addItemClicked(View view) {
        Intent intent = new Intent(ListItem.this, AddItem.class);
        startActivity(intent);
    }


}

