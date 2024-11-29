package com.example.CRBS;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog; // Import AlertDialog
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Import SwipeRefreshLayout
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MyBooking extends AppCompatActivity {

    private int accountId; // Declare the accountId variable
    private LinearLayout reservationContainer;
    private SwipeRefreshLayout swipeRefreshLayout; // SwipeRefreshLayout for refresh functionality

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mybookings); // Set content view to mybookings.xml

        // Retrieve the accountId passed from the previous activity
        accountId = getIntent().getIntExtra("accountId", -1); // Use getIntExtra

        // Log the accountId to verify it's being passed
        Log.d("MyBooking", "Received Account ID: " + accountId); // Log accountId

        // Initialize the back button and set the click listener
        ImageButton backButton = findViewById(R.id.backbutton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the current activity and return to the previous one
                finish();
            }
        });

        // Reference to the reservation container
        reservationContainer = findViewById(R.id.reservationContainer);

        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout); // Make sure you have this in your XML
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                // Fetch the reservation data again on refresh
                fetchBookingsData(reservationContainer);
            }
        });

        // Fetch the reservation data from the PHP script
        fetchBookingsData(reservationContainer);
    }

    private void fetchBookingsData(LinearLayout reservationContainer) {
        String url = "https://swuresource.scarlet2.io/CRBS/get_reservations.php?accountId=" + accountId; // Append accountId to the URL

        RequestQueue queue = Volley.newRequestQueue(this);

        // Show the loading spinner
        swipeRefreshLayout.setRefreshing(true);

        // Create a JSON Array request
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d("MyBooking", "Response: " + response.toString());
                        // Inflate the reservation layout for each booking dynamically
                        LayoutInflater inflater = LayoutInflater.from(MyBooking.this);
                        reservationContainer.removeAllViews(); // Clear previous bookings

                        try {
                            // Loop over the booking data from the response
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject booking = response.getJSONObject(i);

                                // Inflate the layout for each booking
                                View reservationView = inflater.inflate(R.layout.mybookings_layout, reservationContainer, false);

                                // Fill the reservation details with data
                                TextView eventNameTextView = reservationView.findViewById(R.id.eventNameTextView);
                                TextView dateTextView = reservationView.findViewById(R.id.dateTextView);
                                TextView resourcesTextView = reservationView.findViewById(R.id.resourcesTextView);
                                TextView bookedByTextView = reservationView.findViewById(R.id.bookedByTextView);
                                TextView adminStatusTextView = reservationView.findViewById(R.id.statusTextView);
                                Button detailsButton = reservationView.findViewById(R.id.detailsButton); // Add this line for the details button

                                // Populate views with the booking data
                                eventNameTextView.setText(booking.getString("purpose_reservation"));
                                String timeFrame = formatTime(booking.getString("time_beg")) + " - " + formatTime(booking.getString("time_end"));
                                dateTextView.setText(booking.getString("date_reservation") + " | " + timeFrame);
                                resourcesTextView.setText(booking.getString("building") + " | " + booking.getString("facility"));
                                bookedByTextView.setText(booking.getString("name"));
                                adminStatusTextView.setText(booking.getString("admin_status"));

                                // Add click listener to details button
                                detailsButton.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        showBookingDetailsDialog(booking);
                                    }
                                });

                                // Add the view to the container
                                reservationContainer.addView(reservationView, 0);
                            }
                        } catch (JSONException e) {
                            Log.e("MyBooking", "JSON Exception: " + e.getMessage());
                            e.printStackTrace();
                        } finally {
                            // Stop the refreshing animation
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error here
                        Log.e("MyBooking", "Error fetching bookings data: " + error.toString());
                        error.printStackTrace();
                        // Stop the refreshing animation
                        swipeRefreshLayout.setRefreshing(false);
                    }
                });

        // Add the request to the RequestQueue
        queue.add(jsonArrayRequest);
    }

    private void showBookingDetailsDialog(JSONObject booking) {
        // Inflate the dialog layout
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.mybookings_dialog, null);

        // Create the dialog
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Initialize the views in the dialog
        TextView eventNameTextView = dialogView.findViewById(R.id.dialogEventName);
        TextView resourcesTextView = dialogView.findViewById(R.id.dialogResources);
        TextView bookedByTextView = dialogView.findViewById(R.id.dialogBookedBy);
        TextView dateReservedTextView = dialogView.findViewById(R.id.dialogDateReserved);
        TextView bookingDateTextView = dialogView.findViewById(R.id.dialogBookingDate);
        TextView deanEmailTextView = dialogView.findViewById(R.id.dialogDeanEmail);
        TextView orgEmailTextView = dialogView.findViewById(R.id.dialogOrgEmail);
        TextView departmentTextView = dialogView.findViewById(R.id.dialogDepartment);
        TextView orgOfficeTextView = dialogView.findViewById(R.id.dialogOrgOffice);
        TextView requestStatusTextView = dialogView.findViewById(R.id.dialogRequestStatus);
        TextView referenceNoTextView = dialogView.findViewById(R.id.dialogReferenceNo);
        TextView schoolIdTextView = dialogView.findViewById(R.id.dialogSchoolId);
        TextView timeframeTextView = dialogView.findViewById(R.id.dialogTimeframe);
        TextView remarksTextView = dialogView.findViewById(R.id.dialogRemarks);

        try {
            // Populate the dialog views with booking data
            eventNameTextView.setText("Event | Purpose: " + booking.getString("purpose_reservation"));
            bookedByTextView.setText("Booker: " + booking.getString("name"));
            schoolIdTextView.setText("School ID: " + booking.getString("school_id"));
            String timeFrame = formatTime(booking.getString("time_beg")) + " - " + formatTime(booking.getString("time_end"));
            timeframeTextView.setText("Timeframe: " + timeFrame);
            resourcesTextView.setText("Venue Reserved: " + booking.getString("building") + " | " + booking.getString("facility"));
            dateReservedTextView.setText("Date Reserved: " + booking.getString("date_reservation"));

            // Set the booking date
            bookingDateTextView.setText("Date of Booking: " + booking.getString("booking_date")); // Display booking date

            // Fetch the username based on account_id
            int accountId = booking.getInt("account_id");
            getOrgOffice(accountId, orgOfficeTextView);

            deanEmailTextView.setText("Dean's Email: " + booking.getString("deans_email"));
            orgEmailTextView.setText("Organization Email: " + booking.getString("email"));
            departmentTextView.setText("Department: " + booking.getString("department"));

            // Set request status and reference number
            String status = booking.getString("admin_status");
            requestStatusTextView.setText("Request Status: " + status);
            referenceNoTextView.setText("Reference No: " + booking.getString("id"));

            // Enhanced logging for remarks field
            String remarks = booking.optString("remarks", null); // Retrieve remarks with null default
            if (remarks == null) {
                Log.d("MyBooking", "Remarks field is null for booking ID: " + booking.getString("id"));
                remarksTextView.setText("Remarks: No remarks provided");
            } else {
                remarksTextView.setText("Remarks: " + remarks);
                Log.d("MyBooking", "Remarks for booking ID " + booking.getString("id") + ": " + remarks);
            }

            if ("Pending".equals(status)) {
                remarksTextView.setText("Remarks: Pending");
            } else if ("Approved".equals(status)) {
                remarksTextView.setText("Remarks: Approved");
            }

        } catch (JSONException e) {
            Log.e("MyBooking", "JSON Exception in dialog: " + e.getMessage());
            e.printStackTrace();
        }

        // Show the dialog
        dialog.show();
    }

    private String formatTime(String time) {
        // Assuming time is in "HH:mm" format; change it if needed
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("HH:mm");
            SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a");
            Date date = inputFormat.parse(time);
            return outputFormat.format(date);
        } catch (ParseException e) {
            Log.e("MyBooking", "Parse Exception: " + e.getMessage());
            e.printStackTrace();
            return time; // Return original if parsing fails
        }
    }

    private void getOrgOffice(int accountId, TextView orgOfficeTextView) {
        String url = "https://swuresource.scarlet2.io/CRBS/get_username.php?accountId=" + accountId; // Adjust the URL

        RequestQueue queue = Volley.newRequestQueue(this);

        // Create a JSON Object request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            String username = response.getString("username");
                            orgOfficeTextView.setText("Organization | Office: " + username); // Set the username in the TextView
                        } catch (JSONException e) {
                            Log.e("MyBooking", "JSON Exception in org office request: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle error here
                        Log.e("MyBooking", "Error fetching org office: " + error.toString());
                        error.printStackTrace();
                    }
                });

        // Add the request to the RequestQueue
        queue.add(jsonObjectRequest);
    }
}
