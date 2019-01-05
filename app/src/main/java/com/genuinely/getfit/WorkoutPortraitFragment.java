package com.genuinely.getfit;


import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.app.Fragment;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.content.Context.BIND_AUTO_CREATE;
import static android.content.Context.MODE_PRIVATE;


public class WorkoutPortraitFragment extends Fragment implements OnMapReadyCallback {

    ImageView ivUserProfilePicture;
    MapView mapView;
    GoogleMap mMap;
    TextView tvDuration;
    TextView tvDistance;
    Button btnStartWorkout;

    PolylineOptions lineOptions = new PolylineOptions();

    private WorkoutSession duration = new WorkoutSession();
    double stepCount = 0;

    boolean workoutInProgress = false;

    static boolean isRunning;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    Handler handler = new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (!isRunning) return;
            try {
                workoutInProgress = remoteService.getStatus();
                if (workoutInProgress) {
                    btnStartWorkout.setText("Stop Workout");
                    Location location = remoteService.getLastKnownLocation();
                    LatLng position = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 16));

                    Date now = new Date();
                    Date startTime = sdf.parse(remoteService.getStartTime());
                    duration.set(startTime, now);

                    List<String> steps = remoteService.getSteps();
                    stepCount = steps.size();

                    List<Location> locations = remoteService.getLocations();
                    ArrayList<LatLng> positions = new ArrayList<>();
                    for (Location loc: locations){
                        positions.add(new LatLng(loc.getLatitude(), loc.getLongitude()));
                    }

                    mMap.clear();
                    lineOptions = new PolylineOptions();
                    lineOptions.addAll(positions);
                    if (mMap != null) {
                        mMap.addPolyline(lineOptions);
                    }
                }
            }
            catch (RemoteException e){}
            catch (Exception e){}
            refresh();
            handler.postDelayed(this, 100);

        }
    };

    public WorkoutPortraitFragment() {
        // Required empty public constructor
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        try {
            mMap.setMyLocationEnabled(true);
        }
        catch (SecurityException e){}
    }

    @Override
    public void onStart(){
        super.onStart();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_workout_portrait, container, false);
        getViewReady(v);
        refresh();
        registerListeners();
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        initializeRemoteService();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        isRunning = true;
        handler.post(runnable);

        String image = UserProfile.image;
        if (image != null) {
            Bitmap bitmap =  UserProfile.bitmap;
            ivUserProfilePicture.setImageBitmap(bitmap);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        isRunning = false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        getActivity().unbindService(remoteConnection);
        remoteConnection = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    private void getViewReady(View v) {
        ivUserProfilePicture = v.findViewById(R.id.user_profile_picture);
        mapView = (MapView) v.findViewById(R.id.mapview);
        tvDuration = (TextView) v.findViewById(R.id.tvDuration);
        tvDistance = (TextView) v.findViewById(R.id.tvDistance);
        btnStartWorkout = v.findViewById(R.id.btnStartWorkout);
    }

    private void registerListeners() {
        ivUserProfilePicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), UserActivity.class);
                startActivity(intent);
            }
        });

        btnStartWorkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                workoutInProgress = !workoutInProgress;
                if (workoutInProgress) {
                    lineOptions = new PolylineOptions();
                    btnStartWorkout.setText("Stop Workout");
                    duration.reset();
                    tvDuration.setText(duration.toString());
                    try {
                        remoteService.startRecording();
                    }
                    catch (RemoteException e){}
                } else {
                    btnStartWorkout.setText("Start Workout");
                    try {
                        remoteService.stopRecording();
                    }
                    catch (RemoteException e){}
                }
            }
        });
    }

    private void initializeRemoteService() {
        // initialize the service
        remoteConnection = new RemoteConnection();
        Intent intent = new Intent();
        intent.setClassName("com.genuinely.getfit", WorkoutRecordService.class.getName());
        if (!getActivity().bindService(intent, remoteConnection, BIND_AUTO_CREATE)) {
            Toast.makeText(getActivity(),
                    "Fail to bind the remote service.", Toast.LENGTH_LONG).show();
        }
    }


    private void refresh() {
        tvDuration.setText(duration.toString());
        tvDistance.setText(String.format("%.4f", stepCount * UserProfile.step_to_mile));
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
}
