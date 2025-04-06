package com.example.medgenie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class ProfileSettingsActivity extends AppCompatActivity {

    private ImageView profileImage;
    private EditText etUserName;
    private Switch switchDarkMode;
    private Spinner spinnerLanguage;
    private Button btnLogout, btnDeleteAccount;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private FirebaseUser user;
    private SharedPreferences sharedPreferences;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_settings);

        // Initialize Firebase
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        user = auth.getCurrentUser();

        // Initialize UI Elements
        profileImage = findViewById(R.id.profileImage);
        etUserName = findViewById(R.id.etUserName);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        btnLogout = findViewById(R.id.btnLogout);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE);
        loadUserProfile();

        // Setup language spinner
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(adapter);

        // Language Selection
        spinnerLanguage.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                translateApp(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No action needed
            }
        });

        // Dark Mode Toggle
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> toggleDarkMode(isChecked));

        // Logout Button
        btnLogout.setOnClickListener(v -> logoutUser());

        // Delete Account Button
        btnDeleteAccount.setOnClickListener(v -> deleteUserAccount());
    }

    // Load User Data from Firebase
    private void loadUserProfile() {
        if (user != null) {
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    etUserName.setText(documentSnapshot.getString("name"));
                }
            });

            // Check Dark Mode Preference
            boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);
            switchDarkMode.setChecked(isDarkMode);
            if (isDarkMode) enableDarkMode();
        }
    }

    // Change Profile Picture
    public void changeProfilePicture(View view) {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            profileImage.setImageURI(imageUri);

            // Upload Image to Firebase
            StorageReference storageRef = FirebaseStorage.getInstance().getReference("profile_pics/" + user.getUid());
            storageRef.putFile(imageUri).addOnSuccessListener(taskSnapshot ->
                    Toast.makeText(this, "Profile Picture Updated!", Toast.LENGTH_SHORT).show()
            );
        }
    }

    // Enable Dark Mode
    private void enableDarkMode() {
        getWindow().getDecorView().setBackgroundColor(Color.BLACK);
    }

    // Toggle Dark Mode
    private void toggleDarkMode(boolean isChecked) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("dark_mode", isChecked);
        editor.apply();
        if (isChecked) {
            enableDarkMode();
        } else {
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
        }
    }

    // Logout User
    private void logoutUser() {
        auth.signOut();
        startActivity(new Intent(this, Login.class));
        finish();
    }

    // Delete Account
    private void deleteUserAccount() {
        if (user != null) {
            db.collection("users").document(user.getUid()).delete();
            user.delete().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this, "Account Deleted", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, Login.class));
                    finish();
                } else {
                    Toast.makeText(this, "Error deleting account", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    // Dummy Translate Function (Update as per Google Translate API integration)
    private void translateApp(int position) {
        String[] languages = {"en", "es", "fr", "de", "hi"};
        String selectedLang = languages[position];
        Toast.makeText(this, "Language changed to " + selectedLang, Toast.LENGTH_SHORT).show();

        // You can store selectedLang in SharedPreferences or use ML Kit Translate here.
    }
}
