package com.example.CRBS;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Patterns;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReservationPage extends AppCompatActivity implements ActivityAdapter.OnActivityClickListener {

    private EditText bookingDateEditText, startTimeEditText, endTimeEditText;
    private EditText nameEditText, idNumberEditText, orgEmailEditText, deanEmailEditText, purposeEditText;
    private TextView capacityTextView, resourceTypeTextView, departmentTextView;
    private Spinner capacitySpinner, resourceTypeSpinner, departmentSpinner;
    private RecyclerView activitiesRecyclerView;
    private ActivityAdapter activityAdapter;
    private ActivityItem selectedActivity;
    private List<ActivityItem> activities, filteredActivities;
    private List<String> reservedBuildings = new ArrayList<>();
    private TextView noActivitiesTextView;
    private int accountId;
    private static final String TAG = "ReservationPage";

    private AvailabilityChecker availabilityChecker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.reservationpage);

        Log.d(TAG, "onCreate called");

        // Initialize EditTexts
        bookingDateEditText = findViewById(R.id.bookingDateEditText);
        startTimeEditText = findViewById(R.id.startTimeEditText);
        endTimeEditText = findViewById(R.id.endTimeEditText);
        nameEditText = findViewById(R.id.nameEditText);
        idNumberEditText = findViewById(R.id.idnumberEditText);
        orgEmailEditText = findViewById(R.id.organizational_emailEditText);
        deanEmailEditText = findViewById(R.id.dean_emailEditText);
        purposeEditText = findViewById(R.id.purposeEditText);
        ImageButton backbutton = findViewById(R.id.backbutton);

        // Add TextWatcher for ID Number
        idNumberEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String formatted = formatIdNumber(s.toString());
                if (!formatted.equals(s.toString())) {
                    idNumberEditText.setText(formatted);
                    idNumberEditText.setSelection(formatted.length());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Initialize TextViews and Spinners
        capacityTextView = findViewById(R.id.capacityTextView);
        capacitySpinner = findViewById(R.id.capacitySpinner);
        resourceTypeTextView = findViewById(R.id.resourcetypeTextView);
        resourceTypeSpinner = findViewById(R.id.resourcetypeSpinner);
        departmentTextView = findViewById(R.id.departmentsTextView);
        departmentSpinner = findViewById(R.id.departmentsSpinner);
        noActivitiesTextView = findViewById(R.id.noActivitiesTextView);

        // Initialize RecyclerView
        activitiesRecyclerView = findViewById(R.id.activitiesRecyclerView);
        activitiesRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        activities = new ArrayList<>();
        filteredActivities = new ArrayList<>();
        populateActivitiesList();

        activityAdapter = new ActivityAdapter(filteredActivities, this);
        activitiesRecyclerView.setAdapter(activityAdapter);

        backbutton.setOnClickListener(v -> finish());

        // Retrieve accountId from the intent and log it
        accountId = getIntent().getIntExtra("accountId", -1);
        Log.d(TAG, "Received Account ID: " + accountId); // Log the received accountId

        // Ensure accountId is not null
        if (accountId == -1) {
            Log.e(TAG, "Account ID is null");
            Toast.makeText(this, "Account ID is missing", Toast.LENGTH_SHORT).show();
            finish(); // Optionally close the activity if accountId is null
            return;
        }

        // Set up spinners
        setupCapacitySpinner();
        setupResourceTypeSpinner();
        setupDepartmentSpinner();

        // Click listeners for resource type, capacity, and department
        resourceTypeTextView.setOnClickListener(v -> {
            resourceTypeTextView.setVisibility(View.GONE);
            resourceTypeSpinner.setVisibility(View.VISIBLE);
            resourceTypeSpinner.performClick();
        });

        capacityTextView.setOnClickListener(v -> {
            capacityTextView.setVisibility(View.GONE);
            capacitySpinner.setVisibility(View.VISIBLE);
            capacitySpinner.performClick();
        });

        departmentTextView.setOnClickListener(v -> {
            departmentTextView.setVisibility(View.GONE);
            departmentSpinner.setVisibility(View.VISIBLE);
            departmentSpinner.performClick();
        });

        // Set listener to filter activities when spinner selection changes
        capacitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        resourceTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterActivities();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        availabilityChecker = new AvailabilityChecker(this);

        // Handle Date and Time pickers
        bookingDateEditText.setOnClickListener(v -> showDatePickerDialog());
        startTimeEditText.setOnClickListener(v -> showStartTimePickerDialog());
        endTimeEditText.setOnClickListener(v -> showEndTimePickerDialog());

        // Adding an OnChange listener to check availability when the date or times change
        bookingDateEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkAvailability();
            }
        });
        startTimeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkAvailability();
            }
        });
        endTimeEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                checkAvailability();
            }
        });

        findViewById(R.id.reserveButton).setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked"); // Log at the start of the onClick listener
            if (validateForm()) {
                Log.d(TAG, "Form validated"); // Log after validation
                if (selectedActivity != null) {
                    Log.d(TAG, "Selected Activity: " + selectedActivity.getTitle()); // Log selected activity title
                    // Check if the selected activity is in the list of reserved buildings
                    if (reservedBuildings.contains(selectedActivity.getTitle().trim().toLowerCase())) {
                        Toast.makeText(ReservationPage.this, "Selected activity is already reserved", Toast.LENGTH_SHORT).show();
                        return; // Exit to prevent submission
                    }
                    // Existing logic to check availability and submit form
                    checkAvailability(); // Check availability again before submitting

                    // Submit the reservation form to the database
                    String selectedTitle = selectedActivity.getTitle();
                    String selectedDescription = selectedActivity.getDescription();
                    String selectedResourceType = selectedActivity.getResourceType();
                    submitFormToDatabase(selectedTitle, selectedDescription, selectedResourceType);

                    // Assuming the account ID is available in the activity
                    int accountId = getIntent().getIntExtra("accountId", -1);

                    // Once the form is successfully submitted, pass the accountId to MainActivity
                    Intent intent = new Intent(ReservationPage.this, MainActivity.class);
                    intent.putExtra("accountId", accountId);  // Pass the accountId to MainActivity
                    startActivity(intent); // Navigate to MainActivity
                    finish();  // Close the current activity
                } else {
                    Log.d(TAG, "No venue selected"); // Log if no activity is selected
                    Toast.makeText(ReservationPage.this, "Please select a venue", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Validate initial state of spinners
        validateInitialSpinners();
    }

    private void checkAvailability() {
        // Set up the date format for parsing
        SimpleDateFormat inputSdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        SimpleDateFormat outputSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat inputTimeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault()); // For 12-hour format
        SimpleDateFormat outputTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()); // For 24-hour format

        String dateStr = bookingDateEditText.getText().toString(); // Get the date from EditText
        String startTime = startTimeEditText.getText().toString(); // Keep 12-hour format here
        String endTime = endTimeEditText.getText().toString(); // Keep 12-hour format here

        String date = ""; // Initialize date as an empty string

        try {
            // Parse the input date string into a Date object
            Date dateObj = inputSdf.parse(dateStr);
            // Format the Date object into the desired format
            date = outputSdf.format(dateObj);

            // Convert the time from 12-hour to 24-hour format
            Date startDateTime = inputTimeFormat.parse(startTime);
            Date endDateTime = inputTimeFormat.parse(endTime);
            startTime = outputTimeFormat.format(startDateTime);
            endTime = outputTimeFormat.format(endDateTime);

        } catch (ParseException e) {
            Log.e(TAG, "Date or time parsing error: " + e.getMessage());
            return; // Exit the method if parsing fails
        }

        // Log the parameters before making the API call
        Log.d(TAG, "Checking availability for date: " + date + ", startTime: " + startTime + ", endTime: " + endTime);

        if (!date.isEmpty() && !startTime.isEmpty() && !endTime.isEmpty()) {
            availabilityChecker.checkAvailability(date, startTime, endTime, new AvailabilityChecker.AvailabilityCallback() {
                @Override
                public void onSuccess(JSONArray reservations) {
                    Log.d(TAG, "Reservations JSON: " + reservations.toString());
                    filterAvailableBuildings(reservations);
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(ReservationPage.this, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }



    private void filterAvailableBuildings(JSONArray reservations) {
        reservedBuildings.clear(); // Clear previous entries

        Log.d(TAG, "Filtering available buildings, total reservations: " + reservations.length());

        for (int i = 0; i < reservations.length(); i++) {
            try {
                String building = reservations.getString(i).trim();
                reservedBuildings.add(building.toLowerCase());
                Log.d(TAG, "Reserved building: " + building);
            } catch (JSONException e) {
                Log.e(TAG, "JSON error: " + e.getMessage());
            }
        }

        Log.d(TAG, "Final list of reserved buildings: " + reservedBuildings);
        activityAdapter.filterActivitiesByReservations(reservedBuildings);
    }


    private void validateInitialSpinners() {
        if (capacitySpinner.getSelectedItemPosition() == 0) {
        }
        if (resourceTypeSpinner.getSelectedItemPosition() == 0) {
        }
        if (departmentSpinner.getSelectedItemPosition() == 0) {
        }
    }

    private void populateActivitiesList() {
        activities.add(new ActivityItem("PH|Alumni Hall", "7th Floor | Capacity 100", "Event Spaces"));
        activities.add(new ActivityItem("PH|Ground Floor", "Ground Floor | Capacity 100", "Event Spaces"));
        activities.add(new ActivityItem("TH M313", "3rd Floor | Capacity 50", "Teaching Spaces"));
        activities.add(new ActivityItem("PH 212-216", "2nd Floor | Capacity 100", "Teaching Spaces"));
        activities.add(new ActivityItem("SHASH AVR1", "3rd Floor | Capacity 80", "Meeting Rooms"));
        activities.add(new ActivityItem("SHASH AVR2", "4th Floor | Capacity 80", "Meeting Rooms"));
        activities.add(new ActivityItem("TH CL1", "3rd Floor | Capacity 80", "Laboratories"));
        activities.add(new ActivityItem("TH CL2", "3rd Floor | Capacity 100", "Laboratories"));
        activities.add(new ActivityItem("SHASH MT Laboratory", "4th Floor | Capacity 60", "Laboratories"));
        filteredActivities.addAll(activities);
    }

    private void setupCapacitySpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.capacity_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        capacitySpinner.setAdapter(adapter);
        capacitySpinner.setSelection(0);
    }

    private void setupResourceTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.resourcetype_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        resourceTypeSpinner.setAdapter(adapter);
        resourceTypeSpinner.setSelection(0);
    }

    private void setupDepartmentSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.departments_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        departmentSpinner.setAdapter(adapter);
        departmentSpinner.setSelection(0);
    }

    private void filterActivities() {
        filteredActivities.clear();

        int capacityPosition = capacitySpinner.getSelectedItemPosition();
        String selectedResourceType = resourceTypeSpinner.getSelectedItem().toString();
        int minCapacity = getCapacityValue(capacityPosition);

        Log.d(TAG, "Min capacity set to: " + minCapacity);
        Log.d(TAG, "Selected resource type: " + selectedResourceType);

        for (ActivityItem activity : activities) {
            Log.d(TAG, "Checking activity: " + activity.getTitle() + " with capacity: " + activity.getCapacity() + " and resource type: " + activity.getResourceType());

            if (activity.getCapacity() >= minCapacity &&
                    (selectedResourceType.equals("All") || activity.getResourceType().equals(selectedResourceType)) &&
                    !reservedBuildings.contains(activity.getTitle().toLowerCase())) {
                filteredActivities.add(activity);
                Log.d(TAG, "Activity added: " + activity.getTitle());
            } else {
                Log.d(TAG, "Activity filtered out: " + activity.getTitle());
            }
        }

        activityAdapter.updateActivities(filteredActivities);
        activityAdapter.notifyDataSetChanged();
        noActivitiesTextView.setVisibility(filteredActivities.isEmpty() ? View.VISIBLE : View.GONE);
        Log.d(TAG, "Filtered activities count: " + filteredActivities.size());
    }

    private int getCapacityValue(int Position) {
        switch (Position) {
            case 0: return 10;
            case 1: return 20;
            case 2: return 30;
            case 3: return 50;
            case 4: return 80;
            case 5: return 100;
            case 6: return 250;
            default: return 0;
        }
    }

    private String formatIdNumber(String id) {
        // Remove existing formatting
        id = id.replace("-", "");
        StringBuilder formattedId = new StringBuilder();

        // Format according to the desired pattern
        for (int i = 0; i < id.length(); i++) {
            if (i == 2 || i == 6) {
                formattedId.append("-");
            }
            formattedId.append(id.charAt(i));
        }

        return formattedId.toString();
    }

    private boolean validateForm() {
        // Reset errors
        nameEditText.setError(null);
        idNumberEditText.setError(null);
        bookingDateEditText.setError(null);
        startTimeEditText.setError(null);
        endTimeEditText.setError(null);
        purposeEditText.setError(null);
        orgEmailEditText.setError(null);
        deanEmailEditText.setError(null);

        // Check if all required fields are filled
        boolean isValid = true;

        if (nameEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (idNumberEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (bookingDateEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (startTimeEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (endTimeEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (purposeEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (orgEmailEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (deanEmailEditText.getText().toString().isEmpty()) {
            isValid = false;
        }
        if (departmentSpinner.getSelectedItemPosition() == 0) {
            isValid = false;
        }

        // Check for valid emails
        String orgEmail = orgEmailEditText.getText().toString();
        String deanEmail = deanEmailEditText.getText().toString();
        if (orgEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(orgEmail).matches()) {
            isValid = false;
        }
        if (deanEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(deanEmail).matches()) {
            isValid = false;
        }

        if (departmentSpinner.getSelectedItemPosition() == 0) { // Check for Department selection
            isValid = false;
        }

        // Highlight empty fields
        if (!isValid) {
            highlightEmptyFields();
        }

        return isValid;
    }

    private void highlightEmptyFields() {
        if (nameEditText.getText().toString().isEmpty()) {
            nameEditText.setError("Please provide a Name");
        }
        if (idNumberEditText.getText().toString().isEmpty()) {
            idNumberEditText.setError("Please provide Student ID Number");
        }
        if (bookingDateEditText.getText().toString().isEmpty()) {
            bookingDateEditText.setError("Please select a booking date");
        }
        if (startTimeEditText.getText().toString().isEmpty()) {
            startTimeEditText.setError("Please select a start time");
        }
        if (endTimeEditText.getText().toString().isEmpty()) {
            endTimeEditText.setError("Please select an end time");
        }
        if (purposeEditText.getText().toString().isEmpty()) {
            purposeEditText.setError("Please state your purpose");
        }

        // Highlight for Organizational Email
        String orgEmail = orgEmailEditText.getText().toString();
        if (orgEmail.isEmpty()) {
            orgEmailEditText.setError("Please provide your Organizational Email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(orgEmail).matches()) {
            orgEmailEditText.setError("Please provide a valid email address");
        }

        // Highlight for Dean's Email
        String deanEmail = deanEmailEditText.getText().toString();
        if (deanEmail.isEmpty()) {
            deanEmailEditText.setError("Please provide the Dean's Email");
        } else if (!Patterns.EMAIL_ADDRESS.matcher(deanEmail).matches()) {
            deanEmailEditText.setError("Please provide a valid email address");
        }

        // Highlight if Department is not selected
        if (departmentSpinner.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select a Department", Toast.LENGTH_SHORT).show();
            // Optionally, change the background color of the spinner
        }
    }


    private void submitFormToDatabase(String selectedTitle, String selectedDescription, String selectedResourceType) {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "https://swuresource.scarlet2.io/CRBS/submit_reservation.php"; // Update with your API endpoint

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("accountId", accountId); // Ensure accountId is defined
            jsonObject.put("name", nameEditText.getText().toString()); // Updated to 'name'
            jsonObject.put("email", orgEmailEditText.getText().toString()); // Updated to 'email'

            String idNumberWithDashes = idNumberEditText.getText().toString();
            String idNumberForDatabase = idNumberWithDashes.replace("-", "");
            jsonObject.put("school_id", idNumberForDatabase);

            jsonObject.put("department", departmentSpinner.getSelectedItem().toString()); // Get department from the spinner
            jsonObject.put("date_reservation", bookingDateEditText.getText().toString()); // Ensure bookingDate is formatted correctly
            jsonObject.put("time_beg", startTimeEditText.getText().toString()); // Updated to 'time_beg'
            jsonObject.put("time_end", endTimeEditText.getText().toString()); // Updated to 'time_end'
            jsonObject.put("purpose_reservation", purposeEditText.getText().toString()); // Updated to 'purpose_reservation'
            jsonObject.put("facility", selectedResourceType); // Assuming selectedResourceType is the facility
            jsonObject.put("deans_email", deanEmailEditText.getText().toString()); // Updated to 'deans_email'
            jsonObject.put("building", selectedTitle); // Assuming selectedTitle is the building

        } catch (JSONException e) {
            Log.e(TAG, "JSON error: " + e.getMessage());
            Toast.makeText(this, "Failed to create JSON object", Toast.LENGTH_SHORT).show();
            return;
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, jsonObject,
                response -> {
                    // Handle successful response
                    Toast.makeText(ReservationPage.this, "Reservation Sent!", Toast.LENGTH_SHORT).show();
                },
                error -> {
                    // Handle error response
                    Log.e(TAG, "Error: " + error.getMessage());
                    Toast.makeText(ReservationPage.this, "Reservation Failed", Toast.LENGTH_SHORT).show();
                }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(jsonObjectRequest);
    }

    private void showDatePickerDialog() {
        // Reset calendar to current date for picker
        final Calendar calendar = Calendar.getInstance();

        // Set minimum date to 6 days from now
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        long minDate = calendar.getTimeInMillis();

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            bookingDateEditText.setText(selectedDay + "/" + (selectedMonth + 1) + "/" + selectedYear);
            checkAvailability(); // Check availability after date is set
        }, year, month, day);

        // Set the minimum date
        datePickerDialog.getDatePicker().setMinDate(minDate);
        datePickerDialog.show();
    }

    private void showStartTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR); // HOUR returns 12-hour format
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            // Convert to 12-hour format and get AM/PM
            String amPm = selectedHour < 12 ? "AM" : "PM";
            String formattedHour = String.format("%02d", selectedHour % 12 == 0 ? 12 : selectedHour % 12);
            String selectedTime = String.format("%s:%02d %s", formattedHour, selectedMinute, amPm); // Correctly format the time string

            startTimeEditText.setText(selectedTime);
            checkAvailability(); // Check availability after start time is set
        }, hour, minute, false); // Set to false for 12-hour format
        timePickerDialog.show();
    }

    private void showEndTimePickerDialog() {
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR); // HOUR returns 12-hour format
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this, (view, selectedHour, selectedMinute) -> {
            // Convert to 12-hour format and get AM/PM
            String amPm = selectedHour < 12 ? "AM" : "PM";
            String formattedHour = String.format("%02d", selectedHour % 12 == 0 ? 12 : selectedHour % 12);
            String selectedTime = String.format("%s:%02d %s", formattedHour, selectedMinute, amPm); // Correctly format the time string

            endTimeEditText.setText(selectedTime);
            checkAvailability(); // Check availability after end time is set
        }, hour, minute, false); // Set to false for 12-hour format
        timePickerDialog.show();
    }

    @Override
    public void onActivityClick(ActivityItem activity) {
        selectedActivity = activity;
        // Update UI based on the selected activity
    }
}
