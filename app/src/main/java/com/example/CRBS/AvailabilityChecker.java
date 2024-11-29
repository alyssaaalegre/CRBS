package com.example.CRBS;

import android.util.Log;
import android.content.Context;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;

public class AvailabilityChecker {
    private static final String TAG = "AvailabilityChecker";
    private RequestQueue requestQueue;

    public AvailabilityChecker(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void checkAvailability(String date, String startTime, String endTime, AvailabilityCallback callback) {
        String url = "https://swuresource.scarlet2.io/CRBS/get_filter_reservations.php?date=" + date + "&start_time=" + startTime + "&end_time=" + endTime;
        Log.d(TAG, "Request URL: " + url);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        if (response.getBoolean("success")) {
                            JSONArray reservations = response.getJSONArray("reservations");
                            callback.onSuccess(reservations);
                        } else {
                            callback.onError(response.getString("message"));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "JSON error: " + e.getMessage());
                        callback.onError("JSON parsing error");
                    }
                },
                error -> {
                    Log.e(TAG, "Request error: " + error.getMessage());
                    callback.onError("Request failed");
                }
        );

        requestQueue.add(jsonObjectRequest);
    }

    public interface AvailabilityCallback {
        void onSuccess(JSONArray reservations);
        void onError(String message);
    }
}
