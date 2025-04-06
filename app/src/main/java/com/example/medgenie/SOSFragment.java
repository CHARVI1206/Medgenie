package com.example.medgenie;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.view.LayoutInflater;
import android.view.ViewGroup;


public class SOSFragment extends Fragment {
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private Button btnSOS, btnHospital, btnAmbulance;
    private TextView tvSOSStatus;
    private FusedLocationProviderClient locationClient;
    private DatabaseReference sosDatabase;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        locationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        sosDatabase = FirebaseDatabase.getInstance().getReference("SOS Alerts");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sos, container, false);
        btnSOS = view.findViewById(R.id.btn_sos);
        btnHospital = view.findViewById(R.id.btn_nearest_hospital);
        btnAmbulance = view.findViewById(R.id.btn_book_ambulance);
        tvSOSStatus = view.findViewById(R.id.tv_sos_status);

        btnSOS.setOnClickListener(v -> sendSOSAlert());
        btnHospital.setOnClickListener(v -> findNearestHospital());
        btnAmbulance.setOnClickListener(v -> bookAmbulance());

        return view;
    }

    private void sendSOSAlert() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
            return;
        }

        locationClient.getLastLocation().addOnSuccessListener(location -> {
            if (location != null) {
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                // Store SOS alert in Firebase
                String alertId = sosDatabase.push().getKey();
                sosDatabase.child(alertId).setValue(new SOSAlert(latitude, longitude));

                // Send SMS alert to emergency contacts (Twilio or Firebase Messaging can be integrated)
                sendSMSAlert(latitude, longitude);

                tvSOSStatus.setText("SOS Sent Successfully! 📍 Location: " + latitude + ", " + longitude);
            } else {
                tvSOSStatus.setText("Failed to get location. Try again!");
            }
        });
    }

    private void sendSMSAlert(double latitude, double longitude) {
        String message = "🚨 EMERGENCY ALERT! 🚨\nUser needs help at location: " +
                "https://www.google.com/maps/search/?api=1&query=" + latitude + "," + longitude;

        // Auto-dial emergency contact (Replace with actual number)
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:112"));
        startActivity(intent);
    }

    private void findNearestHospital() {
        String googleMapsSearch = "https://www.google.com/maps/search/hospitals+near+me/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsSearch));
        startActivity(intent);
    }

    private void bookAmbulance() {
        String googleMapsSearch = "https://www.google.com/maps/search/ambulance+near+me/";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googleMapsSearch));
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            sendSOSAlert();
        }
    }

    public static class SOSAlert {
        public double latitude, longitude;

        public SOSAlert(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
}
