package com.example.CRBS;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // For logging
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import androidx.appcompat.app.AppCompatActivity;

public class RequestRepair extends AppCompatActivity {

    private int accountId; // Declare accountId

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.repairpage);

        // Retrieve the accountId from the intent
        accountId = getIntent().getIntExtra("accountId", -1);
        Log.d("RequestRepair", "Received Account ID: " + accountId);

        // Initialize the back button and set the click listener
        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass accountId to RepairRequestPopup
                Intent intent = new Intent(RequestRepair.this, RepairRequestPopup.class);
                intent.putExtra("accountId", accountId); // Pass the accountId
                startActivity(intent);
            }
        });

        // Initialize the reserve button and set the click listener
        Button reserveButton = findViewById(R.id.reserveButton);
        reserveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Pass accountId to RepairRequestPopup
                Intent intent = new Intent(RequestRepair.this, RepairRequestPopup.class);
                intent.putExtra("accountId", accountId); // Pass the accountId
                startActivity(intent);
            }
        });
    }
}
