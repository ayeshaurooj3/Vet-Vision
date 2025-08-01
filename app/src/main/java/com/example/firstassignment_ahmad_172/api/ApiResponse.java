package com.example.firstassignment_ahmad_172.api;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ApiResponse {
    @SerializedName("message")
    private String message;

    @SerializedName("prediction_score")
    private float predictionScore;

    @SerializedName("recommendations")
    private List<String> recommendations;

    @SerializedName("status")
    private String status;

    @SerializedName("suggested_medicine")
    private String suggestedMedicine;

    @SerializedName("error")
    private String error;  // 🔹 Added error field

    // Getters
    public String getMessage() { return message; }
    public float getPredictionScore() { return predictionScore; }
    public List<String> getRecommendations() { return recommendations; }
    public String getStatus() { return status; }
    public String getSuggestedMedicine() { return suggestedMedicine; }
    public String getError() { return error; }
}
