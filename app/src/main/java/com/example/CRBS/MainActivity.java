package com.example.CRBS;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log; // Import Log for logging
import android.widget.ImageButton;
import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ImageButton calendarButton;
    private ImageButton myBookingsButton;
    private ImageButton UtilityButton; // Declare the ImageButton
    private int accountId; // Change to int

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Enable edge-to-edge
        EdgeToEdge.enable(this);

        // Set the content view for the main page layout
        setContentView(R.layout.mainpage);

        // Retrieve the accountId passed from the previous activity
        accountId = getIntent().getIntExtra("accountId", -1); // Use getIntExtra

        // Log the accountId to verify it's being passed
        Log.d("MainActivity", "Received Account ID: " + accountId); // Log accountId

        // Adjust padding based on system bars (for edge-to-edge experience)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize TabLayout and ViewPager2
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        // Set up ViewPager2 adapter
        viewPager.setAdapter(new ViewPagerAdapter(this));

        // Link the TabLayout and ViewPager2
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Event Spaces");
                    break;
                case 1:
                    tab.setText("Teaching Spaces");
                    break;
                case 2:
                    tab.setText("Meeting Rooms");
                    break;
                case 3:
                    tab.setText("Laboratories");
                    break;
            }
        }).attach();

        // Initialize the calendarButton (ImageButton) and set the listener
        calendarButton = findViewById(R.id.calendarButton); // Find the button by ID
        calendarButton.setOnClickListener(v -> {
            // Open the ReservationPage activity
            Intent intent = new Intent(MainActivity.this, ReservationPage.class);
            intent.putExtra("accountId", accountId); // Use accountId
            Log.d("MainActivity", "Navigating to ReservationPage with Account ID: " + accountId); // Log accountId before navigating
            startActivity(intent);
        });

        // Initialize the MyBookingsButton (ImageButton) and set the listener
        myBookingsButton = findViewById(R.id.MyBookingsButton); // Find the button by ID
        myBookingsButton.setOnClickListener(v -> {
            // Open the MyBooking activity
            Intent intent = new Intent(MainActivity.this, MyBooking.class);
            intent.putExtra("accountId", accountId); // Use accountId
            Log.d("MainActivity", "Navigating to MyBooking with Account ID: " + accountId); // Log accountId before navigating
            startActivity(intent);
        });

        UtilityButton = findViewById(R.id.UtilityButton); // Find the button by ID
        UtilityButton.setOnClickListener(v -> {
            // Open the RequestRepair activity
            Intent intent = new Intent(MainActivity.this, RequestRepair.class);
            intent.putExtra("accountId", accountId); // Use accountId
            Log.d("MainActivity", "Navigating to RequestRepair with Account ID: " + accountId); // Log accountId before navigating
            startActivity(intent);
        });
    }

    // Adapter to manage fragments for each tab
    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            // Return a different fragment for each tab
            switch (position) {
                case 0:
                    return new EventSpacesFragment();
                case 1:
                    return new TeachingSpacesFragment();
                case 2:
                    return new MeetingRoomsFragment();
                case 3:
                    return new LaboratoriesFragment();
                default:
                    return new EventSpacesFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 4; // Number of tabs
        }
    }
}
