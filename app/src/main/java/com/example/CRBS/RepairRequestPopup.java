package com.example.CRBS;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log; // For logging
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RepairRequestPopup extends AppCompatActivity {

    private int accountId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.requestrepairpopup);

        accountId = getIntent().getIntExtra("accountId", -1);
        Log.d("RepairRequestPopup", "Received Account ID: " + accountId);

        // Set up spinners
        setupSpinners();

        // Set up input fields
        EditText editTextName = findViewById(R.id.editTextName);
        EditText editTextEmail = findViewById(R.id.editTextEmail);
        EditText editTextIdNumber = findViewById(R.id.editTextIdNumber);
        EditText editTextDepartment = findViewById(R.id.editTextDepartment);
        EditText editTextDescription = findViewById(R.id.editTextDescription);

        // Handle ID formatting
        handleIdFormatting(editTextIdNumber);

        // Handle close button click
        Button closeButton = findViewById(R.id.buttonClose);
        closeButton.setOnClickListener(v -> navigateToMainActivity());

        // Handle submit button click
        Button submitButton = findViewById(R.id.buttonSubmit);
        submitButton.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String idNumber = editTextIdNumber.getText().toString().trim().replace("-", "");
            String department = editTextDepartment.getText().toString().trim();
            String requestType = ((Spinner) findViewById(R.id.spinnerRequestType)).getSelectedItem().toString();
            String requestDescription = editTextDescription.getText().toString().trim();

            if (validateInputs(name, email, idNumber, department, requestDescription)) {
                submitRepairRequest(name, email, idNumber, department, requestType, requestDescription);
            }
        });
    }

    private void setupSpinners() {
        Spinner requestTypeSpinner = findViewById(R.id.spinnerRequestType);
        ArrayAdapter<CharSequence> requestTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.request_type_array, android.R.layout.simple_spinner_item);
        requestTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        requestTypeSpinner.setAdapter(requestTypeAdapter);

        // Initialize the facility spinner with placeholder
        Spinner facilitySpinner = findViewById(R.id.spinnerFacility);
        ArrayAdapter<String> facilityAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, Collections.singletonList("Coming Soon"));
        facilityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        facilitySpinner.setAdapter(facilityAdapter);
    }

    private void handleIdFormatting(EditText editTextIdNumber) {
        editTextIdNumber.addTextChangedListener(new TextWatcher() {
            private boolean isFormatting;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (isFormatting) return;

                isFormatting = true;
                String input = s.toString().replaceAll("[^\\d]", "");
                StringBuilder formatted = new StringBuilder();

                // Format the ID number as 00-0000-000000
                for (int i = 0; i < input.length(); i++) {
                    if (i == 2 || i == 6) {
                        formatted.append('-');
                    }
                    formatted.append(input.charAt(i));
                }

                // Limit to 14 characters (including dashes)
                if (formatted.length() > 14) {
                    formatted.setLength(14);
                }

                editTextIdNumber.setText(formatted.toString());
                editTextIdNumber.setSelection(formatted.length());
                isFormatting = false;
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private boolean validateInputs(String name, String email, String idNumber, String department, String requestDescription) {
        if (name.isEmpty()) {
            showToast("Name is required");
            return false;
        }

        if (email.isEmpty() || !isValidEmail(email)) {
            showToast("Valid email is required");
            return false;
        }

        if (idNumber.isEmpty()) {
            showToast("ID number is required");
            return false;
        }

        if (department.isEmpty()) {
            showToast("Department is required");
            return false;
        }

        if (requestDescription.isEmpty()) {
            showToast("Description is required");
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void submitRepairRequest(String name, String email, String idNumber, String department, String requestType, String requestDescription) {
        RequestQueue queue = Volley.newRequestQueue(this);
        String url = "https://swuresource.scarlet2.io/app/submit_repair_request.php";

        StringRequest stringRequest = new StringRequest(Request.Method.POST, url,
                response -> {
                    Log.d("RepairRequestPopup", "Response: " + response);
                    showToast("Repair request submitted successfully!");
                    navigateToMainActivity();
                },
                error -> {
                    Log.e("RepairRequestPopup", "Error: " + error.toString());
                    showToast("Error submitting request.");
                }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("accountId", String.valueOf(accountId));
                params.put("name", name);
                params.put("email", email);
                params.put("id_Number", idNumber);
                params.put("department", department);
                params.put("request_type", requestType);
                params.put("request_description", requestDescription);
                params.put("facility", "Coming Soon");  // Include placeholder facility
                return params;
            }
        };

        queue.add(stringRequest);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("accountId", accountId); // Pass the account ID back to MainActivity
        startActivity(intent);
        finish();
    }

}
