package com.genuinely.getfit;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;

import static android.content.Context.MODE_PRIVATE;

public class UserProfile {
    static SharedPreferences preferences;
    static SharedPreferences.Editor editor;

    final static String KEY_NAME = "name";
    final static String KEY_GENDER = "gender";
    final static String KEY_WEIGHT = "weight";
    final static String KEY_IMAGE = "image";
    final static String MALE = "Male";
    final static String FEMALE = "Female";
    final static double MALE_STEP_TO_MILE =  0.000473485; // 2112 steps
    final static double FEMALE_STEP_TO_MILE =  0.000416667; // 2400 steps

    static String name, gender, image;
    static int weight;
    static double step_to_mile, step_to_calories;
    static Bitmap bitmap;

    static void initiallize(Context context){
        preferences = context.getSharedPreferences("MyPreferences", MODE_PRIVATE);
        editor = preferences.edit();
        retriveData();
        saveData();
    }

    static void retriveData(){
        name = preferences.getString(KEY_NAME, "Jone Doe");
        gender = preferences.getString(KEY_GENDER, MALE);
        weight = preferences.getInt(KEY_WEIGHT, 150);
        image =  preferences.getString(KEY_IMAGE, null);

        step_to_mile = miles_of_gender(gender);
        step_to_calories = calories_of_weight(weight);
        if (image!=null) bitmap = decodeBase64(image);
        else bitmap = null;
    }

    static void saveData(){
        step_to_mile = miles_of_gender(gender);
        step_to_calories = calories_of_weight(weight);
        image = encodeTobase64(bitmap);

        editor.putString(KEY_NAME, name);
        editor.putString(KEY_GENDER, gender);
        editor.putInt(KEY_WEIGHT, weight);
        editor.putString(KEY_IMAGE, image);
        editor.commit();
    }

    static double miles_of_gender(String gender){
        if (gender == MALE) return MALE_STEP_TO_MILE;
        else return FEMALE_STEP_TO_MILE;
    }

    static double calories_of_weight(int weight){
        double result;
        if (weight<100) result = 28;
        else if (weight>=100 && weight<120) result = 28;
        else if (weight>=120 && weight<140) result = 33;
        else if (weight>=140 && weight<160) result = 38;
        else if (weight>=160 && weight<180) result = 44;
        else if (weight>=180 && weight<200) result = 49;
        else if (weight>=200 && weight<220) result = 55;
        else if (weight>=220 && weight<250) result = 60;
        else if (weight>=250 && weight<275) result = 69;
        else if (weight>=275 && weight<300) result = 75;
        else result = 82;
        return result/1000.0;
    }

    public static String encodeTobase64(Bitmap image) {
        if (image == null) return null;
        Bitmap immage = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immage.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);

        Log.d("Image Log:", imageEncoded);
        return imageEncoded;
    }

    public static Bitmap decodeBase64(String input) {
        if (input == null) return null;
        byte[] decodedByte = Base64.decode(input, 0);
        return BitmapFactory
                .decodeByteArray(decodedByte, 0, decodedByte.length);
    }
}
