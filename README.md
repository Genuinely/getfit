# GetFit

GetFit is an Android mobile application that helps people get fit! It tracks your running workouts using GPS and the Google Maps API. It can show you the duration of the run, the location of the run, and how many calories are burnt from the run.

## Requirement

**Minimum Android SDK API Level**: require API 21: Android 5.0 (Lollipop).  This app shall run on > 85% of all Android phone and tablet devices.

**Google MAP API**: GetFit uses Goggle Maps API.  You need to acquire your own API key in order for this app to work properly.  Go to your [Google Developer Account](https://developers.google.com/maps/documentation/android/start#get-key) and get a Google Maps API key.  Don't worry, it is free of charge.  Your key starts with "*AIza*".  Once you have your Google MAP API key, go to */res/values/google_map.xml* and replace the key value with your own key.  

## Features

Below is a list of main features implemented in GetFit.

### (1) Record Workout
The Record Workout Screen records how far and how long you've run. The distance is in km and the duration is in minutes. There is a map showing the current location as well has where you have run.

### (2) Workout Details
To view the workout details, simply rotate the screen 90 degrees. This feature makes use of [fragments](https://developer.android.com/guide/components/fragments), which can be thought of as "mini-Activities". These fragments make it so that GetFit has the capability to show how far you've run, how long you've run, and calories burnt. It can also show all time distance ran, time ran, and calories burnt.

### (3) All Workouts Stored
All workouts are stored in a [SQLite database](https://developer.android.com/training/data-storage/sqlite), so they can be viewed at a later time. 

### (4) GPS and Google Map Integration
The app uses [Google Maps API](https://developers.google.com/maps/documentation/android-sdk/start) to track location and uses the phone's internal accelerometer to calculate steps.

### (5) Calorie Tracking Algorithm
The calories burnt is calculated through the number of steps taken based off this [source](https://www.verywellfit.com/pedometer-steps-to-calories-converter-3882595). 

## Assumptions

I made several assumptions for the distance and calories calculation:

### (1) Steps per Mile (SPM)

In getfit, SPM calculation is gender based:
* Male: 2112 steps in mile
* Female: 2400 steps in mile

A more rigorious modeling of SPM can be based on the type of activities (slow walking, running, etc.) and the height of a person.  You can find more information from this [source](https://www.verywellfit.com/how-many-walking-steps-are-in-a-mile-3435916).

### (2) Calories per Weight (CPW)

CPW calculation is tabulated below:


| Weight | Calries per step |
|--------|------------------|
| < 120     | 0.028 |
| 120 - 139 | 0.033 |
| 140 - 159 | 0.038 |
| 160 - 179 | 0.044 |
| 180 - 199 | 0.049 |
| 200 - 219 | 0.055 |
| 220 - 249 | 0.060 |
| 250 - 274 | 0.069 |
| 275 - 299 | 0.075 |
| >= 300 | 0.082 |



