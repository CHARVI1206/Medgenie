package com.example.medgenie;

import com.example.medgenie.GeminiRequest;
import com.example.medgenie.GeminiResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface GeminiApi {
    @POST("v1beta/models/gemini-pro:generateContent")
    Call<GeminiResponse> generateContent(
            @Header("Authorization") String apiKey,
            @Body GeminiRequest request
    );
}
