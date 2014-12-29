package com.adrielservice.gia.callrecorder.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

/**
 * Created by dbeilis on 12/14/14.
 */
public class SettingsFragment extends Fragment {

    static public final String PREF_RECORD_CALLS = "PREF_RECORD_CALLS";
    static public final String PREF_AUDIO_SOURCE = "PREF_AUDIO_SOURCE";
    static public final String PREF_AUDIO_FORMAT = "PREF_AUDIO_FORMAT";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // addPreferencesFromResource(R.xml.userpreferences);
    }

}

