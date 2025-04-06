package com.example.medgenie;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class AddContactActivity extends AppCompatActivity {

    private EditText etContactName, etPhoneNumber;
    private Button btnSave;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_contact);

        etContactName = findViewById(R.id.etContactName);
        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSave = findViewById(R.id.btnSave);

        db = FirebaseFirestore.getInstance();

        btnSave.setOnClickListener(v -> saveContact());
    }

    private void saveContact() {
        String name = etContactName.getText().toString().trim();
        String phone = etPhoneNumber.getText().toString().trim();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        if (name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> contact = new HashMap<>();
        contact.put("name", name);
        contact.put("phone", phone);

        db.collection("emergency_contacts")
                .document(uid)
                .collection("contacts")
                .add(contact)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Contact Saved!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to save contact: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}
