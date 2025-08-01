package com.example.firstassignment_ahmad_172;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPhone, etPassword;
    private final OkHttpClient client = new OkHttpClient();
    private static final String BASE_URL = "https://talkzilla-backend.onrender.com/api/users/register";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Get UI elements
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etPassword = findViewById(R.id.etPassword);
        MaterialButton btnSignup = findViewById(R.id.btnSignup);

        // Handle SignUp Button Click
        btnSignup.setOnClickListener(v -> registerUser());

        // Navigate to Login Page
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate Inputs
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required!");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(phone)) {
            etPhone.setError("Phone is required!");
            return;
        }
        if (TextUtils.isEmpty(password) || password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters!");
            return;
        }

        // Create JSON body
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("name", name);
            jsonObject.put("email", email);
            jsonObject.put("phone", phone);
            jsonObject.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        // Convert JSON to RequestBody
        RequestBody body = RequestBody.create(jsonObject.toString(), MediaType.get("application/json; charset=utf-8"));

        // Make HTTP request in a separate thread
        new Thread(() -> {
            Request request = new Request.Builder()
                    .url(BASE_URL)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    runOnUiThread(() -> {
                        Toast.makeText(SignupActivity.this, "Signup Successful! Please login.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(SignupActivity.this, MainActivity.class));
                        finish();
                    });
                } else {
                    String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e("Signup", "Error: " + errorMessage);
                    runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Signup Failed: " + errorMessage, Toast.LENGTH_LONG).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(SignupActivity.this, "Network error. Please try again.", Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
