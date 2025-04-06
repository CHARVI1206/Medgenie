package com.example.medgenie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class EmergencySOSActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button btnSOS, btnAddContact;
    private ListView listViewContacts;
    private List<String> emergencyContacts = new ArrayList<>();
    private FusedLocationProviderClient locationProvider;
    private MapView mapView;
    private GoogleMap googleMap;
    private String lastKnownLocation = "Unknown Location";

    private FirebaseFirestore db;
    private static final int PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sos_emergency);

        btnSOS = findViewById(R.id.btnSOS);
        btnAddContact = findViewById(R.id.btnAddContact);
        listViewContacts = findViewById(R.id.listViewContacts);
        mapView = findViewById(R.id.mapView);

        // Initialize Firebase & Location Services
        db = FirebaseFirestore.getInstance();
        locationProvider = LocationServices.getFusedLocationProviderClient(this);

        // Initialize Google Map
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        loadEmergencyContacts();

        // SOS Button Click Listener
        btnSOS.setOnClickListener(view -> sendSOSAlert());

        // Add Emergency Contact
        btnAddContact.setOnClickListener(view -> startActivity(new Intent(this, AddContactActivity.class)));

        requestPermissions();
    }

    // Load Emergency Contacts from Firebase
    private void loadEmergencyContacts() {
        CollectionReference contactsRef = db.collection("emergency_contacts");
        contactsRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                emergencyContacts.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    emergencyContacts.add(document.getString("phone"));
                }
            }
        });
    }

    // Send SOS Alert (Call & SMS)
    private void sendSOSAlert() {
        if (emergencyContacts.isEmpty()) {
            Toast.makeText(this, "No Emergency Contacts!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_SHORT).show();
            return;
        }

        locationProvider.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                String mapsUrl = "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;
                String message = "🚨 EMERGENCY ALERT! 🚨\nI need help! My location: " + mapsUrl;

                SmsManager smsManager = SmsManager.getDefault();
                for (String phone : emergencyContacts) {
                    smsManager.sendTextMessage(phone, null, message, null, null);
                }

                Toast.makeText(this, "SOS Alert Sent!", Toast.LENGTH_SHORT).show();

                // Make Emergency Call
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + emergencyContacts.get(0)));
                    startActivity(callIntent);
                }

            } else {
                Toast.makeText(this, "Could not get location.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    // Get Live Location
    private void getLiveLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationProvider.getLastLocation().addOnSuccessListener(location -> {
                if (location != null) {
                    lastKnownLocation = "Lat: " + location.getLatitude() + ", Lng: " + location.getLongitude();
                    updateMap(location);
                }
            });
        }
    }

    // Update Google Map with Current Location
    private void updateMap(Location location) {
        if (googleMap != null) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(userLocation).title("My Location"));
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        getLiveLocation();
    }

    // Request Permissions
    private void requestPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.SEND_SMS,
                Manifest.permission.CALL_PHONE
        }, PERMISSION_REQUEST_CODE);
    }

}
