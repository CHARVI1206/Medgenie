package com.example.medgenie;

import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import com.example.medgenie.BuildConfig;

public class PregnancyTrackerActivity extends AppCompatActivity {

    private Spinner spinnerTrimester, spinnerGender;
    private EditText etBabyAge;
    private TextView tvHealthTips, tvNutritionAdvice;
    private Button btnGetHealthTips, btnSetReminder, btnGetNutrition;

    private FirebaseFirestore db;

    // ✅ Use API key from BuildConfig
    private static final String apiKey = BuildConfig.GEMINI_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pregnancy_tracker);

        spinnerTrimester = findViewById(R.id.spinnerTrimester);
        spinnerGender = findViewById(R.id.spinnerGender);
        etBabyAge = findViewById(R.id.etBabyAge);
        tvHealthTips = findViewById(R.id.tvHealthTips);
        tvNutritionAdvice = findViewById(R.id.tvNutritionAdvice);
        btnGetHealthTips = findViewById(R.id.btnGetHealthTips);
        btnSetReminder = findViewById(R.id.btnSetReminder);
        btnGetNutrition = findViewById(R.id.btnGetNutrition);

        db = FirebaseFirestore.getInstance();

        btnGetHealthTips.setOnClickListener(view -> getTrimesterHealthTips());
        btnSetReminder.setOnClickListener(view -> setDoctorReminder());
        btnGetNutrition.setOnClickListener(view -> getBabyNutritionAdvice());
    }

    // ✅ Fetch AI-Based Pregnancy Health Tips
    private void getTrimesterHealthTips() {
        String trimester = spinnerTrimester.getSelectedItem().toString();

        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();
                String jsonRequest = new Gson().toJson(new GeminiRequest("Pregnancy health tips for " + trimester));

                Request request = new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-pro:generateText?key=" + apiKey)
                        .post(RequestBody.create(jsonRequest, MediaType.parse("application/json")))
                        .build();

                Response response = client.newCall(request).execute();
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> tvHealthTips.setText(parseGeminiResponse(responseBody)));
                } else {
                    runOnUiThread(() -> tvHealthTips.setText("Failed to fetch tips."));
                }
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> tvHealthTips.setText("Error occurred while fetching tips."));
            }
        }).start();
    }

    // ✅ Set Doctor Appointment Reminder
    private void setDoctorReminder() {
        Calendar beginTime = Calendar.getInstance();
        beginTime.add(Calendar.DAY_OF_MONTH, 7); // Reminder for 7 days later

        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, beginTime.getTimeInMillis() + 60 * 60 * 1000)
                .putExtra(CalendarContract.Events.TITLE, "Doctor Appointment")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Prenatal check-up reminder")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "Hospital/Clinic");

        startActivity(intent);
    }

    // ✅ Fetch Baby Nutrition Advice from Firestore
    private void getBabyNutritionAdvice() {
        String age = etBabyAge.getText().toString();
        String gender = spinnerGender.getSelectedItem().toString();

        if (age.isEmpty()) {
            Toast.makeText(this, "Enter baby's age!", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("baby_nutrition").whereEqualTo("age", age).whereEqualTo("gender", gender)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        String advice = task.getResult().getDocuments().get(0).getString("advice");
                        tvNutritionAdvice.setText("🍎 Nutrition Advice: " + advice);
                    } else {
                        tvNutritionAdvice.setText("No advice available for this age & gender.");
                    }
                });
    }

    // ✅ Parse Gemini API Response
    private String parseGeminiResponse(String responseBody) {
        // You might want to use a more robust parser here (e.g., Gson)
        int start = responseBody.indexOf("text") + 8;
        int end = responseBody.lastIndexOf("\"");
        return start >= 0 && end > start ? responseBody.substring(start, end) : "No valid response";
    }

    // ✅ Request model for Gemini API
    static class GeminiRequest {
        final String prompt;

        GeminiRequest(String query) {
            this.prompt = query;
        }
    }
}
