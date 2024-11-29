package com.example.CRBS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_RUN = "firstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        VideoView videoView = findViewById(R.id.videoView);
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.splash_video);
        videoView.setVideoURI(videoUri);

        videoView.setOnCompletionListener(mp -> {
            // Check shared preferences to determine navigation
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            boolean isFirstRun = prefs.getBoolean(KEY_FIRST_RUN, true);

            Intent intent;
            if (isFirstRun) {
                // If first run, navigate to GetStarted
                intent = new Intent(SplashActivity.this, GetStarted.class);
            } else {
                // If not first run, navigate directly to LogIn
                intent = new Intent(SplashActivity.this, LogIn.class);
            }
            startActivity(intent);
            finish(); // Close the splash activity
        });

        videoView.start(); // Start the video
    }
}
