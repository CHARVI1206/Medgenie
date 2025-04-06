package com.example.medgenie;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    private Switch switchNotification;
    private SharedPreferences sharedPreferences;

    private static final String PREF_NAME = "Settings";
    private static final String NOTIFICATION_KEY = "notifications_enabled";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        switchNotification = findViewById(R.id.switchNotification);
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        // Load previous settings
        boolean notificationsEnabled = sharedPreferences.getBoolean(NOTIFICATION_KEY, true);
        switchNotification.setChecked(notificationsEnabled);

        // Save changes on toggle
        switchNotification.setOnCheckedChangeListener((CompoundButton buttonView, boolean isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(NOTIFICATION_KEY, isChecked);
            editor.apply();

            String msg = isChecked ? "Notifications Enabled" : "Notifications Disabled";
            Toast.makeText(SettingsActivity.this, msg, Toast.LENGTH_SHORT).show();
        });

        findViewById(R.id.btnBack).setOnClickListener(v ->
                startActivity(new Intent(SettingsActivity.this, ProfileSettingsActivity.class))
        );
    }
}
