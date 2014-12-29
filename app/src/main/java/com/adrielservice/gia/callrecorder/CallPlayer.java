package com.adrielservice.gia.callrecorder;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.Toast;

/*
  This is brain dead at the moment.  At a minimum I need to have the 
  MediaPlayer playing on a thread.

  Right now if you are playing a clip and leave the CallPlayer activity it 
  faults.

  Ideally I would just farm all this out to a pre-existing media player 
  activity, but the default one in the SDK 1.6 emulator doesn't know how
  to play 3gpp files.

  If I'm going to have to go to all the bother of creating a little media
  player to play back recordings should at a minimum have the seekbar to scrub
  around and allow pausing, etc..
*/

public class CallPlayer extends Activity
    implements MediaPlayer.OnPreparedListener, MediaPlayer.OnInfoListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = "CallRecorder";
    private AudioPlayerControl aPlayer = null;
    private MediaController controller = null;
    private ViewGroup anchor = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.player);

        anchor = (ViewGroup)findViewById(R.id.playerlayout);

        if (aPlayer != null) {
            Log.i(TAG, "CallPlayer onCreate called with aPlayer already allocated, destroying old one.");
            aPlayer.destroy();
            aPlayer = null;
        }
        if (controller != null) {
            Log.i(TAG, "CallPlayer onCreate called with controller already allocated, destroying old one.");
            controller = null;
        }

        Intent i = getIntent();
        String path = i.getData().getEncodedPath();

        Log.i(TAG, "CallPlayer onCreate with data: " + path);
        try {
            aPlayer = new AudioPlayerControl(path, this);

            // creating the controller here fails.  Have to do it once our onCreate has finished?
            // do it in the onPrepared listener for the actual MediaPlayer
        } catch (java.io.IOException e) {
            Log.e(TAG, "CallPlayer onCreate failed while creating AudioPlayerControl", e);
            Toast t = Toast.makeText(this, "CallPlayer received error attempting to create AudioPlayerControl: " + e, Toast.LENGTH_LONG);
            t.show();
            finish();
        }
    }

    public void onDestroy() {
        Log.i(TAG, "CallPlayer onDestroy");
        if (aPlayer != null) {
            aPlayer.destroy();
            aPlayer = null;
        }
        super.onDestroy();
    }

    // MediaPlayer.OnPreparedListener
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "CallPlayer onPrepared about to construct MediaController object");
        controller = new MediaController(this, true); // enable fast forward
        //controller = new MyMediaController(this, true); // enable fast forward
        //controller = new MediaController(getApplicationContext()); // why is using 'this' different than 'getApplicationContext()' ?

        controller.setMediaPlayer(aPlayer);
        controller.setAnchorView(anchor);
        controller.setEnabled(true);
        controller.show(0); //aPlayer.getDuration());
        
        // controller disappears after 3 seconds no matter what... set timer to handle re-showing it?
    }

    public boolean onInfo(MediaPlayer mp, int what, int extra) {
        Log.i(TAG, "CallPlayer onInfo with what " + what + " extra " + extra);
        return false;
    }

    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "CallPlayer onError with what " + what + " extra " + extra);
        Toast t = Toast.makeText(this, "CallPlayer received error (what:" + what + " extra:" + extra + ") from MediaPlayer attempting to play ", Toast.LENGTH_LONG);
        t.show();
        finish();
        return true;
    }

    public void onCompletion(MediaPlayer mp) {
        Log.i(TAG, "CallPlayer onCompletion, finishing activity");
        finish();
    }
}
