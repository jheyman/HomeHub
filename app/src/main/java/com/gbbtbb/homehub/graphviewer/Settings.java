package com.gbbtbb.homehub.graphviewer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class Settings {
    private SharedPreferences mPreferences;

    public static final String PREF_PREFIX = "com.gbbtbb.graphviewerwidget.";

    private Settings() {
    }

    public GraphSettings getGraphSettings() {
        String prefix = PREF_PREFIX;

        // If custom preferences for this widget instance do not exists yet, get
        // default values, this will initialize all parameters such that they
        // display properly in the Preference fragments
        if (!mPreferences.contains(prefix + "NumHours")) {
            GraphSettings gs = GraphSettings.get(mPreferences, PREF_PREFIX);
            gs.save(mPreferences, prefix);
        }

        return GraphSettings.get(mPreferences, PREF_PREFIX);
    }

    public static Settings get(Context context) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        Settings s = new Settings();
        s.mPreferences = pref;
        return s;
    }

    public static class GraphSettings {
        private String mPrefix;
        private int mNumHours;

        public int getHistoryLength() { return mNumHours; }

        public static GraphSettings get(SharedPreferences pref, String prefix) {
            GraphSettings gs = new GraphSettings();
            gs.mPrefix = prefix;
            gs.mNumHours = Integer.parseInt(pref.getString(prefix + "NumHours", Integer.toString(24)));
            Log.i("PREFERENCE", "NumHours CHANGED TO" + Integer.toString(gs.mNumHours));
            return gs;
        }

        public void save(Context context) {
            save(PreferenceManager.getDefaultSharedPreferences(context), mPrefix);
        }

        public void save(SharedPreferences pref, String prefix) {
            pref.edit()
                    .putString(prefix + "NumHours", Integer.toString(mNumHours))
                    .apply();
        }
    }
}