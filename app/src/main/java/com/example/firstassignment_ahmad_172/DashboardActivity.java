package com.example.firstassignment_ahmad_172;
import com.example.firstassignment_ahmad_172.api.ApiResponse;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import com.bumptech.glide.Glide;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firstassignment_ahmad_172.api.ApiService;
import com.example.firstassignment_ahmad_172.api.ApiResponse;
import com.example.firstassignment_ahmad_172.model.ChatAdapter;
import com.example.firstassignment_ahmad_172.model.ChatMessage;
import com.example.firstassignment_ahmad_172.storage.SharedPrefManager;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import android.graphics.Bitmap;
import android.util.Base64;

import org.json.JSONArray;
import org.json.JSONObject;
import android.content.SharedPreferences;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;


import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DashboardActivity extends AppCompatActivity {
    // **✅ Declare imagePickerLauncher here**
    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private static final int PICK_IMAGE_REQUEST = 1;
    private DrawerLayout drawerLayout;
    private RecyclerView chatRecyclerView;
    private EditText messageInput;
    private ImageButton sendButton, imageButton;
    private ImageView userImage;
    private TextView logoutButton;
    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private SharedPreferences sharedPreferences;
    private String userId;  // Move userId initialization inside onCreate
    private Uri selectedImageUri;
    private ImageView imgPreview;
    private Button uploadImage;
    private static final int PICK_PROFILE_IMAGE = 200;
    private static final int PICK_PREVIEW_IMAGE = 100;
    private boolean isWaitingForResponse = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        drawerLayout = findViewById(R.id.drawer_layout);
        chatRecyclerView = findViewById(R.id.chat_list);
        messageInput = findViewById(R.id.message_input);
        sendButton = findViewById(R.id.send_button);
        imageButton = findViewById(R.id.image_button);
        userImage = findViewById(R.id.user_image);

        chatMessages = new ArrayList<>();
        sharedPreferences = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        // Get user ID after initializing sharedPreferences
        userId = sharedPreferences.getString("userId", null);

        if (userId == null) {
            userId = SharedPrefManager.getInstance(this).getUserId(); // Try fetching from SharedPrefManager
        }

        if (userId == null) {
            Toast.makeText(this, "User ID not found. Please re-login.", Toast.LENGTH_SHORT).show();
        }

        // Load saved profile image
        loadProfileImage();

        // Set up RecyclerView
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatMessages);
        chatRecyclerView.setAdapter(chatAdapter);

        // Logout Click
        ImageView logoutButton = findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            new AlertDialog.Builder(DashboardActivity.this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setIcon(R.drawable.logout)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        SharedPrefManager.getInstance(DashboardActivity.this).logout();
                        startActivity(new Intent(DashboardActivity.this, MainActivity.class));
                        finish();
                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // Open Image Picker when clicking user image
        userImage.setOnClickListener(v -> openProfileImagePicker());

        // 📌 **Initialize Image Picker**
        // Update your imagePickerLauncher initialization:
        // In your Activity or Fragment
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            // Update both image views immediately
                            userImage.setImageURI(selectedImageUri);

                            // Also update the dialog's image if it's open
                            ShapeableImageView dialogImage = findViewById(R.id.dialog_profile_image);
                            if (dialogImage != null) {
                                dialogImage.setImageURI(selectedImageUri);
                            }

                            // Upload to backend
                            try {
                                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                        getContentResolver(), selectedImageUri);
                                uploadImageToBackend(bitmap);
                            } catch (IOException e) {
                                Log.e("ImagePicker", "Error loading image", e);
                            }
                        }
                    }
                }
        );
        // **Send Text Message**
        sendButton.setOnClickListener(v -> {
            String message = messageInput.getText().toString().trim();

            if (message.isEmpty()) {
                showEmptyMessageError();
                return;
            }

            if (userId == null) {
                Toast.makeText(DashboardActivity.this, "User ID not found. Please re-login.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isWaitingForResponse) {
                return; // Prevent multiple sends
            }

            chatMessages.add(new ChatMessage(message, true));
            chatAdapter.notifyDataSetChanged();
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            messageInput.setText("");
            sendMessageToBackend(message, userId);
        });

        // **Upload Image - Open `upload_image_dialog` when clicking `imageButton`**
        imageButton.setOnClickListener(v -> openImageUploadDialog());
    }
    private void showEmptyMessageError() {
        Toast toast = Toast.makeText(getApplicationContext(), "Please enter some message", Toast.LENGTH_SHORT);

        // Position the toast at the top
        toast.setGravity(Gravity.TOP | Gravity.FILL_HORIZONTAL, 0, 150);

        // Show the toast
        toast.show();
    }

    private void showInvalidImageMessage() {
        new Handler(Looper.getMainLooper()).post(() -> {
            LayoutInflater inflater = LayoutInflater.from(DashboardActivity.this);
            View view = inflater.inflate(R.layout.custom_alert_dialog, null);

            // Create the AlertDialog
            AlertDialog alertDialog = new MaterialAlertDialogBuilder(DashboardActivity.this)
                    .setView(view)
                    .create();

            alertDialog.show();

            // Set dialog window size
            alertDialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            // Find the OK button inside the custom layout
            Button btnOk = view.findViewById(R.id.btnOoK);
            btnOk.setOnClickListener(v -> alertDialog.dismiss());  // Close the dialog when clicked
        });
    }



    /**
     * Opens the image upload dialog.
     */
    private void openImageUploadDialog() {
        // Create a Dialog
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.upload_image_dialog);

        // Set full width for the dialog
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT)); // Transparent background
        }

        dialog.setCancelable(true);

        // Get Views
        ImageView btnClose = dialog.findViewById(R.id.btnClose);
        Button chooseImage = dialog.findViewById(R.id.btnChooseImage);
        Button uploadImage = dialog.findViewById(R.id.btnUpload);
        ImageView imgPreview = dialog.findViewById(R.id.imgPreview);

        // Initially hide upload button
        uploadImage.setVisibility(View.GONE);

        // Handle Close Button
        btnClose.setOnClickListener(v -> dialog.dismiss());

        // Handle Choose Image
        chooseImage.setOnClickListener(v -> {
            openImagePicker2(imgPreview, uploadImage); // Calls function to pick an image
        });

        // Handle Upload Image
        uploadImage.setOnClickListener(v -> {
            uploadSelectedImage(imgPreview); // Calls function to send image to backend
            dialog.dismiss(); // Close the dialog after selecting the image
        });

        // Show the Dialog
        dialog.show();
    }


    private void openImagePicker2(ImageView imgPreview, Button uploadImage) {
        this.imgPreview = imgPreview; // Store reference
        this.uploadImage = uploadImage; // Store reference

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_PREVIEW_IMAGE); // Start image picker intent
    }



    private void uploadSelectedImage(ImageView imgPreview) {
        if (selectedImageUri == null) {
            Toast.makeText(this, "No image selected!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Failed to get image stream!", Toast.LENGTH_SHORT).show();
                return;
            }

            File tempFile = new File(getCacheDir(), "upload_image.jpg");
            FileOutputStream outputStream = new FileOutputStream(tempFile);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.close();
            inputStream.close();

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), tempFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("file", tempFile.getName(), requestFile);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("https://Ahmadft-mastitis-detection.hf.space/")  // Ensure correct URL
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(new OkHttpClient.Builder()
                            .connectTimeout(60, TimeUnit.SECONDS)
                            .readTimeout(60, TimeUnit.SECONDS)
                            .writeTimeout(60, TimeUnit.SECONDS)
                            .retryOnConnectionFailure(true)
                            .build())
                    .build();

            ApiService apiService = retrofit.create(ApiService.class);
            Call<ApiResponse> call = apiService.uploadImage(body);

            call.enqueue(new Callback<ApiResponse>() {
                @Override
                public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        ApiResponse apiResponse = response.body();

                        if (apiResponse.getError() != null && apiResponse.getError().equals("Invalid Image")) {

                            showInvalidImageMessage();
                            return; // Stop further processing
                        }

                        // ✅ Valid image, show chatbot response
                        showChatbotResponse(apiResponse);
                    } else {
                        // ❌ Handle HTTP error responses (e.g., 400 Bad Request)
                        if (response.code() == 400) {

                            showInvalidImageMessage(); // Show error in frontend


                        } else {
                            Toast.makeText(DashboardActivity.this, "Error: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }


                @Override
                public void onFailure(Call<ApiResponse> call, Throwable t) {
                    Log.e("API_ERROR", "Upload Failed", t);
                    Toast.makeText(DashboardActivity.this, "Upload Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            Log.e("API_ERROR", "Error processing image", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void showChatbotResponse(ApiResponse response) {
        runOnUiThread(() -> {
            if (selectedImageUri != null) {
                chatMessages.add(new ChatMessage(selectedImageUri.toString(), true, true));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }

            // Handle invalid image case
            if (response.getMessage().contains("❌ The uploaded image is not a cow teat")) {
                chatMessages.add(new ChatMessage("<b>Error:</b> 🚫 " + response.getMessage(), false));
                chatAdapter.notifyItemInserted(chatMessages.size() - 1);
                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                return; // Stop further processing
            }

            // Format response for valid cases
            String formattedResponse = "<b><big>" + response.getStatus() + " 📝</big></b><br>" +
                    "<b>Message:</b> " + response.getMessage() + " 😊<br><br>" +
                    "<b>Recommendations:</b><br>";

            for (String recommendation : response.getRecommendations()) {
                formattedResponse += "• " + "💡 " + recommendation + " 🎯" + "<br>";
            }

            formattedResponse += "<br><b>Suggested Medicine:</b> 💊 " + response.getSuggestedMedicine() + " 💉";

            String finalFormattedResponse = formattedResponse;

            // Simulate "Typing..." effect before showing response
            chatMessages.add(new ChatMessage("Typing... 🖊️", false));
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                showTypingEffect(finalFormattedResponse);
            }, 1500);
        });
    }



    // Method to display chatbot response with a typing effect
    private void showTypingEffect(String fullResponse) {
        new Thread(() -> {
            StringBuilder typedText = new StringBuilder();
            int lastIndex = chatMessages.size() - 1; // Get index of the "Typing..." message

            for (char c : fullResponse.toCharArray()) {
                typedText.append(c);
                String finalText = typedText.toString();

                runOnUiThread(() -> {
                    if (!chatMessages.isEmpty() && lastIndex < chatMessages.size()) {
                        chatMessages.set(lastIndex, new ChatMessage(finalText, false)); // Update the last message
                        chatAdapter.notifyItemChanged(lastIndex);

                        // **Auto-scroll while typing**
                        chatRecyclerView.smoothScrollToPosition(lastIndex);
                    }
                });

                try {
                    Thread.sleep(10); // Adjust typing speed (50ms per character)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }


    private String getRealPathFromURI(Uri contentUri) {
        String filePath = null;
        String[] projection = {MediaStore.Images.Media.DATA};

        try (Cursor cursor = getContentResolver().query(contentUri, projection, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                filePath = cursor.getString(columnIndex);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (filePath == null) {
            // If the direct file path is null, use FileDescriptor method
            return getFilePathFromContentUri(contentUri);
        }
        return filePath;
    }

    // **Alternative method for Android 10+ scoped storage**
    private String getFilePathFromContentUri(Uri contentUri) {
        File file = new File(getCacheDir(), "temp_image.jpg");
        try (InputStream inputStream = getContentResolver().openInputStream(contentUri);
             OutputStream outputStream = new FileOutputStream(file)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return file.getAbsolutePath();
    }
    private void addMessageToChat(String message, boolean isUser) {
        if (chatAdapter == null) {
            return; // Prevent crashes if the adapter is not initialized
        }

        runOnUiThread(() -> {
            chatMessages.add(new ChatMessage(message, isUser)); // Add text message
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        });
    }

    private void addImageToChat(String imageUrl, boolean isUser) {
        if (chatAdapter == null) {
            return; // Prevent crashes if the adapter is not initialized
        }

        runOnUiThread(() -> {
            chatMessages.add(new ChatMessage(imageUrl, isUser, true)); // Add image message
            chatAdapter.notifyItemInserted(chatMessages.size() - 1);
            chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
        });
    }

    // 📌 Open Image Picker

    private void openProfileImagePicker() {
        // Show the profile edit dialog instead of directly opening image picker
        showProfileEditDialog();
    }

    private void showProfileEditDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_profile_edit, null);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        // Initialize views
        ShapeableImageView profileImage = dialogView.findViewById(R.id.dialog_profile_image);
        ImageButton editImageButton = dialogView.findViewById(R.id.edit_image_button);
        TextInputEditText etName = dialogView.findViewById(R.id.et_name);
        TextInputEditText etEmail = dialogView.findViewById(R.id.et_email);
        TextInputEditText etPhone = dialogView.findViewById(R.id.et_phone);
        MaterialButton btnSave = dialogView.findViewById(R.id.btn_save);
        MaterialButton btnCancel = dialogView.findViewById(R.id.btn_cancel);

        // Load current user data
        loadCurrentUserData(etName, etEmail, etPhone, profileImage);

        // Set click listeners
        editImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        btnSave.setOnClickListener(v -> {
            // Save updated user data
            saveUserData(etName.getText().toString(),
                    etEmail.getText().toString(),
                    etPhone.getText().toString());
            dialog.dismiss();
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();

        // Make dialog background transparent
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    private void loadCurrentUserData(TextInputEditText etName,
                                     TextInputEditText etEmail,
                                     TextInputEditText etPhone,
                                     ShapeableImageView profileImage) {
        // Get cached data first for quick display
        String cachedName = sharedPreferences.getString("user_name", "");
        String cachedEmail = sharedPreferences.getString("user_email", "");
        String cachedPhone = sharedPreferences.getString("user_phone", "");
        String cachedImageUrl = sharedPreferences.getString("profile_image_url", null);

        etName.setText(cachedName);
        etEmail.setText(cachedEmail);
        etPhone.setText(cachedPhone);

        // Load cached image if available
        if (cachedImageUrl != null && !cachedImageUrl.isEmpty()) {
            loadImageWithGlide(cachedImageUrl, profileImage);
        }

        // Then load fresh data from API
        String userId = SharedPrefManager.getInstance(this).getUserId();
        if (userId != null) {
            fetchUserDataFromAPI(userId, etName, etEmail, etPhone, profileImage);
        }
    }

    private void loadImageWithGlide(String imageUrl, ImageView imageView) {
        Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user) // Fallback if image fails to load
                .into(imageView);
    }

    private void fetchUserDataFromAPI(String userId,
                                      TextInputEditText etName,
                                      TextInputEditText etEmail,
                                      TextInputEditText etPhone,
                                      ShapeableImageView profileImage) {
        new Thread(() -> {
            try {
                URL url = new URL("https://talkzilla-backend.onrender.com/api/users/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(10000); // 10 seconds timeout
                conn.setReadTimeout(10000);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
                br.close();

                JSONObject userJson = new JSONObject(response.toString());
                runOnUiThread(() -> {
                    try {
                        String name = userJson.optString("name", "");
                        String email = userJson.optString("email", "");
                        String phone = userJson.optString("phone", "");
                        String imageUrl = userJson.optString("image_url", "");

                        etName.setText(name);
                        etEmail.setText(email);
                        etPhone.setText(phone);

                        if (!imageUrl.isEmpty()) {
                            // Ensure URL is properly formatted
                            if (!imageUrl.startsWith("http")) {
                                imageUrl = "https://talkzilla-backend.onrender.com" + imageUrl;
                            }

                            loadImageWithGlide(imageUrl, profileImage);
                            loadImageWithGlide(imageUrl, userImage); // Update main profile image

                            // Save to SharedPreferences
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("user_name", name);
                            editor.putString("user_email", email);
                            editor.putString("user_phone", phone);
                            editor.putString("profile_image_url", imageUrl);
                            editor.apply();
                        }
                    } catch (Exception e) {
                        Log.e("Profile", "Error parsing user data", e);
                    }
                });
            } catch (Exception e) {
                Log.e("Profile", "Error fetching user data", e);
            }
        }).start();
    }
    private void saveUserData(String name, String email, String phone) {
        // Save to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("user_name", name);
        editor.putString("user_email", email);
        editor.putString("user_phone", phone);
        editor.apply();

        // TODO: Update via API
        updateUserOnBackend(name, email, phone);
    }

    private void updateUserOnBackend(String name, String email, String phone) {
        // Implement your API call to update user details
        // This is just a placeholder
        new Thread(() -> {
            try {
                // Your API implementation here
            } catch (Exception e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    // 📌 Handle Image Selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d("ImagePicker", "onActivityResult called with requestCode: " + requestCode);

        if (resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData(); // Store the selected image globally
            Log.d("ImagePicker", "Image URI: " + selectedImageUri);

            if (selectedImageUri != null) {
                if (requestCode == PICK_PROFILE_IMAGE) {
                    // Handle Profile Image Selection
                    userImage.setImageURI(selectedImageUri);
                    Log.d("ImagePicker", "Profile image updated");

                    // Convert URI to Bitmap and upload
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), selectedImageUri);
                        uploadImageToBackend(bitmap);
                    } catch (IOException e) {
                        Log.e("ImagePicker", "Error converting URI to Bitmap", e);
                    }

                } else if (requestCode == PICK_PREVIEW_IMAGE) {
                    // Handle Preview Image Selection
                    imgPreview.setImageURI(selectedImageUri);
                    uploadImage.setVisibility(View.VISIBLE);
                    Log.d("ImagePicker", "Image preview updated and upload button visible");
                }
            } else {
                Log.e("ImagePicker", "selectedImageUri is null!");
            }
        }
    }



    // 📌 Save Image to Shared Preferences
    private void uploadImageToBackend(Bitmap bitmap) {
        new Thread(() -> {
            try {
                URL url = new URL("https://talkzilla-backend.onrender.com/api/users/upload");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=*****");
                conn.setDoOutput(true);
                conn.setDoInput(true);

                // Convert Bitmap to ByteArray
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] imageBytes = byteArrayOutputStream.toByteArray();

                // Get user_id from SharedPreferences or any storage
                String userId = SharedPrefManager.getInstance(this).getUserId();

                // Write Multipart Form Data
                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());

                // Send user_id
                dos.writeBytes("--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"user_id\"\r\n\r\n");
                dos.writeBytes(userId + "\r\n");

                // Send image file
                dos.writeBytes("--*****\r\n");
                dos.writeBytes("Content-Disposition: form-data; name=\"image\"; filename=\"profile.png\"\r\n");
                dos.writeBytes("Content-Type: image/png\r\n\r\n");
                dos.write(imageBytes);
                dos.writeBytes("\r\n--*****--\r\n");

                dos.flush();
                dos.close();

                // Read Response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    responseBuilder.append(response);
                }
                br.close();
                conn.disconnect();

                // Parse Response
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                String imageUrl = jsonResponse.getString("image_url");

                // Save Image URL
                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("profile_image_url", imageUrl);
                    editor.apply();
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    // 📌 Load & Set Profile Image
    private void loadProfileImage() {
        new Thread(() -> {
            try {
                // Get user ID
                String userId = SharedPrefManager.getInstance(this).getUserId();

                // Create API URL using path parameter
                URL url = new URL("https://talkzilla-backend.onrender.com/api/users/get-profile-image/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setDoInput(true);

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    responseBuilder.append(response);
                }
                br.close();
                conn.disconnect();

                // Parse JSON response
                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                String imageUrl = jsonResponse.getString("image_url");

                // Save image URL & Load with Glide
                runOnUiThread(() -> {
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("profile_image_url", imageUrl);
                    editor.apply();

                    Glide.with(this)
                            .load(imageUrl)
                            .placeholder(R.drawable.user) // Placeholder
                            .into(userImage);
                });

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }


    private void sendMessageToBackend(String message, String userId) {
        // Disable send button and show loading state
        runOnUiThread(() -> {
            isWaitingForResponse = true;
            sendButton.setEnabled(false);
            sendButton.setAlpha(0.5f);
            sendButton.setImageResource(R.drawable.disabled);
        });

        new Thread(() -> {
            try {
                // API Call to Backend
                URL url = new URL("https://talkzilla-backend.onrender.com/api/chatbot");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(10000); // 10 seconds timeout
                conn.setReadTimeout(10000);

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("userId", userId);
                jsonObject.put("message", message);

                OutputStream os = conn.getOutputStream();
                os.write(jsonObject.toString().getBytes());
                os.flush();
                os.close();

                // Read response
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    responseBuilder.append(response);
                }
                br.close();
                conn.disconnect();

                JSONObject jsonResponse = new JSONObject(responseBuilder.toString());
                String botMessage = jsonResponse.getString("response");

                // Handle successful response
                runOnUiThread(() -> {
                    ChatMessage chatMessage = new ChatMessage("", false);
                    chatMessages.add(chatMessage);
                    chatAdapter.notifyDataSetChanged();
                    chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);

                    // Typing effect
                    new Thread(() -> {
                        for (int i = 0; i < botMessage.length(); i++) {
                            final String partialMessage = botMessage.substring(0, i + 1);

                            runOnUiThread(() -> {
                                chatMessage.setMessage(partialMessage);
                                chatAdapter.notifyDataSetChanged();
                                chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                            });

                            try {
                                Thread.sleep(30); // Typing speed
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }

                        // Re-enable send button after typing completes
                        runOnUiThread(() -> {
                            isWaitingForResponse = false;
                            sendButton.setEnabled(true);
                            sendButton.setAlpha(1f);
                            sendButton.setImageResource(R.drawable.ic_send);
                        });
                    }).start();
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    // Handle error state
                    isWaitingForResponse = false;
                    sendButton.setEnabled(true);
                    sendButton.setAlpha(1f);
                    sendButton.setImageResource(R.drawable.ic_send);

                    // Show error message
                    Toast.makeText(DashboardActivity.this,
                            "Failed to send message: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();


                });
            }
        }).start();
    }
    private void loadChatHistory() {
        new Thread(() -> {
            try {
                URL url = new URL("https://talkzilla-backend.onrender.com/api/chatbot/history/" + userId);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoInput(true);

                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder responseBuilder = new StringBuilder();
                String response;
                while ((response = br.readLine()) != null) {
                    responseBuilder.append(response);
                }
                br.close();
                conn.disconnect();

                JSONArray chatArray = new JSONArray(responseBuilder.toString());
                List<ChatMessage> chatList = new ArrayList<>();

                for (int i = 0; i < chatArray.length(); i++) {
                    JSONObject chatObject = chatArray.getJSONObject(i);

                    // Extract user message and bot response
                    String userMessage = chatObject.optString("userMessage", "").trim();
                    String botResponse = chatObject.optString("botResponse", "").trim();

                    // Add user message
                    if (!userMessage.isEmpty()) {
                        chatList.add(new ChatMessage(userMessage, true)); // true means user message
                    }

                    // Add bot response
                    if (!botResponse.isEmpty()) {
                        chatList.add(new ChatMessage(botResponse, false)); // false means bot response
                    }
                }

                runOnUiThread(() -> {
                    chatMessages.clear();
                    chatMessages.addAll(chatList);
                    chatAdapter.notifyDataSetChanged();

                    if (!chatMessages.isEmpty()) {
                        chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                    }
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> Toast.makeText(DashboardActivity.this, "Error loading chat history", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (userId != null) {
            loadChatHistory(); // Load chat history when activity resumes
        }
    }

}