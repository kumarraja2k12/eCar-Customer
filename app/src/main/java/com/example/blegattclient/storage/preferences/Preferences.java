package com.example.blegattclient.storage.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.blegattclient.services.pojo.Threshold;

import java.util.List;

public class Preferences {

    private static Preferences instance;
    public static Preferences getInstance(Context appContext) {
        if(instance == null) {
            instance = new Preferences(appContext);
        }
        return instance;
    }

    private Context context;
    public Preferences(Context appContext) {
        context = appContext;
    }

    private final String KEY_VEHICLE_NUMBER = "vehicle_number";
    private final String KEY_THRESHOLD_LEVEL = "level";
    private final String KEY_THRESHOLD_PPM = "ppm";

    private final String KEY_PREFERENCES_NAME = "MyPreferences";

    public boolean writeVehicleNumber(String vehicleNumber) {

        SharedPreferences pref = getSharedPreferences();
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(KEY_VEHICLE_NUMBER, vehicleNumber);
        editor.apply();
        return editor.commit(); // commit changes
    }

    private SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(KEY_PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    public String readVehicleNumber() {
        SharedPreferences pref = getSharedPreferences(); // 0 - for private mode
        return pref.getString(KEY_VEHICLE_NUMBER, null); // getting String
    }

    public boolean writeThresholds(List<Threshold> thresholds) {

        if(thresholds.size() > 0) {
            SharedPreferences pref = getSharedPreferences();
            SharedPreferences.Editor editor = pref.edit();
            for (Threshold threshold : thresholds) {
                if (threshold.type.equalsIgnoreCase("Level")) {
                    editor.putString(KEY_THRESHOLD_LEVEL, threshold.toString());
                } else if (threshold.type.equalsIgnoreCase("ppm")) {
                    editor.putString(KEY_THRESHOLD_PPM, threshold.toString());
                }
            }
            editor.apply();
            ;
            return editor.commit(); // commit changes
        }
        return false;
    }

    public Threshold readLevelThreshold() {
        SharedPreferences pref = getSharedPreferences(); // 0 - for private mode
        return Threshold.GetThreshold(pref.getString(KEY_THRESHOLD_LEVEL, null)); // getting String
    }

    public Threshold readPpmThreshold() {
        SharedPreferences pref = getSharedPreferences(); // 0 - for private mode
        return Threshold.GetThreshold(pref.getString(KEY_THRESHOLD_PPM, null)); // getting String
    }
}
