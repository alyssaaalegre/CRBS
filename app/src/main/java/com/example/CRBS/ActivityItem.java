package com.example.CRBS;

import static com.android.volley.VolleyLog.TAG;

import android.util.Log;

public class ActivityItem {
    private String title;
    private String description;
    private String resourceType; // Field for resource type
    private int capacity;
    private boolean isSelected; // Add this field

    // Constructor
    public ActivityItem(String title, String description, String resourceType) {
        this.title = title.trim();
        this.description = description;
        this.resourceType = resourceType; // Initialize resource type
        this.isSelected = false; // Default value
    }

    // Getter methods
    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getResourceType() {
        return resourceType; // Getter for resource type
    }
    public boolean isSelected() { return isSelected;}

    public void setSelected(boolean selected) {isSelected = selected;}

    // Method to extract capacity from the description
    public int getCapacity() {
        if (description != null) {
            try {
                String[] parts = description.split("\\| Capacity ");
                if (parts.length > 1) {
                    String capacityStr = parts[1].trim();
                    return Integer.parseInt(capacityStr);
                }
            } catch (NumberFormatException e) {
                Log.e(TAG, "Capacity parsing error: " + e.getMessage());
            }
        }
        return 0; // Default to 0 if not found or cannot be parsed
    }

    // Method to get the type (equivalent to getResourceType)
    public String getType() {
        return resourceType;
    }
}
