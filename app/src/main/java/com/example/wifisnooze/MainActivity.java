package com.example.wifisnooze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE};
    private static final int REQUEST_PERMISSIONS = 1;

    private Button startSnoozeButton;
    private TableLayout numberPad;
    private WifiManager wifi;
    private StringBuilder timerText;

    public void onStartSnoozeClick(View view) {
        boolean enabled = wifi.isWifiEnabled();
        Log.d("WIFI", String.format("wifi enabled: %b", enabled));
        boolean result = wifi.setWifiEnabled(!enabled);
        Log.d("WIFI", String.format("wifi result: %b", result));
    }

    public void onNumberClick(View view) {
        Button btn = (Button)view;
        if (timerText.length() < 6) {
            timerText.append(btn.getText());
        }
    }

    public void onClearClick(View view) {
        timerText.setLength(0);
    }

    public void onBackspaceClick(View view) {
        timerText.setLength(Math.max(timerText.length() - 1, 0));
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

    @SuppressLint("SetTextI18n")
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
        startSnoozeButton = findViewById(R.id.startSnoozeButton);
        numberPad = findViewById(R.id.numberPad);
        wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        timerText = new StringBuilder();

        // UI Glue
        startSnoozeButton.setOnClickListener(this::onStartSnoozeClick);
        TableRow.LayoutParams lp = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
        for (int i = 0; i < 3; ++i) {
            TableRow row = new TableRow(this);
            row.setLayoutParams(lp);
            for (int j = 1; j <= 3; ++j) {
                Button btn = new Button(this);
                btn.setText(Integer.toString(i * 3 + j));
                btn.setOnClickListener(this::onNumberClick);
                row.addView(btn);
            }
            numberPad.addView(row);
        }
        TableRow finalRow = new TableRow(this);
        finalRow.setLayoutParams(lp);
        // Clear button
        Button clearBtn = new Button(this);
        clearBtn.setText("C");
        clearBtn.setOnClickListener(this::onClearClick);
        finalRow.addView(clearBtn);
        // Zero button
        Button zeroBtn = new Button(this);
        zeroBtn.setText("0");
        zeroBtn.setOnClickListener(this::onNumberClick);
        finalRow.addView(zeroBtn);
        // Backspace button
        Button backspaceBtn = new Button(this);
        backspaceBtn.setText("<-");
        backspaceBtn.setOnClickListener(this::onBackspaceClick);
        finalRow.addView(backspaceBtn);
        numberPad.addView(finalRow);
    }
}