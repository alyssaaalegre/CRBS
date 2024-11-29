package com.example.CRBS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class GetStarted extends AppCompatActivity {

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_FIRST_RUN = "firstRun";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.get_started_page);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup the button to mark the first run as complete
        Button startButton = findViewById(R.id.startButton); // Replace with your actual button ID
        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Mark the first run as complete
                SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean(KEY_FIRST_RUN, false); // Set to false so GetStarted won't show again
                editor.apply();

                // Launch the main activity
                Intent intent = new Intent(GetStarted.this, LogIn.class);
                startActivity(intent);
                finish(); // Close the Get Started page
            }
        });
    }
}
