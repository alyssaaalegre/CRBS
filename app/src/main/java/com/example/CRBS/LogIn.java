package com.example.CRBS;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class LogIn extends AppCompatActivity {

    private EditText usernameEditText;
    private EditText passwordEditText;
    private Button loginButton;
    private CheckBox rememberMeCheckbox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.log_in); // Ensure your layout file is named correctly

        // Initialize UI elements
        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginButton = findViewById(R.id.login_button);
        rememberMeCheckbox = findViewById(R.id.remember_me);

        // Set padding to avoid system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Load saved credentials if available
        loadSavedCredentials();

        // Set up the login button click listener
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString();
            String password = passwordEditText.getText().toString();

            if (validateFields(username, password)) {
                performLogin(username, password);
            }
        });
    }

    private boolean validateFields(String username, String password) {
        boolean isValid = true;

        // Validate username
        if (username.isEmpty()) {
            usernameEditText.setError("Username is required");
            isValid = false;
        }

        // Validate password
        if (password.isEmpty()) {
            passwordEditText.setError("Password is required");
            isValid = false;
        }

        return isValid;
    }

    private void loadSavedCredentials() {
        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String savedUsername = sharedPreferences.getString("username", "");
        String savedPassword = sharedPreferences.getString("password", "");
        boolean isRemembered = sharedPreferences.getBoolean("remember_me", false);

        if (isRemembered) {
            usernameEditText.setText(savedUsername);
            passwordEditText.setText(savedPassword);
            rememberMeCheckbox.setChecked(true);
        }
    }

    private void performLogin(String username, String password) {
        String url = "https://swuresource.scarlet2.io/CRBS/log_in.php";

        // Create the JSON object to send
        JSONObject jsonInput = new JSONObject();
        try {
            jsonInput.put("username", username);
            jsonInput.put("password", password);
        } catch (JSONException e) {
            Log.e("LogIn", "JSON Exception: " + e.getMessage());
            return; // Exit if there's an issue creating the JSON
        }

        // Log the JSON string
        String jsonString = jsonInput.toString();
        Log.d("LogIn", "Sending JSON: " + jsonString);

        // Create a JsonObjectRequest
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                jsonInput,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("LogIn", "Response: " + response.toString()); // Log full response
                        try {
                            if (response.getBoolean("success")) {
                                // Login successful, retrieve account_id
                                int accountId = response.getInt("account_id");
                                Log.d("LogIn", "Retrieved Account ID: " + accountId); // Log accountId

                                // Log the username and password for debugging (be cautious with logging sensitive information)
                                Log.d("LogIn", "Username: " + username + ", Password: " + password);

                                // Save account_id to SharedPreferences
                                SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("account_id", accountId);

                                // Save username, password, and remember me state
                                if (rememberMeCheckbox.isChecked()) {
                                    editor.putString("username", username);
                                    editor.putString("password", password);
                                    editor.putBoolean("remember_me", true);
                                } else {
                                    editor.remove("username");
                                    editor.remove("password");
                                    editor.putBoolean("remember_me", false);
                                }
                                editor.apply();

                                // Start MainActivity and pass accountId
                                Intent intent = new Intent(LogIn.this, MainActivity.class);
                                intent.putExtra("accountId", accountId); // Pass the accountId with the correct key
                                startActivity(intent);
                                finish(); // Close LogIn activity
                            } else {
                                // Handle login failure
                                String errorMessage = response.getString("message");
                                Log.d("LogIn", "Login failed: " + errorMessage); // Log error message
                                Toast.makeText(LogIn.this, errorMessage, Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            Log.e("LogIn", "JSON Exception: " + e.getMessage());
                            Toast.makeText(LogIn.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error response
                        Toast.makeText(LogIn.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.e("LogIn", "Volley Error: " + error.toString());
                    }
                }
        );

        // Add the request to the RequestQueue
        Volley.newRequestQueue(this).add(jsonObjectRequest);
    }
}
