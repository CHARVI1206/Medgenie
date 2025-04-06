package com.example.medgenie;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;

public class NotificationsActivity extends AppCompatActivity {

    private ListView listViewNotifications;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> notificationsList;

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);

        listViewNotifications = findViewById(R.id.listViewNotifications); // Make sure this ID exists in XML

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        notificationsList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, notificationsList);
        listViewNotifications.setAdapter(adapter);

        loadNotifications();
    }

    private void loadNotifications() {
        String uid = auth.getCurrentUser().getUid();

        db.collection("users")
                .document(uid)
                .collection("notifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    notificationsList.clear();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String title = doc.getString("title");
                        String message = doc.getString("message");
                        notificationsList.add("📌 " + title + "\n" + message);
                    }
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to load notifications", Toast.LENGTH_SHORT).show());
    }
}
