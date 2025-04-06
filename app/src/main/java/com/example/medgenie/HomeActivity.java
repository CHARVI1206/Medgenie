package com.example.medgenie;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;
    private List<Post> postList;
    private FirebaseFirestore db;
    private Translator translator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase
        db = FirebaseFirestore.getInstance();

        // Setup UI elements
        recyclerView = findViewById(R.id.recyclerViewFeed);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postList = new ArrayList<>();
        postAdapter = new PostAdapter(postList);
        recyclerView.setAdapter(postAdapter);

        // Bottom Navigation setup
        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(this::onNavigationItemSelected);

        // Setup translator and then fetch posts
        setupTranslator();
    }

    // Set up Google ML Kit Translator with model download
    private void setupTranslator() {
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(TranslateLanguage.ENGLISH)
                .setTargetLanguage(TranslateLanguage.HINDI)
                .build();
        translator = Translation.getClient(options);

        translator.downloadModelIfNeeded()
                .addOnSuccessListener(unused -> fetchPosts())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Translation model download failed.", Toast.LENGTH_SHORT).show();
                    fetchPosts(); // Still fetch posts without translation
                });
    }

    // Fetch posts from Firestore and translate captions
    private void fetchPosts() {
        db.collection("posts").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        postList.clear();
                        List<Post> newPosts = new ArrayList<>();

                        for (QueryDocumentSnapshot doc : task.getResult()) {
                            String caption = doc.getString("caption");
                            String imageUrl = doc.getString("imageUrl");

                            translateCaption(caption, translatedCaption -> {
                                newPosts.add(new Post(imageUrl, translatedCaption));

                                if (newPosts.size() == task.getResult().size()) {
                                    postList.addAll(newPosts);
                                    postAdapter.notifyDataSetChanged();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(this, "Failed to load posts", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Translate caption using Google Translate
    private void translateCaption(String caption, TranslationCallback callback) {
        translator.translate(caption)
                .addOnSuccessListener(callback::onTranslated)
                .addOnFailureListener(e -> callback.onTranslated(caption)); // fallback to original
    }

    // Handle Bottom Navigation actions
    private boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            // Already in home
            return true;
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileSettingsActivity.class));
            return true;
        } else if (id == R.id.nav_notifications) {
            startActivity(new Intent(this, NotificationsActivity.class));
            return true;
        }

        return false;
    }

    // Open AI Chat Activity
    public void openAiChat(View view) {
        startActivity(new Intent(this, AiChatActivity.class));
    }

    // Callback interface for caption translation
    interface TranslationCallback {
        void onTranslated(String translatedText);
    }
}
