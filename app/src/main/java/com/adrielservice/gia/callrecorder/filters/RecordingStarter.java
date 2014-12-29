package com.adrielservice.gia.callrecorder.filters;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.adrielservice.gia.callrecorder.services.RecordService;

/**
 * Created by dbeilis on 12/24/14.
 */
public class RecordingStarter {

    public static void startRecording(Context context, String phoneNumber) {
        Intent callIntent = new Intent(context, RecordService.class);
        if (phoneNumber.equalsIgnoreCase("")) {
            phoneNumber = "unkonwn";
        }
        callIntent.putExtra("phoneNumber", phoneNumber);

        ComponentName name = context.startService(callIntent);
        if (null == name) {
            Log.e("CallRecorder", "startService for RecordService returned null ComponentName");
        } else {
            Log.i("CallRecorder", "startService returned " + name.flattenToString());
        }
    }
}
