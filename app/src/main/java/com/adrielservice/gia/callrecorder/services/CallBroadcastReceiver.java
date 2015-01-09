package com.adrielservice.gia.callrecorder.services;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.telephony.TelephonyManager;
import android.util.Log;

import java.util.Date;

public class CallBroadcastReceiver extends BroadcastReceiver {

    private static int lastState = TelephonyManager.CALL_STATE_IDLE;

    //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
    private static Date callStartTime;
    private static boolean isIncoming;
    private static String savedNumber;  //because the passed incoming is only valid in ringing

    private static void startRecording(Context context, String phoneNumber, CallType type) {
        Intent callIntent = new Intent(context, RecordService.class);
        callIntent.putExtra("phoneNumber", phoneNumber);
        callIntent.putExtra("type", type);

        ComponentName name = context.startService(callIntent);
        if (null == name) {
            Log.e("CallRecorder", "startService for RecordService returned null ComponentName");
        } else {
            Log.i("CallRecorder", "startService returned " + name.flattenToString());
        }
    }

    private static void stopRecording(Context context, String phoneNumber, CallType type) {
        Intent callIntent = new Intent(context, RecordService.class);
        callIntent.putExtra("phoneNumber", phoneNumber);
        callIntent.putExtra("type", type);

        Boolean stopped = context.stopService(new Intent(context, RecordService.class));
        Log.i("PhoneListener", "stopService for RecordService returned " + stopped);
    }

    //Deals with actual events

    @Override
    public void onReceive(Context context, Intent intent) {

        //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
        if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
        } else {
            String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
            String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);
            int state = 0;
            if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                state = TelephonyManager.CALL_STATE_IDLE;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                state = TelephonyManager.CALL_STATE_OFFHOOK;
            } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                state = TelephonyManager.CALL_STATE_RINGING;
            }

            onCallStateChanged(context, state, number);
        }
    }

    //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
    //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
    public void onCallStateChanged(Context context, int state, String number) {
        if (lastState == state) {
            //No change, debounce extras
            return;
        }
        switch (state) {
            case TelephonyManager.CALL_STATE_RINGING:
                isIncoming = true;
                callStartTime = new Date();
                savedNumber = number;
                onIncomingCallStarted(context, number, callStartTime);
                break;
            case TelephonyManager.CALL_STATE_OFFHOOK:
                //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                    isIncoming = false;
                    callStartTime = new Date();
                    onOutgoingCallStarted(context, savedNumber, callStartTime);
                }
                break;
            case TelephonyManager.CALL_STATE_IDLE:
                //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                    //Ring but no pickup-  a miss
                    onMissedCall(context, savedNumber, callStartTime);
                } else if (isIncoming) {
                    onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                } else {
                    onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                }
                break;
        }
        lastState = state;
    }

    //Derived classes should override these to respond to specific events of interest
    private void onIncomingCallStarted(Context ctx, String number, Date start) {
        startRecording(ctx, number, CallType.CALL_INCOMING);
    }

    private void onOutgoingCallStarted(Context ctx, String number, Date start) {
        startRecording(ctx, number, CallType.CALL_OUTGOING);
    }

    private void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        stopRecording(ctx, number, CallType.CALL_OUTGOING);
    }

    private void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        stopRecording(ctx, number, CallType.CALL_OUTGOING);
    }

    private void onMissedCall(Context ctx, String number, Date start) {
        // TODO store in the database
    }

    public static enum CallType {
        CALL_INCOMING,
        CALL_OUTGOING,
        CALL_MISSED
    }

}
