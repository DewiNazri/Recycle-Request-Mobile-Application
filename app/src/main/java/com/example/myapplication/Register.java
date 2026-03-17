package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.model.User;
import com.example.myapplication.remote.ApiUtils;
import com.example.myapplication.remote.UserService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Register extends AppCompatActivity {

    private EditText edtEmail, edtUsername, edtPassword, edtConfirmPassword;
    private Button btnRegister;
    private TextView tvLoginLink;

    private UserService userService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        edtEmail = findViewById(R.id.edtEmail);
        edtUsername = findViewById(R.id.edtUsername);
        edtPassword = findViewById(R.id.edtPassword);
        edtConfirmPassword = findViewById(R.id.edtConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLoginLink = findViewById(R.id.tvLoginLink);

        userService = ApiUtils.getUserService();

        // Register new user
        btnRegister.setOnClickListener(v -> registerUser());

        // Navigate to Login page
        tvLoginLink.setOnClickListener(v -> {
            startActivity(new Intent(Register.this, Login.class));
            finish();
        });
    }

    private void registerUser() {
        String email = edtEmail.getText().toString().trim();
        String username = edtUsername.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        // 🔍 Debugging Logs
        Log.d("RegisterDebug", "Email: " + email);
        Log.d("RegisterDebug", "Userame: " + username);
        Log.d("RegisterDebug", "Password: " + password);
        Log.d("RegisterDebug", "Confirm Password: " + confirmPassword);

        if (email.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setUsername(username);
        newUser.setPassword(password);
        newUser.setRole("customer");

        // 🔍 Log to verify that correct values are going into the request
        Log.d("RegisterCheck", "Email: " + newUser.getEmail());
        Log.d("RegisterCheck", "Username: " + newUser.getUsername());

        Call<User> call = userService.register(
                "YOUR_API_KEY_HERE",
                newUser.getEmail(),
                newUser.getUsername(),
                newUser.getPassword(),
                newUser.getRole()
        );


        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                Log.d("RegisterDebug", "Response code: " + response.code());

                if (response.isSuccessful()) {
                    Toast.makeText(Register.this, "Registration successful. Please login.", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(Register.this, Login.class));
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Log.e("RegisterFail", "Error body: " + errorBody);
                        Toast.makeText(Register.this, "Failed: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Log.e("RegisterException", e.toString());
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e("RegisterError", t.getMessage());
                Toast.makeText(Register.this, "Error connecting to server.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
