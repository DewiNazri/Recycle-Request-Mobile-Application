package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.model.FailLogin;
import com.example.myapplication.model.User;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.UserService;
import com.example.myapplication.sharedPref.SharedPrefManager;
import com.google.gson.Gson;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Login extends AppCompatActivity {

    private EditText edtEmail;
    private EditText edtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.edtEmail);
        edtPassword = findViewById(R.id.edtPassword);

        TextView registerLink = findViewById(R.id.textRegisterLink);
        registerLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Login.this, Register.class); // ← Ensure you have Register.java activity
                startActivity(intent);
            }
        });


    }

    public void loginClicked(View view) {
        String email = edtEmail.getText().toString();
        String password = edtPassword.getText().toString();

        if (validateLogin(email, password)) {
            doLogin(email, password);
        }
    }

    private void doLogin(String email, String password) {
        UserService userService = ApiUtils.getUserService();
        Call<User> call = userService.login(email, password); // ← only username & password

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful()) {
                    User user = response.body();
                    if (user != null) {
                        displayToast("Login successful");

                        SharedPrefManager spm = new SharedPrefManager(Login.this);
                        spm.storeUser(user);

                        String role = user.getRole(); // assuming your API sends this

                        Intent intent;
                        if ("customer".equalsIgnoreCase(role)) {
                            intent = new Intent(Login.this, MainPageCustomer.class);
                        } else if ("admin".equalsIgnoreCase(role)) {
                            intent = new Intent(Login.this, MainPageAdmin.class);
                        } else {
                            displayToast("Unknown role: " + role);
                            return;
                        }
                        startActivity(intent);
                        finish();
                    } else {
                        displayToast("Login error: Missing user data");
                    }
                } else {
                    try {
                        String errorResp = response.errorBody().string();
                        FailLogin e = new Gson().fromJson(errorResp, FailLogin.class);
                        displayToast(e.getError().getMessage());
                    } catch (Exception e) {
                        Log.e("LoginError", e.toString());
                        displayToast("Error parsing login failure");
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                displayToast("Error connecting to server.");
                Log.e("MyApp:", t.toString());
            }
        });
    }

    private boolean validateLogin(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            displayToast("Username is required");
            return false;
        }
        if (password == null || password.trim().isEmpty()) {
            displayToast("Password is required");
            return false;
        }
        return true;
    }

    public void displayToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
