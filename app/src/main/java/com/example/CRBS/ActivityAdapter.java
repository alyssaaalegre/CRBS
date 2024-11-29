package com.example.CRBS;

import static com.android.volley.VolleyLog.TAG;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final List<ActivityItem> activities; // Original list of activities
    public List<ActivityItem> filteredActivities; // Separate list for filtered activities
    private final OnActivityClickListener activityClickListener; // Add listener interface

    public ActivityAdapter(List<ActivityItem> activities, OnActivityClickListener activityClickListener) {
        this.activities = activities;
        this.filteredActivities = new ArrayList<>(activities); // Initialize filtered activities with all activities initially
        this.activityClickListener = activityClickListener; // Initialize the listener
    }

    // Define the interface
    public interface OnActivityClickListener {
        void onActivityClick(ActivityItem activityItem);
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        ActivityItem activity = filteredActivities.get(position); // Bind data from filtered list
        holder.activityTitle.setText(activity.getTitle());
        holder.activityDescription.setText(activity.getDescription());

        // Highlight selected item
        if (activity.isSelected()) {
            holder.itemView.setBackgroundColor(Color.LTGRAY); // Highlight color
        } else {
            holder.itemView.setBackgroundColor(Color.TRANSPARENT); // Default color
        }

        // Set click listener for the entire item
        holder.itemView.setOnClickListener(v -> {
            // Clear selection for all items in the filtered list
            for (ActivityItem item : filteredActivities) {
                item.setSelected(false);
            }

            // Set selected for the clicked item
            activity.setSelected(true);
            activityClickListener.onActivityClick(activity); // Notify the listener with the clicked activity

            notifyDataSetChanged(); // Refresh the adapter
        });
    }

    @Override
    public int getItemCount() {
        return filteredActivities.size(); // Return count of filtered activities
    }

    public void updateActivities(List<ActivityItem> newFilteredActivities) {
        Log.d(TAG, "Updating activities. New size: " + newFilteredActivities.size());
        this.filteredActivities.clear();
        this.filteredActivities.addAll(newFilteredActivities);
        notifyDataSetChanged(); // This can be kept as a safety net
    }

    public void filterActivitiesByReservations(List<String> reservedBuildings) {
        List<ActivityItem> newFilteredActivities = new ArrayList<>();

        // Keep track of the original size for notifications
        int originalSize = filteredActivities.size();

        for (ActivityItem activity : activities) {
            if (!reservedBuildings.contains(activity.getTitle().trim().toLowerCase())) {
                newFilteredActivities.add(activity); // Only add available activities
            }
        }

        // Determine the new size after filtering
        int newSize = newFilteredActivities.size();

        // Update the filtered activities
        filteredActivities.clear();
        filteredActivities.addAll(newFilteredActivities);

        // Notify the adapter of changes
        if (newSize < originalSize) {
            // If items were removed
            for (int i = originalSize - 1; i >= newSize; i--) {
                notifyItemRemoved(i);
            }
        } else if (newSize > originalSize) {
            // If items were added
            for (int i = originalSize; i < newSize; i++) {
                notifyItemInserted(i);
            }
        }
        notifyDataSetChanged(); // Use as a last resort if structural changes occur
    }


    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView activityTitle;
        TextView activityDescription;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            activityTitle = itemView.findViewById(R.id.activityTitle);
            activityDescription = itemView.findViewById(R.id.activityDescription);
        }
    }
}
