package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.AnnouncementAdapter;
import com.example.myapplication.model.Announcement;
import com.example.myapplication.remote.AnnouncementService;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.sharedPref.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainPageCustomer extends AppCompatActivity {

    CardView cardLatestAnnouncement;
    TextView tvAnnouncementTitle, tvAnnouncementMessage, tvInstruction;

    RecyclerView rvOlderAnnouncements;
    AnnouncementAdapter adapter;
    List<Announcement> allAnnouncements = new ArrayList<>();

    boolean showingOlder = false;

    AnnouncementService announcementService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_page_customer);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_page_customer), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Check if user is logged in
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        if (!spm.isLoggedIn()) {
            finish();
            startActivity(new Intent(this, Login.class));
            return;
        }

        // Announcements setup
        cardLatestAnnouncement = findViewById(R.id.cardLatestAnnouncement);
        tvAnnouncementTitle = findViewById(R.id.tvAnnouncementTitle);
        tvAnnouncementMessage = findViewById(R.id.tvAnnouncementMessage);
        tvInstruction = findViewById(R.id.tvInstruction);
        rvOlderAnnouncements = findViewById(R.id.rvOlderAnnouncements);

        rvOlderAnnouncements.setLayoutManager(new LinearLayoutManager(this));
        adapter = new AnnouncementAdapter(this, new ArrayList<>());
        adapter.setSimpleMode(true);
        rvOlderAnnouncements.setAdapter(adapter);

        announcementService = ApiUtils.getAnnouncementService();
        loadAnnouncements();

        cardLatestAnnouncement.setOnLongClickListener(v -> {
            toggleOlder();
            return true;
        });
    }

    private void loadAnnouncements() {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        String apiKey = spm.getUser().getToken();

        announcementService.getAllAnnouncements(apiKey).enqueue(new Callback<List<Announcement>>() {
            @Override
            public void onResponse(Call<List<Announcement>> call, Response<List<Announcement>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    allAnnouncements = response.body();
                    Announcement latest = allAnnouncements.get(0);
                    tvAnnouncementTitle.setText(latest.getTitle());
                    tvAnnouncementMessage.setText(latest.getMessage());
                }
            }

            @Override
            public void onFailure(Call<List<Announcement>> call, Throwable t) {
                Toast.makeText(MainPageCustomer.this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void toggleOlder() {
        if (showingOlder) {
            rvOlderAnnouncements.setVisibility(View.GONE);
            tvInstruction.setText("💡 Long press the card to view older announcements");
            showingOlder = false;
        } else {
            if (allAnnouncements.size() > 1) {
                List<Announcement> older = allAnnouncements.subList(1, allAnnouncements.size());
                adapter.updateData(older);
                rvOlderAnnouncements.setVisibility(View.VISIBLE);
                tvInstruction.setText("💡 Long press again to hide older announcements");
                showingOlder = true;
            } else {
                Toast.makeText(this, "No older announcements.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void logoutClicked(View view) {
        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        spm.logout();
        Toast.makeText(getApplicationContext(), "You have successfully logged out.", Toast.LENGTH_LONG).show();
        finish();
        startActivity(new Intent(this, Login.class));
    }

    public void viewRequestClicked(View view) {
        startActivity(new Intent(MainPageCustomer.this, ListMyRequest.class));
    }

    public void addRequestClicked(View view) {
        startActivity(new Intent(MainPageCustomer.this, AddRequest.class));
    }
}
