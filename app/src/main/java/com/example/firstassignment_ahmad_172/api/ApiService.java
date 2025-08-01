package com.example.firstassignment_ahmad_172.api;

import com.example.firstassignment_ahmad_172.api.ApiResponse; // ✅ Corrected import
import com.example.firstassignment_ahmad_172.UserLoginRequest;
import com.example.firstassignment_ahmad_172.model.UserLoginResponse;
import com.example.firstassignment_ahmad_172.model.User;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {

    @GET("users/{id}")
    Call<User> getUserDetails(@Path("id") String userId);

    @POST("users/register")
    Call<ApiResponse> registerUser(@Body User user);

    @POST("users/login")
    Call<UserLoginResponse> loginUser(@Body UserLoginRequest request);

    @POST("users/forgot-password")
    Call<ResponseBody> forgotPassword(@Body Map<String, String> emailRequest);

    @POST("users/reset-password")
    Call<ResponseBody> resetPassword(@Body Map<String, String> resetRequest);

    @POST("users/verify-otp")
    Call<ResponseBody> verifyOTP(@Body Map<String, String> otpRequest);

    @Multipart
    @POST("predict")
    Call<ApiResponse> uploadImage(@Part MultipartBody.Part file); // ✅ Corrected API call
}
