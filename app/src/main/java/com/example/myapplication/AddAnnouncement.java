package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.adapter.AnnouncementAdapter;
import com.example.myapplication.model.Announcement;
import com.example.myapplication.remote.AnnouncementService;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.sharedPref.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddAnnouncement extends AppCompatActivity {
    EditText announcementTitle, announcementMessage;
    Button submitButton, btnLogout;

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
        setContentView(R.layout.activity_add_announcement);

        announcementTitle = findViewById(R.id.announcementTitle);
        announcementMessage = findViewById(R.id.announcementMessage);
        submitButton = findViewById(R.id.submitAnnouncementButton);
        btnLogout = findViewById(R.id.btnLogout); // ✅ initialize logout button

        Button backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {
            // Go back to MainPageAdmin
            Intent intent = new Intent(AddAnnouncement.this, MainPageAdmin.class);
            startActivity(intent);
            finish(); // close this activity
        });

        submitButton.setOnClickListener(v -> {
            String title = announcementTitle.getText().toString().trim();
            String message = announcementMessage.getText().toString().trim();

            if (!title.isEmpty() && !message.isEmpty()) {
                saveAnnouncement(title, message);
            } else {
                Toast.makeText(this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
            }
        });

        // ✅ Logout listener placed correctly
        btnLogout.setOnClickListener(view -> {
            SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
            spm.logout();
            Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(AddAnnouncement.this, Login.class)); // or your login screen
            finish();
        });

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
                Toast.makeText(AddAnnouncement.this, "Failed to load announcements", Toast.LENGTH_SHORT).show();
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

    private void saveAnnouncement(String title, String message) {
        AnnouncementService announcementService = ApiUtils.getAnnouncementService();

        SharedPrefManager spm = new SharedPrefManager(getApplicationContext());
        String apiKey = spm.getUser().getToken();
        Log.d("API_KEY", "Key = " + apiKey);

        announcementService.addAnnouncement(apiKey, title, message)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(AddAnnouncement.this, "Announcement added", Toast.LENGTH_SHORT).show();

                            // Stay on the same page, clear inputs
                            announcementTitle.setText("");
                            announcementMessage.setText("");

                            // Reload announcements
                            loadAnnouncements();
                        } else {
                            Toast.makeText(AddAnnouncement.this, "Failed to add. Code: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Toast.makeText(AddAnnouncement.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
