package com.adrielservice.gia.callrecorder.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.Toast;

import com.adrielservice.gia.callrecorder.R;

import java.io.File;
import java.io.IOException;

public class RecordService extends Service implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener {

    public static final String DEFAULT_STORAGE_LOCATION = Environment.getExternalStorageDirectory().getPath() + "/gia/callrecorder";
    private static final String TAG = "CallRecorder";
    private static final String PREF_RECORD_CALLS = "PREF_RECORD_CALLS";
    private static final String PREF_AUDIO_SOURCE = "PREF_AUDIO_SOURCE";
    private static final String PREF_AUDIO_FORMAT = "PREF_AUDIO_FORMAT";
    private static final int RECORDING_NOTIFICATION_ID = 1;

    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private File recording = null;

    private String phoneNumber = "unknown";

	/*
     * private static void test() throws java.security.NoSuchAlgorithmException
	 * { KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
	 * kpg.initialize(2048); KeyPair kp = kpg.genKeyPair(); Key publicKey =
	 * kp.getPublic(); Key privateKey = kp.getPrivate(); }
	 */

    private File makeOutputFile(String phoneNumber, SharedPreferences prefs) {
        File dir = new File(DEFAULT_STORAGE_LOCATION);

        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                Log.e("CallRecorder", "RecordService::makeOutputFile unable to create directory " + dir + ": " + e);
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create the directory "
                        + dir + " to store recordings: " + e, Toast.LENGTH_LONG);
                t.show();
                return null;
            }
        } else {
            if (!dir.canWrite()) {
                Log.e(TAG, "RecordService::makeOutputFile does not have write permission for directory: " + dir);
                Toast t = Toast.makeText(getApplicationContext(),
                        "CallRecorder does not have write permission for the directory directory " + dir
                                + " to store recordings", Toast.LENGTH_LONG);
                t.show();
                return null;
            }
        }

        // create filename based on call data
        String prefix = "call";
        prefix += "-" + phoneNumber;
        String date = (DateFormat.format("dd-MM-yyyy hh:mm:ss", new java.util.Date()).toString());
        prefix += "-" + date + "-rec";

        // add info to file name about what audio channel we were recording
        int audioSource = Integer.parseInt(prefs.getString(PREF_AUDIO_SOURCE, "1"));
        prefix += "-channel" + audioSource + "-";

        // create suffix based on format
        String suffix = "";
        int audioFormat = Integer.parseInt(prefs.getString(PREF_AUDIO_FORMAT, "1"));
        switch (audioFormat) {
            case MediaRecorder.OutputFormat.THREE_GPP:
                suffix = ".3gpp";
                break;
            case MediaRecorder.OutputFormat.MPEG_4:
                suffix = ".mp3";
                break;
            case MediaRecorder.OutputFormat.AMR_WB:
                suffix = ".amr";
                break;
        }

        try {
            return File.createTempFile(prefix, suffix, dir);
        } catch (IOException e) {
            Log.e("CallRecorder", "RecordService::makeOutputFile unable to create temp file in " + dir + ": " + e);
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to create temp file in " + dir
                    + ": " + e, Toast.LENGTH_LONG);
            t.show();
            return null;
        }
    }

    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
        Log.i("CallRecorder", "onCreate created MediaRecorder object");
    }

    public void onStart(Intent intent, int startId) {

        phoneNumber = getPhoneNumber(intent);

        Log.i("CallRecorder", "RecordService::onStartCommand called while isRecording:" + isRecording);

        if (isRecording)
            return;

        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        Boolean shouldRecord = prefs.getBoolean(PREF_RECORD_CALLS, true);
        if (!shouldRecord) {
            Log.i("CallRecord", "RecordService::onStartCommand with PREF_RECORD_CALLS false, not recording");
            // return START_STICKY;
            return;
        }

        int audioSource = Integer.parseInt(prefs.getString(PREF_AUDIO_SOURCE, "1"));
        int audioFormat = Integer.parseInt(prefs.getString(PREF_AUDIO_FORMAT, "1"));

        recording = makeOutputFile(phoneNumber, prefs);
        if (recording == null) {
            recorder = null;
            return; // return 0;
        }

        Log.i("CallRecorder", "RecordService will config MediaRecorder with audioSource: " + audioSource
                + " audioFormat: " + audioFormat);
        try {
            // These calls will throw exceptions unless you set the
            // android.permission.RECORD_AUDIO permission for your app
            recorder.reset();
            recorder.setAudioSource(audioSource);
            Log.d("CallRecorder", "set audioSource " + audioSource);
            recorder.setOutputFormat(audioFormat);
            Log.d("CallRecorder", "set output " + audioFormat);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            Log.d("CallRecorder", "set encoder default");
            recorder.setOutputFile(recording.getAbsolutePath());
            Log.d("CallRecorder", "set file: " + recording);
            // recorder.setMaxDuration(msDuration); //1000); // 1 seconds
            // recorder.setMaxFileSize(bytesMax); //1024*1024); // 1KB

            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);

            try {
                recorder.prepare();
            } catch (java.io.IOException e) {
                Log.e("CallRecorder", "RecordService::onStart() IOException attempting recorder.prepare()\n");
                Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e,
                        Toast.LENGTH_LONG);
                t.show();
                recorder = null;
                phoneNumber = "unknown";
                return; // return 0; //START_STICKY;
            }
            Log.d("CallRecorder", "recorder.prepare() returned");

            recorder.start();
            isRecording = true;
            Log.i("CallRecorder", "recorder.start() returned");
            updateNotification(true, phoneNumber);
        } catch (java.lang.Exception e) {
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder was unable to start recording: " + e,
                    Toast.LENGTH_LONG);
            t.show();

            Log.e("CallRecorder", "RecordService::onStart caught unexpected exception", e);
            recorder = null;
            phoneNumber = "unknown";
        }

        return; // return 0; //return START_STICKY;
    }

    private String getPhoneNumber(Intent intent) {
        String phoneNumber = "Unknown";

        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras == null) {
                Log.d("RecordService", "No phone number is specified");
            } else {
                phoneNumber = (String) extras.get("phoneNumber");
                Log.d("RecordService", "Phone Number " + phoneNumber);
            }
        }

        return phoneNumber;
    }

    public void onDestroy() {
        super.onDestroy();

        if (null != recorder) {
            Log.i("CallRecorder", "RecordService::onDestroy calling recorder.release()");
            isRecording = false;
            recorder.release();
            Toast t = Toast.makeText(getApplicationContext(), "CallRecorder finished recording call to " + recording,
                    Toast.LENGTH_LONG);
            t.show();

			/*
			 * // encrypt the recording String keyfile = "/sdcard/keyring"; try
			 * { //PGPPublicKey k = readPublicKey(new FileInputStream(keyfile));
			 * test(); } catch (java.security.NoSuchAlgorithmException e) {
			 * Log.e("CallRecorder",
			 * "RecordService::onDestroy crypto test failed: ", e); }
			 * //encrypt(recording);
			 */
        }

        updateNotification(false, phoneNumber);
    }

    // methods to handle binding the service

    public IBinder onBind(Intent intent) {
        return null;
    }

    public boolean onUnbind(Intent intent) {
        return false;
    }

    public void onRebind(Intent intent) {
    }

    @SuppressWarnings("deprecation")
    private void updateNotification(Boolean status, String phoneNumber) {
        Context c = getApplicationContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(c);

        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);

        if (status) {
            int icon = R.drawable.icon;
            CharSequence tickerText = "Recording call " + phoneNumber;
            long when = System.currentTimeMillis();

            Notification notification = new Notification(icon, tickerText, when);

            Context context = getApplicationContext();
            CharSequence contentTitle = "CallRecorder Status";
            CharSequence contentText = "Recording call from channel...";
            Intent notificationIntent = new Intent(this, RecordService.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
            mNotificationManager.notify(RECORDING_NOTIFICATION_ID, notification);
        } else {
            mNotificationManager.cancel(RECORDING_NOTIFICATION_ID);
        }
    }

    // MediaRecorder.OnInfoListener
    public void onInfo(MediaRecorder mr, int what, int extra) {
        Log.i("CallRecorder", "RecordService got MediaRecorder onInfo callback with what: " + what + " extra: " + extra);
        isRecording = false;
    }

    // MediaRecorder.OnErrorListener
    public void onError(MediaRecorder mr, int what, int extra) {
        Log.e("CallRecorder", "RecordService got MediaRecorder onError callback with what: " + what + " extra: "
                + extra);
        isRecording = false;
        mr.release();
    }
}
