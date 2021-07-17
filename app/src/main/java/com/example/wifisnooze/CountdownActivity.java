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

    private TextView countdownView;
    private Button cancelButton;
    private DateTime unsnoozeTime;
    private WifiManager wifi;
    private CountDownTimer timer;


    @SuppressLint("DefaultLocale")
    private void updateCountdownView(long milliseconds) {
        Duration duration = new Duration(milliseconds);
        long hours = duration.getStandardHours();
        long minutes = duration.getStandardMinutes();
        long seconds = duration.getStandardSeconds();
        countdownView.setText(String.format("%dh %dm %ds", hours, minutes, seconds));
    }

    private void onCancelClick(View view) {
        timer.cancel();
        // TODO: should we re-enabled WiFi here?
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_countdown);

        countdownView = findViewById(R.id.countdownView);
        cancelButton = findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(this::onCancelClick);

        unsnoozeTime = (DateTime)getIntent().getSerializableExtra(ARG_UNSNOOZE_TIME);
        Duration untilUnsnooze = new Duration(DateTime.now(), unsnoozeTime);
        updateCountdownView(untilUnsnooze.getMillis());

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