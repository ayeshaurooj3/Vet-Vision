package com.example.firstassignment_ahmad_172;

import android.content.Intent;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import androidx.appcompat.app.AppCompatActivity;

import com.example.firstassignment_ahmad_172.api.ApiService;
import com.example.firstassignment_ahmad_172.api.RetrofitClient;
import com.example.firstassignment_ahmad_172.model.User;
import com.example.firstassignment_ahmad_172.storage.SharedPrefManager;
import com.google.android.material.button.MaterialButton;
import com.example.firstassignment_ahmad_172.model.UserLoginResponse;  // ✅ Correct import
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.HashMap;
import java.util.Map;
import okhttp3.ResponseBody;

public class MainActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private TextView btnForgotPassword, tvSignup;
    private ApiService apiService; // Retrofit API Service

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize API Service
        apiService = RetrofitClient.getApiService();

        // Get UI elements
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        TextView tvSignup = findViewById(R.id.tvSignup);


        // Check if user is already logged in
        if (SharedPrefManager.getInstance(this).getUserId() != null) {
            Log.d("LOGIN", "User already logged in, redirecting to Dashboard.");
            startActivity(new Intent(MainActivity.this, DashboardActivity.class));
            finish();
            return;
        }

        // Navigate to SignupActivity
        tvSignup.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, SignupActivity.class));
            finish();
        });
        btnForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        // Handle Login Button Click
        btnLogin.setOnClickListener(v -> loginUser());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        // Validate Input
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required!");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required!");
            return;
        }

        // Create Login Request
        UserLoginRequest loginRequest = new UserLoginRequest(email, password);

        Call<UserLoginResponse> call = apiService.loginUser(loginRequest);
        call.enqueue(new Callback<UserLoginResponse>() {
            @Override
            public void onResponse(Call<UserLoginResponse> call, Response<UserLoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserLoginResponse loginResponse = response.body();
                    Log.d("LOGIN", "Full response: " + loginResponse.toString());

                    User user = loginResponse.getUser();
                    if (user != null) {
                        Log.d("LOGIN", "User object received. ID: " + user.getId());

                        // Save user
                        SharedPrefManager.getInstance(MainActivity.this).saveUser(user);

                        // Verify immediately
                        String savedId = SharedPrefManager.getInstance(MainActivity.this).getUserId();
                        Log.d("LOGIN", "Saved and retrieved ID: " + savedId);

                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(MainActivity.this, DashboardActivity.class));
                        finish();
                    } else {
                        Log.e("LOGIN", "User object is null in response");
                        Toast.makeText(MainActivity.this, "User data not found!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("LOGIN", "Error response: " + response.code());
                    Toast.makeText(MainActivity.this, "Invalid Credentials!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<UserLoginResponse> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Login Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("LOGIN", "API Failure", t);
            }
        });
    }
    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_forgot_password, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().setDimAmount(0.6f);
        }

        EditText emailInput = dialogView.findViewById(R.id.etEmail);
        EditText otpInput = dialogView.findViewById(R.id.etOTP);
        EditText newPasswordInput = dialogView.findViewById(R.id.etNewPassword);

        Button btnReset = dialogView.findViewById(R.id.btnReset);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        Button btnVerifyOTP = dialogView.findViewById(R.id.btnVerifyOTP);
        Button btnSubmitNewPassword = dialogView.findViewById(R.id.btnSubmitNewPassword);

        // Layout containers for hiding/showing
        View tilOTP = dialogView.findViewById(R.id.tilOTP);
        View tilNewPassword = dialogView.findViewById(R.id.tilNewPassword);

        // Initially hide OTP and password fields
        tilOTP.setVisibility(View.GONE);
        tilNewPassword.setVisibility(View.GONE);
        btnVerifyOTP.setVisibility(View.GONE);
        btnSubmitNewPassword.setVisibility(View.GONE);

        // Step 1: Send Reset Password Request
        btnReset.setOnClickListener(v -> {
            String email = emailInput.getText().toString().trim();
            if (!TextUtils.isEmpty(email)) {
                sendResetPasswordRequest(email, dialog);

                // ✅ Make OTP field and verify button visible
                tilOTP.setVisibility(View.VISIBLE);
                btnVerifyOTP.setVisibility(View.VISIBLE);
                btnReset.setVisibility(View.GONE); // Hide Reset button
            } else {
                Toast.makeText(this, "Email is required", Toast.LENGTH_SHORT).show();
            }
        });

        // Step 2: Verify OTP
        btnVerifyOTP.setOnClickListener(v -> {
            String otp = otpInput.getText().toString().trim();
            if (!TextUtils.isEmpty(otp)) {
                verifyOTP(emailInput.getText().toString().trim(), otp, dialog, newPasswordInput, btnSubmitNewPassword, btnVerifyOTP);

            } else {
                Toast.makeText(this, "OTP is required", Toast.LENGTH_SHORT).show();
            }
        });

        // Step 3: Submit New Password
        btnSubmitNewPassword.setOnClickListener(v -> {
            String newPassword = newPasswordInput.getText().toString().trim();
            String otp = otpInput.getText().toString().trim(); // ✅ Get OTP from input field
            String email = emailInput.getText().toString().trim();

            if (!TextUtils.isEmpty(newPassword) && !TextUtils.isEmpty(otp)) {
                submitNewPassword(email, otp, newPassword, dialog);
            } else {
                Toast.makeText(this, "OTP and New password are required", Toast.LENGTH_SHORT).show();
            }
        });

        // Cancel Button
        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }


    private void sendResetPasswordRequest(String email, AlertDialog dialog) {
        Map<String, String> emailRequest = new HashMap<>();
        emailRequest.put("email", email);

        apiService.forgotPassword(emailRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "OTP sent to email!", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(MainActivity.this, "User not found!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Request Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void verifyOTP(String email, String otp,
                           AlertDialog dialog,
                           EditText newPasswordInput,
                           Button btnSubmitNewPassword,
                           Button btnVerifyOTP) {
        Map<String, String> otpRequest = new HashMap<>();
        otpRequest.put("email", email);
        otpRequest.put("otp", otp);

        apiService.verifyOTP(otpRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "OTP verified!", Toast.LENGTH_LONG).show();

                    // ✅ Ensure both EditText & TextInputLayout are visible
                    View tilNewPassword = dialog.findViewById(R.id.tilNewPassword); // Get the TextInputLayout

                    if (tilNewPassword != null) {
                        tilNewPassword.setVisibility(View.VISIBLE); // Show the TextInputLayout
                    }
                    newPasswordInput.setVisibility(View.VISIBLE); // Show the password EditText
                    btnSubmitNewPassword.setVisibility(View.VISIBLE); // Show the submit button
                    btnVerifyOTP.setVisibility(View.GONE); // Hide OTP button after success
                } else {
                    Toast.makeText(MainActivity.this, "Invalid OTP!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Request Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void submitNewPassword(String email, String otp, String newPassword, AlertDialog dialog) {
        Map<String, String> passwordRequest = new HashMap<>();
        passwordRequest.put("email", email);
        passwordRequest.put("otp", otp);
        passwordRequest.put("newPassword", newPassword);

        apiService.resetPassword(passwordRequest).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(MainActivity.this, "Password reset successfully!", Toast.LENGTH_LONG).show();
                    dialog.dismiss(); // Close dialog after success
                } else {
                    Toast.makeText(MainActivity.this, "Failed to reset password!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Request Failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }



}
