package com.genuinely.getfit;

import android.Manifest;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WorkoutRecordService extends Service implements SensorEventListener, StepRecordable {

    IWorkoutAidlInterface.Stub mBinder;
    boolean workoutInProgress = false;
    Date startTime, endTime;
    private StepCounter simpleStepCounter;
    SensorManager sensorManager;
    Sensor stepDetectorSensor, accelerometerSeosor;
    Handler handler = new Handler();
    ArrayList<String> steps = new ArrayList<>();
    ArrayList<Location> locations = new ArrayList<>();
    private LocationManager locationManager;
    private String locationProvider;
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    public WorkoutRecordService() {
    }

    @Override
    public void onCreate() {

        super.onCreate();
        mBinder = new IWorkoutAidlInterface.Stub() {
            public void startRecording(){
                Log.w("==========", "Start Recording");
                locations.clear();
                steps.clear();
                workoutInProgress = true;
                startTime = new Date();
                endTime = null;
            }

            public void stopRecording(){
                workoutInProgress = false;
                endTime = new Date();
                Log.i("==========", "Stop Recording");

                ContentValues contentValues = new ContentValues();
                contentValues.put(
                        WorkoutContentProvider.START,
                        sdf.format(startTime));
                contentValues.put(
                        WorkoutContentProvider.END,
                        sdf.format(new Date()));
                contentValues.put(
                        WorkoutContentProvider.COUNT,
                        Integer.toString(steps.size()));
                Uri uri = getContentResolver().insert(
                        WorkoutContentProvider.URI,
                        contentValues);
            }

            public Location getLastKnownLocation(){
                if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getApplicationContext(),
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return null;
                }
                return locationManager.getLastKnownLocation(locationProvider);
            }

            public List<Location> getLocations(){
                return locations;
            }

            public List<String> getSteps(){
                return steps;
            }

            public boolean getStatus(){
                return workoutInProgress;
            }

            public String getStartTime(){
                if (startTime != null) return sdf.format(startTime);
                else return null;
            }

            public String getEndTime(){
                if (endTime != null) return sdf.format(endTime);
                else return null;
            }

        };

        startLocationService();
        startStepService();
    }

    @Override
    public void onDestroy(){
        if (locationListener!=null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    void startLocationService() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_LOW);

        locationProvider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        Location location = locationManager.getLastKnownLocation(locationProvider);
        saveLocation(location);
        locationManager.requestLocationUpdates(locationProvider, 1000, 1, locationListener);
    }

    android.location.LocationListener locationListener =  new android.location.LocationListener() {

        @Override
        public void onStatusChanged(String provider, int status, Bundle arg2) {

        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onLocationChanged(Location location) {
            saveLocation(location);
        }
    };

    private void saveLocation(Location location){
        if (!workoutInProgress) return;
        if (location == null) return;
        locations.add(location);
    }

    void startStepService(){
        simpleStepCounter = new StepCounter();
        simpleStepCounter.registerListener(this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepDetectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        accelerometerSeosor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (stepDetectorSensor != null) { // if STEP_DETECTOR is available
            sensorManager.registerListener(this, stepDetectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else { // otherwise use the accelerometer sensor
            sensorManager.registerListener(this, accelerometerSeosor, SensorManager.SENSOR_DELAY_FASTEST);
        }

        // simulate steps
        /*
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                recordStep();
                Random ran = new Random();
                handler.postDelayed(this, ran.nextInt(750) + 250);
            }
        };
        handler.postDelayed(runnable, 2000);
        */
    }

    @Override
    public void onSensorChanged(SensorEvent event){
        if (event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){
            recordStep();
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            simpleStepCounter.updateAccel(
                    event.timestamp, event.values[0], event.values[1], event.values[2]);
        }
    }

    void recordStep(){
        if (!workoutInProgress) return;
        Date date = new Date();
        steps.add(sdf.format(date));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy){
    }

    @Override
    public void step(long timeNs) {
        Log.w("=====step:", "time"+ timeNs);
        recordStep();
        //numSteps++;
        //TvSteps.setText(TEXT_NUM_STEPS + numSteps);
    }
}
