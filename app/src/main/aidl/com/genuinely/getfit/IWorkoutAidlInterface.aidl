// IWorkoutAidlInterface.aidl
package com.genuinely.getfit;

import android.location.Location;

interface IWorkoutAidlInterface
{
    void startRecording();
    void stopRecording();
    Location getLastKnownLocation();
    List<String> getSteps();
    List<Location> getLocations();
    boolean getStatus();
    String getStartTime();
    String getEndTime();
}
