package com.example.wifisnooze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE};
    private static final int REQUEST_PERMISSIONS = 1;

    private WifiManager wifi;
    private StringBuilder timerText;

    public void onStartSnoozeClick(View view) {
        boolean enabled = wifi.isWifiEnabled();
        Log.d("WIFI", String.format("wifi enabled: %b", enabled));
        boolean result = wifi.setWifiEnabled(!enabled);
        Log.d("WIFI", String.format("wifi result: %b", result));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                    @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSIONS) {
            boolean requestGranted = (grantResults.length != 0);
            for (int result : grantResults) {
                if (result == PackageManager.PERMISSION_DENIED) {
                    requestGranted = false;
                    break;
                }
            }
            if (!requestGranted) {
                new AlertDialog.Builder(this)
                        .setTitle(R.string.app_name)
                        .setMessage("Permissions are required to continue")
                        .setPositiveButton("Grant Permissions", (dialog, which) ->
                                ActivityCompat.requestPermissions(
                                        this, PERMISSIONS, REQUEST_PERMISSIONS))
                        .setNegativeButton("Quit", (dialog, which) -> finish())
                        .create().show();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Context context = getApplicationContext();

        // Make sure we aren't missing the necessary platform feature.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI)) {
            Toast.makeText(context, "WiFi feature not found", Toast.LENGTH_LONG).show();
            finish();
        }

        // Make sure we have all the permissions we need.
        boolean needPerms = false;
        for (String p : PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(context, p)
                    != PackageManager.PERMISSION_GRANTED) {
                needPerms = true;
                break;
            }
        }
        if (needPerms) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSIONS);
        }

        // Initialize member variables.
        wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        timerText = new StringBuilder();
    }
}