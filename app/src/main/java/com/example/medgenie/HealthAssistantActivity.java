package com.example.medgenie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;


public class HealthAssistantActivity extends AppCompatActivity {

    private EditText etSymptomInput;
    private TextView tvDiagnosisResult;
    private Button btnGetDiagnosis, btnFindHospitals;

    private FirebaseFirestore db;
    private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health_assistant);

        etSymptomInput = findViewById(R.id.etSymptomInput);
        tvDiagnosisResult = findViewById(R.id.tvDiagnosisResult);
        btnGetDiagnosis = findViewById(R.id.btnGetDiagnosis);
        btnFindHospitals = findViewById(R.id.btnFindHospitals);

        db = FirebaseFirestore.getInstance();

        // Button Listeners
        btnGetDiagnosis.setOnClickListener(view -> getAIHealthDiagnosis());
        btnFindHospitals.setOnClickListener(view -> openGoogleMapsForHospitals());
    }

    // Call Gemini API for AI Health Diagnosis
    private void getAIHealthDiagnosis() {
        String symptoms = etSymptomInput.getText().toString();
        if (symptoms.isEmpty()) {
            Toast.makeText(this, "Enter symptoms first!", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String jsonRequest = new Gson().toJson(new GeminiRequest(symptoms));

                Request request = new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateText?key=" + GEMINI_API_KEY)
                        .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> tvDiagnosisResult.setText(parseGeminiResponse(responseBody)));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Failed to get diagnosis!", Toast.LENGTH_SHORT).show());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Parse Gemini AI Response
    private String parseGeminiResponse(String responseBody) {
        try {
            JSONObject json = new JSONObject(responseBody);
            return json.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("output")
                    .getString("text");
        } catch (Exception e) {
            e.printStackTrace();
            return "Could not parse diagnosis.";
        }
    }


    // Open Google Maps for Nearest Hospitals
    private void openGoogleMapsForHospitals() {
        Uri gmmIntentUri = Uri.parse("geo:0,0?q=nearest hospitals");
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    // Request Object for Gemini API
    static class GeminiRequest {
        final String prompt;

        GeminiRequest(String symptoms) {
            this.prompt = "Analyze these symptoms and suggest a possible diagnosis: " + symptoms;
        }
    }
}