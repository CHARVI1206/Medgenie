package com.example.medgenie;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.FirebaseApp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONObject;


import java.util.UUID;

public class AiDoctorActivity extends AppCompatActivity {

    private EditText inputSymptoms;
    private Button analyzeSymptoms, uploadImage, uploadReport, getDiagnosis;
    private ImageView uploadedImage;
    private TextView aiResponse;
    private Uri imageUri;
    private FirebaseStorage storage;
    private StorageReference storageRef;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int PICK_REPORT_REQUEST = 2;
    private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY"; // Replace with your real Gemini API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_doctor);

        FirebaseApp.initializeApp(this);
        storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        inputSymptoms = findViewById(R.id.inputSymptoms);
        analyzeSymptoms = findViewById(R.id.analyzeSymptoms);
        uploadImage = findViewById(R.id.uploadImage);
        uploadReport = findViewById(R.id.uploadReport);
        getDiagnosis = findViewById(R.id.getDiagnosis);
        uploadedImage = findViewById(R.id.uploadedImage);
        aiResponse = findViewById(R.id.aiResponse);

        analyzeSymptoms.setOnClickListener(v -> analyzeSymptoms());
        uploadImage.setOnClickListener(v -> selectImage());
        uploadReport.setOnClickListener(v -> selectReport());
        getDiagnosis.setOnClickListener(v -> getAiDiagnosis());
    }

    private void analyzeSymptoms() {
        String symptoms = inputSymptoms.getText().toString().trim();
        if (!symptoms.isEmpty()) {
            sendToGemini(symptoms);
        } else {
            aiResponse.setText("Please enter symptoms before analysis.");
        }
    }

    private void getAiDiagnosis() {
        aiResponse.setText("Fetching AI diagnosis...");
        analyzeSymptoms();
    }

    private void selectImage() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private void selectReport() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        startActivityForResult(intent, PICK_REPORT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            Uri uri = data.getData();

            if (requestCode == PICK_IMAGE_REQUEST) {
                imageUri = uri;
                uploadedImage.setImageURI(uri);
                uploadedImage.setVisibility(View.VISIBLE);
                uploadImageToFirebase(uri);
            } else if (requestCode == PICK_REPORT_REQUEST) {
                uploadReportToFirebase(uri);
            }
        }
    }

    private void uploadImageToFirebase(Uri uri) {
        StorageReference imgRef = storageRef.child("images/" + UUID.randomUUID().toString());

        imgRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> aiResponse.setText("Image uploaded successfully."))
                .addOnFailureListener(e -> aiResponse.setText("Image upload failed: " + e.getMessage()));
    }

    private void uploadReportToFirebase(Uri uri) {
        StorageReference reportRef = storageRef.child("reports/" + UUID.randomUUID().toString());

        reportRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> aiResponse.setText("Report uploaded successfully."))
                .addOnFailureListener(e -> aiResponse.setText("Report upload failed: " + e.getMessage()));
    }

    private void sendToGemini(String text) {
        new Thread(() -> {
            OkHttpClient client = new OkHttpClient();
            JSONObject json = new JSONObject();
            try {
                json.put("prompt", text);
                json.put("temperature", 0.7);
                json.put("max_tokens", 100);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.parse("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1/models/gemini-pro:generateText?key=" + GEMINI_API_KEY)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                runOnUiThread(() -> aiResponse.setText(responseBody));
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> aiResponse.setText("Error processing request"));
            }
        }).start();
    }

}
