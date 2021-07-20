package com.example.wifisnooze;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.joda.time.DateTime;
import org.joda.time.Duration;

public class CountdownActivity extends AppCompatActivity {

    public static final String ARG_UNSNOOZE_TIME = "Unsnooze Time";
    public static final String ARG_PREV_WIFI_STATE = "Previous WiFi State";

    private TextView countdownView;
    private DateTime unsnoozeTime;
    private WifiManager wifi;
    private CountDownTimer timer;
    private int prevWifiState;

    @SuppressLint("DefaultLocale")
    private void updateCountdownView(long milliseconds) {
        Duration duration = new Duration(milliseconds);
        long totalSeconds = duration.getStandardSeconds();
        long seconds = totalSeconds % 60;
        long totalMinutes = totalSeconds / 60 ;
        long minutes = totalMinutes % 60;
        long hours = totalMinutes / 60;
        countdownView.setText(String.format("%dh %dm %ds", hours, minutes, seconds));
    }

    private void onAddOneMinClick(View view) {
        unsnoozeTime = unsnoozeTime.plusMinutes(1);
        long untilUnsnooze = new Duration(DateTime.now(), unsnoozeTime).getMillis();
        timer.cancel();
        timer = new SnoozeTimer(untilUnsnooze);
        timer.start();
        updateCountdownView(untilUnsnooze);
    }

    private void onCancelClick(View view) {
        timer.cancel();
        if (prevWifiState == WifiManager.WIFI_STATE_ENABLED
                || prevWifiState == WifiManager.WIFI_STATE_ENABLING) {
            wifi.setWifiEnabled(true);
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        countdownView = findViewById(R.id.countdownView);

        Button addOneMinButton = findViewById(R.id.addOneMinButton);
        addOneMinButton.setOnClickListener(this::onAddOneMinClick);

        Button cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this::onCancelClick);

        unsnoozeTime = (DateTime)getIntent().getSerializableExtra(ARG_UNSNOOZE_TIME);
        Duration untilUnsnooze = new Duration(DateTime.now(), unsnoozeTime);
        updateCountdownView(untilUnsnooze.getMillis());

        prevWifiState = getIntent().getIntExtra(ARG_PREV_WIFI_STATE, -1);

        wifi = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        timer = new SnoozeTimer(untilUnsnooze.getMillis());
        timer.start();
    }

    private class SnoozeTimer extends CountDownTimer {
        public SnoozeTimer(long millisInFuture) {
            super(millisInFuture, 1000);
        }
        @Override
        public void onTick(long millisUntilFinished) {
            updateCountdownView(millisUntilFinished);
        }
        @Override
        public void onFinish() {
            wifi.setWifiEnabled(true);
            Toast.makeText(getApplicationContext(),
                    "WiFi snooze expired", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}