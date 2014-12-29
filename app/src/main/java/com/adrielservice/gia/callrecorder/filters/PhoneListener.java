package com.adrielservice.gia.callrecorder.filters;

import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.adrielservice.gia.callrecorder.services.RecordService;

public class PhoneListener extends PhoneStateListener
{
    private Context context;

    private String phoneNumber = "unknown";

    public PhoneListener(Context c) {
        Log.i("PhoneListener", "PhoneListener constructor");
        context = c;
    }

    @Override
    public void onCallStateChanged (int state, String incomingNumber) {
        super.onCallStateChanged(state, incomingNumber);

        Log.d("CallRecorder", "PhoneListener::onCallStateChanged state:" + state + " incomingNumber:" + incomingNumber);

        switch (state) {
        case TelephonyManager.CALL_STATE_IDLE:
            Log.d("PhoneListener", "CALL_STATE_IDLE, stopping recording");
            Boolean stopped = context.stopService(new Intent(context, RecordService.class));
            Log.i("PhoneListener", "stopService for RecordService returned " + stopped);
            break;
        case TelephonyManager.CALL_STATE_RINGING:
            Log.d("PhoneListener", "CALL_STATE_RINGING");
            break;
        case TelephonyManager.CALL_STATE_OFFHOOK:
            Log.d("PhoneListener", "CALL_STATE_OFFHOOK starting recording");
            RecordingStarter.startRecording(context, phoneNumber);
            break;
        }
    }
}
