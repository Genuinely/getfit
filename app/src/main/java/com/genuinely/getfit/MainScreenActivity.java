package com.genuinely.getfit;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);

        Configuration config = getResources().getConfiguration();
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (config.orientation == Configuration.ORIENTATION_LANDSCAPE) {

            WorkoutLandscapeFragment workoutLandscapeFragment = new WorkoutLandscapeFragment();
            fragmentTransaction.replace(android.R.id.content, workoutLandscapeFragment);

        } else {
            WorkoutPortraitFragment workoutPortraitFragment = new WorkoutPortraitFragment();
            fragmentTransaction.replace(android.R.id.content, workoutPortraitFragment);
        }
        fragmentTransaction.commit();
    }
}
