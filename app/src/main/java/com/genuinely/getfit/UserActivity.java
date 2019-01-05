package com.genuinely.getfit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class UserActivity extends AppCompatActivity {

    ImageView ivPicture;
    TextView tvName, tvGender, tvWeight;
    TextView average_distance, average_time, average_workouts, average_calories;
    TextView all_distance, all_time, all_workouts, all_calories;

    private static final int PICK_IMAGE = 100;
    private static final int WEEKS = 3;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        tvName = (TextView) findViewById(R.id.name);
        tvGender = (TextView) findViewById(R.id.gender);
        tvWeight = (TextView) findViewById(R.id.weight);
        ivPicture = (ImageView) findViewById(R.id.picture);

        average_distance = (TextView) findViewById(R.id.average_distance);
        average_time = (TextView) findViewById(R.id.average_time);
        average_workouts = (TextView) findViewById(R.id.average_workouts);
        average_calories = (TextView) findViewById(R.id.average_calories);
        all_distance = (TextView) findViewById(R.id.all_distance);
        all_time = (TextView) findViewById(R.id.all_time);
        all_workouts = (TextView) findViewById(R.id.all_workouts);
        all_calories = (TextView) findViewById(R.id.all_calories);

        UserProfile.initiallize(getApplicationContext());

        tvName.setText(UserProfile.name);
        tvGender.setText(UserProfile.gender);
        tvWeight.setText(UserProfile.weight + " lbs");
        if (UserProfile.bitmap!=null) {
            ivPicture.setImageBitmap(UserProfile.bitmap);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        updateUI();
    }

    public void updateUI(){
        long stepCount = 0;
        long time = 0;
        int workouts = 0;

        String URL = "content://com.willsuwei.alphafitness/devices";
        Uri devices = Uri.parse(URL);
        Cursor c = managedQuery(devices, null, null, null, WorkoutContentProvider._ID);

        Date startDate, endDate;
        if (c.moveToFirst()) {
            do {
                Log.w("======",
                        c.getString(c.getColumnIndex(WorkoutContentProvider._ID))
                                + ", "
                                +  c.getString(
                                c.getColumnIndex( WorkoutContentProvider.START))
                                + ", "
                                + c.getString(
                                c.getColumnIndex( WorkoutContentProvider.END))
                                + ", "
                                + c.getString(
                                c.getColumnIndex( WorkoutContentProvider.COUNT))
                );

                try {
                    workouts++;
                    startDate = sdf.parse(c.getString(c.getColumnIndex(WorkoutContentProvider.START)));
                    endDate = sdf.parse(c.getString(c.getColumnIndex(WorkoutContentProvider.END)));
                    time += (endDate.getTime() - startDate.getTime());
                    int count = Integer.parseInt(c.getString(c.getColumnIndex(WorkoutContentProvider.COUNT)));
                    stepCount += count;
                    Log.w("+++++", workouts + " " + time + " " + stepCount);
                }
                catch (Exception e){}
            } while (c.moveToNext());
        }

        average_distance.setText(String.format("%.4f", stepCount * UserProfile.step_to_mile / WEEKS) + " miles");
        WorkoutSession duration = new WorkoutSession();
        duration.set(time / 1000 / WEEKS);
        average_time.setText(duration.toString());
        average_workouts.setText(workouts / WEEKS + " times");
        average_calories.setText(String.format("%.4f", stepCount * UserProfile.step_to_calories / WEEKS) + " Cal");

        all_distance.setText(String.format("%.4f", stepCount * UserProfile.step_to_mile) + " miles");
        duration = new WorkoutSession();
        duration.set(time / 1000);
        all_time.setText(duration.toString());
        all_workouts.setText(workouts + " times");
        all_calories.setText(String.format("%.4f", stepCount * UserProfile.step_to_calories) + " Cal");

    }

    public void editName(View view){
        final EditText etName = new EditText(this);
        etName.setHint("Enter your name here");
        etName.setGravity(Gravity.CENTER);
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Edit Name")
                .setView(etName)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserProfile.name = etName.getText().toString();
                        tvName.setText(UserProfile.name);
                        UserProfile.saveData();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void editGender(View view){
        final Spinner spinner = new Spinner(this);
        ArrayList<String> list = new ArrayList<>();
        list.add(UserProfile.MALE);
        list.add(UserProfile.FEMALE);
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Select Gender")
                .setView(spinner)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserProfile.gender = spinner.getSelectedItem().toString();
                        tvGender.setText(UserProfile.gender);
                        UserProfile.saveData();
                        updateUI();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void editWeight(View view){
        final EditText etWeight = new EditText(this);
        etWeight.setHint("Enter your weight here");
        etWeight.setGravity(Gravity.CENTER);

        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle("Edit Weight")
                .setView(etWeight)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        UserProfile.weight = Integer.parseInt(etWeight.getText().toString());
                        tvWeight.setText(UserProfile.weight + " lbs");
                        UserProfile.saveData();
                        updateUI();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .show();
    }

    public void choosePicture(View v){
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == PICK_IMAGE){
            try {
                Uri imageUri = data.getData();
                ivPicture.setImageURI(imageUri);
                BitmapDrawable drawable = (BitmapDrawable) ivPicture.getDrawable();
                UserProfile.bitmap = drawable.getBitmap();
                UserProfile.saveData();
            }
            catch (Exception e){
            }
        }
    }
}
