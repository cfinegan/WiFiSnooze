package com.example.wifisnooze;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;

public class MainActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE};
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int MAX_TIMER_DIGITS = 6;

    private static StringBuilder getPaddedText(StringBuilder text) {
        StringBuilder paddedText = new StringBuilder(MAX_TIMER_DIGITS);
        for (int i = 0; i < (MAX_TIMER_DIGITS - text.length()); ++i) {
            paddedText.append('0');
        }
        paddedText.append(text);
        return paddedText;
    }

    private TextView timerView;
    private WifiManager wifi;
    private StringBuilder timerText;

    private void refreshTimerView() {
        StringBuilder staging = getPaddedText(timerText);
        timerView.setText(String.format("%sh %sm %ss",
                staging.substring(0, 2), staging.substring(2, 4), staging.substring(4, 6)));
    }

    public void onStartSnoozeClick(View view) {
        StringBuilder staging = getPaddedText(timerText);

        int hours = Integer.parseInt(staging.substring(0, 2));
        int minutes = Integer.parseInt(staging.substring(2, 4));
        int seconds = Integer.parseInt(staging.substring(4, 6));

        Intent intent = new Intent(this, CountdownActivity.class);
        intent.putExtra(CountdownActivity.ARG_UNSNOOZE_TIME,
                DateTime.now().plusHours(hours).plusMinutes(minutes).plusSeconds(seconds));

        int prevState = wifi.getWifiState();
        if (wifi.setWifiEnabled(false)) {
            try {
                startActivity(intent);
            } catch (Exception e) {
                // Provide a "strong exception guarantee" for disabling wifi -  the wifi only stays
                // disabled if countdown activity is successfully launched.
                if (prevState == WifiManager.WIFI_STATE_ENABLED
                        || prevState == WifiManager.WIFI_STATE_ENABLING) {
                    wifi.setWifiEnabled(true);
                }
                throw e;
            }
        } else {
            Toast.makeText(getApplicationContext(),
                    "failed to start countdown activity", Toast.LENGTH_SHORT).show();
        }
    }

    public void onNumberClick(View view) {
        Button btn = (Button)view;
        if (timerText.length() < MAX_TIMER_DIGITS) {
            timerText.append(btn.getText());
        }
        refreshTimerView();
    }

    public void onClearClick(View view) {
        timerText.setLength(0);
        refreshTimerView();
    }

    public void onBackspaceClick(View view) {
        timerText.setLength(Math.max(timerText.length() - 1, 0));
        refreshTimerView();
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
        timerView = findViewById(R.id.timerView);
        wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        timerText = new StringBuilder(MAX_TIMER_DIGITS);

        // Setup snooze button.
        Button startSnoozeButton = findViewById(R.id.startSnoozeButton);
        startSnoozeButton.setOnClickListener(this::onStartSnoozeClick);

        // Setup number pad.
        TableLayout numberPad = findViewById(R.id.numberPad);
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

        refreshTimerView();
    }
}