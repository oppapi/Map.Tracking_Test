package com.example.maptest3;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.*;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_FINE_LOCATION = 99;

    TextView tv_lat, tv_lon, tv_altitude, tv_accuracy, tv_speed, tv_sensor, tv_updates, tv_address;
    SwitchCompat sw_locationsupdates, sw_gps;

    LocationRequest locationRequest;
    FusedLocationProviderClient fusedLocationClient;
    LocationCallback locationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tv_lat = findViewById(R.id.tv_lat);
        tv_lon = findViewById(R.id.tv_lon);
        tv_altitude = findViewById(R.id.tv_altitude);
        tv_accuracy = findViewById(R.id.tv_accuracy);
        tv_speed = findViewById(R.id.tv_speed);
        tv_sensor = findViewById(R.id.tv_sensor);
        tv_updates = findViewById(R.id.tv_updates);
        tv_address = findViewById(R.id.tv_address);

        sw_locationsupdates = findViewById(R.id.sw_locationsupdates);
        sw_gps = findViewById(R.id.sw_gps);

        // Initialize location request
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(30000);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    tv_updates.setText("Location not available");
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        updateUIValues(location);
                        tv_updates.setText("Location updated");
                    }
                }
            }
        };

        // GPS switch
        sw_gps.setOnClickListener(v -> {
            if (sw_gps.isChecked()) {
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                tv_sensor.setText("Using GPS");
            } else {
                locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
                tv_sensor.setText("Using Wifi/Cell");
            }
        });

        // Start/Stop switch
        sw_locationsupdates.setOnClickListener(view -> {
            if (sw_locationsupdates.isChecked()) {
                startLocationUpdates();
            } else {
                stopLocationUpdates();
            }
        });
    }

    private void stopLocationUpdates() {
        tv_updates.setText("Location updates stopped");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        tv_updates.setText("Location updates started");

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    getMainLooper()
            );

        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        PERMISSION_FINE_LOCATION
                );
            }
        }
    }

    private void updateUIValues(Location location) {
        tv_lat.setText(String.valueOf(location.getLatitude()));
        tv_lon.setText(String.valueOf(location.getLongitude()));
        tv_accuracy.setText(String.valueOf(location.getAccuracy()));
        tv_altitude.setText(location.hasAltitude() ? String.valueOf(location.getAltitude()) : "N/A");
        tv_speed.setText(location.hasSpeed() ? String.valueOf(location.getSpeed()) : "N/A");

        Geocoder geocoder = new Geocoder(MainActivity.this);

        try {
            List<Address> addresses = geocoder.getFromLocation(
                    location.getLatitude(),
                    location.getLongitude(),
                    1
            );

            if (addresses != null && addresses.size() > 0) {
                tv_address.setText(addresses.get(0).getAddressLine(0));
            } else {
                tv_address.setText("Address not found");
            }

        } catch (Exception e) {
            tv_address.setText("Address not available");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_FINE_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (sw_locationsupdates.isChecked()) {
            startLocationUpdates();
        }
    }
}