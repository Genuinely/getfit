package com.genuinely.getfit;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.BIND_AUTO_CREATE;
import static java.lang.Math.max;
import static java.lang.Math.min;


/**
 * A simple {@link Fragment} subclass.
 */
public class WorkoutLandscapeFragment extends Fragment {

    TextView tvAvgSpeed, tvMaxSpeed, tvMinSpeed;

    static boolean isRunning;

    LineChart chart;

    final static long INFINITY = 100000000;

    long averageSpeed = 0, maxSpeed = INFINITY, minSpeed = -INFINITY; // unit in seconds/miles. smaller means higher

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);


    public WorkoutLandscapeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workout_landscape, container, false);

        tvAvgSpeed = v.findViewById(R.id.average);
        tvMaxSpeed = v.findViewById(R.id.max);
        tvMinSpeed = v.findViewById(R.id.min);
        chart = (LineChart) v.findViewById(R.id.chart);

        // initialize the service
        remoteConnection = new RemoteConnection();
        Intent intent = new Intent();
        intent.setClassName("com.willsuwei.alphafitness", WorkoutRecordService.class.getName());
        if (!getActivity().bindService(intent, remoteConnection, BIND_AUTO_CREATE)) {
            Toast.makeText(getActivity(),
                    "Fail to bind the remote service.", Toast.LENGTH_LONG).show();
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        isRunning = true;
        handler.post(runnable);
    }

    @Override
    public void onPause() {
        super.onPause();
        isRunning = false;
    }

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            try {
                Date startTime;
                String timeString = remoteService.getStartTime();
                if (timeString == null) startTime = new Date();
                else startTime = sdf.parse(timeString);

                Date endTime;
                timeString = remoteService.getEndTime();
                if (timeString == null) endTime = new Date();
                else endTime = sdf.parse(timeString);

                List<String> steps = remoteService.getSteps();
                updateChart(startTime, endTime, steps);
            }
            catch (RemoteException e){}
            catch (Exception e){}
            handler.postDelayed(this, 1000);
        }
    };

    final static long STEP_DURATION = 5000;
    final static int MAX_POINTS = 13;
    void updateChart(Date startTime, Date endTime, List<String> steps){
        averageSpeed = (int) ((endTime.getTime() - startTime.getTime()) / 1000 / (steps.size() * UserProfile.step_to_mile));

        List<Entry> stepEntries = new ArrayList<Entry>();
        List<Entry> caloriesEntries = new ArrayList<Entry>();

        for (int i=0; i<MAX_POINTS; i++){
            stepEntries.add(new Entry(0, 0));
            caloriesEntries.add(new Entry(0, 0));
        }
        for (long i = startTime.getTime(); i < endTime.getTime() - STEP_DURATION; i+=STEP_DURATION){
            int stepCount = 0;
            for (String step: steps){
                try {
                    Date date = sdf.parse(step);
                    if (date.getTime() >= i && date.getTime() < i + STEP_DURATION){
                        stepCount++;
                        Log.w("=====", step);
                        Log.e("=====", "" + stepCount);
                    }
                }
                catch (Exception e){}
            }
            if (stepCount != 0) {
                long speed = (int) (STEP_DURATION / 1000 / (stepCount * UserProfile.step_to_mile));
                maxSpeed = min(speed, maxSpeed);
                minSpeed = max(speed, minSpeed);
            }
            stepEntries.add(new Entry((i-startTime.getTime()) / 1000, stepCount));
            caloriesEntries.add(new Entry((i-startTime.getTime()) / 1000, (float) (stepCount * UserProfile.step_to_calories * 100)));
            stepEntries.remove(0);
            caloriesEntries.remove(0);
        }

        for (int i=0; i<MAX_POINTS; i++){
            stepEntries.get(i).setX(i*5);
            caloriesEntries.get(i).setX(i*5);
        }

        LineDataSet stepDataSet = new LineDataSet(stepEntries, "Steps per 5 sec"); // add entries to dataset
        stepDataSet.setColor(Color.GREEN);
        stepDataSet.setValueTextColor(Color.BLACK); // styling, ...

        LineDataSet caloriesDataSet = new LineDataSet(caloriesEntries, "Calories (Scaled 100:1)"); // add entries to dataset
        caloriesDataSet.setColor(Color.RED);
        caloriesDataSet.setValueTextColor(Color.BLACK); // styling, ...

        List<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(stepDataSet);
        dataSets.add(caloriesDataSet);


        LineData lineData = new LineData(dataSets);
        chart.setData(lineData);
        chart.invalidate(); // refresh

        tvAvgSpeed.setText(formatSpeed(averageSpeed));
        if (maxSpeed != INFINITY) tvMaxSpeed.setText(formatSpeed(maxSpeed));
        else tvMaxSpeed.setText("--:--");
        if (minSpeed != -INFINITY) tvMinSpeed.setText(formatSpeed(minSpeed));
        else tvMinSpeed.setText("--:--");
    }

    String formatSpeed(long speed){
        return String.format("%02d", speed / 60) + ":" + String.format("%02d", speed % 60);
    }


    IWorkoutAidlInterface remoteService;
    RemoteConnection remoteConnection = null;

    class RemoteConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            remoteService = IWorkoutAidlInterface.Stub.asInterface((IBinder) service);
            Toast.makeText(getActivity(),
                    "Remote Service connected.", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            remoteService = null;
            Toast.makeText(getActivity(),
                    "Remote Service disconnected.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unbindService(remoteConnection);
        remoteConnection = null;
    }

}
