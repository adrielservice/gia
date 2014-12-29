package com.adrielservice.gia.callrecorder.filters;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by dbeilis on 12/24/14.
 */
public class OutgoingCallInterceptor extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String outgoingPhoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);

        Log.d("OutgoingCallInterceptor", "CALL_STATE_OFFHOOK starting recording");
        RecordingStarter.startRecording(context, outgoingPhoneNumber);
    }

}