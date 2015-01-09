package com.adrielservice.gia.callrecorder.ui;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.adrielservice.gia.callrecorder.R;

import java.io.IOException;

public class MediaPlayerActivity extends Activity implements OnCompletionListener, SeekBar.OnSeekBarChangeListener {

    private ImageButton btnPlay;
    private ImageButton btnForward;
    private ImageButton btnBackward;
    private ImageButton btnNext;
    private ImageButton btnPrevious;
    private ImageButton btnPlaylist;
    private ImageButton btnRepeat;
    private ImageButton btnShuffle;
    private SeekBar mediaProgressBar;
    private TextView mediaTitleLabel;
    private TextView mediaCurrentDurationLabel;
    private TextView mediaTotalDurationLabel;
    // Media Player
    private MediaPlayer mp;
    // Handler to update UI timer, progress bar etc,.
    private Handler mHandler = new Handler();

    private Utilities utils;
    private int seekForwardTime = 5000; // 5000 milliseconds
    private int seekBackwardTime = 5000; // 5000 milliseconds
    private boolean isShuffle = false;
    private boolean isRepeat = false;

    /**
     * Background Runnable thread
     */
    private Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            long totalDuration = mp.getDuration();
            long currentDuration = mp.getCurrentPosition();

            // Displaying Total Duration time
            mediaTotalDurationLabel.setText("" + utils.milliSecondsToTimer(totalDuration));
            // Displaying time completed playing
            mediaCurrentDurationLabel.setText("" + utils.milliSecondsToTimer(currentDuration));

            // Updating progress bar
            int progress = utils.getProgressPercentage(currentDuration, totalDuration);
            mediaProgressBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };

    private String mediaPath;
    private String mediaTitle;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.media_player);

        // All player buttons
        btnPlay = (ImageButton) findViewById(R.id.btnPlay);
        btnForward = (ImageButton) findViewById(R.id.btnForward);
        btnBackward = (ImageButton) findViewById(R.id.btnBackward);
        btnNext = (ImageButton) findViewById(R.id.btnNext);
        btnPrevious = (ImageButton) findViewById(R.id.btnPrevious);
        btnPlaylist = (ImageButton) findViewById(R.id.btnPlaylist);
        btnRepeat = (ImageButton) findViewById(R.id.btnRepeat);
        btnShuffle = (ImageButton) findViewById(R.id.btnShuffle);
        mediaProgressBar = (SeekBar) findViewById(R.id.songProgressBar);
        mediaTitleLabel = (TextView) findViewById(R.id.songTitle);
        mediaCurrentDurationLabel = (TextView) findViewById(R.id.songCurrentDurationLabel);
        mediaTotalDurationLabel = (TextView) findViewById(R.id.songTotalDurationLabel);

        // MediaPlayer
        mp = new MediaPlayer();
        utils = new Utilities();

        // Listeners
        mediaProgressBar.setOnSeekBarChangeListener(this); // Important
        mp.setOnCompletionListener(this); // Important

        Intent intent = getIntent();
        mediaPath = intent.getStringExtra("mediaPath");
        mediaTitle = intent.getStringExtra("mediaTitle");
        playMedia(mediaPath, mediaTitle);

        /**
         * Play button click event
         * plays a song and changes button to pause image
         * pauses a song and changes button to play image
         * */
        btnPlay.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check for already playing
                if (mp.isPlaying()) {
                    if (mp != null) {
                        mp.pause();
                        // Changing button image to play button
                        btnPlay.setImageResource(R.drawable.btn_play);
                    }
                } else {
                    // Resume song
                    if (mp != null) {
                        mp.start();
                        // Changing button image to pause button
                        btnPlay.setImageResource(R.drawable.btn_pause);
                    }
                }
            }
        });

        /**
         * Forward button click event
         * Forwards song specified seconds
         * */
        btnForward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            // get current song position
            int currentPosition = mp.getCurrentPosition();
            // check if seekForward time is lesser than song duration
            if (currentPosition + seekForwardTime <= mp.getDuration()) {
                // forward song
                mp.seekTo(currentPosition + seekForwardTime);
            } else {
                // forward to end position
                mp.seekTo(mp.getDuration());
            }
            }
        });

        /**
         * Backward button click event
         * Backward song to specified seconds
         * */
        btnBackward.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            // get current song position
            int currentPosition = mp.getCurrentPosition();
            // check if seekBackward time is greater than 0 sec
            if (currentPosition - seekBackwardTime >= 0) {
                // forward song
                mp.seekTo(currentPosition - seekBackwardTime);
            } else {
                // backward to starting position
                mp.seekTo(0);
            }
            }
        });

        /**
         * Next button click event
         * Plays next song by taking currentSongIndex + 1
         * */
        btnNext.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // check if next song is there or not
//                if (currentSongIndex < (songsList.size() - 1)) {
//                    playSong(currentSongIndex +  1);
//                    currentSongIndex = currentSongIndex + 1;
//                } else {
//                    // play first song
//                    playSong(0);
//                    currentSongIndex = 0;
//                }
            }
        });

        /**
         * Back button click event
         * Plays previous song by currentSongIndex - 1
         * */
        btnPrevious.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
//            if (currentSongIndex > 0) {
//                playSong(currentSongIndex - 1);
//                currentSongIndex = currentSongIndex - 1;
//            } else {
//                // play last song
//                playSong(songsList.size() - 1);
//                currentSongIndex = songsList.size() - 1;
//            }
            }
        });

        /**
         * Button Click event for Repeat button
         * Enables repeat flag to true
         * */
        btnRepeat.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            if (isRepeat) {
                isRepeat = false;
                Toast.makeText(getApplicationContext(), "Repeat is OFF", Toast.LENGTH_SHORT).show();
                btnRepeat.setImageResource(R.drawable.btn_repeat);
            } else {
                // make repeat to true
                isRepeat = true;
                Toast.makeText(getApplicationContext(), "Repeat is ON", Toast.LENGTH_SHORT).show();
                // make shuffle to false
                isShuffle = false;
                btnRepeat.setImageResource(R.drawable.btn_repeat_focused);
                btnShuffle.setImageResource(R.drawable.btn_shuffle);
            }
            }
        });

        /**
         * Button Click event for Shuffle button
         * Enables shuffle flag to true
         * */
        btnShuffle.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
            if (isShuffle) {
                isShuffle = false;
                Toast.makeText(getApplicationContext(), "Shuffle is OFF", Toast.LENGTH_SHORT).show();
                btnShuffle.setImageResource(R.drawable.btn_shuffle);
            } else {
                // make repeat to true
                isShuffle = true;
                Toast.makeText(getApplicationContext(), "Shuffle is ON", Toast.LENGTH_SHORT).show();
                // make shuffle to false
                isRepeat = false;
                btnShuffle.setImageResource(R.drawable.btn_shuffle_focused);
                btnRepeat.setImageResource(R.drawable.btn_repeat);
            }
            }
        });

        /**
         * Button Click event for Play list click event
         * Launches list activity which displays list of songs
         * */
//        btnPlaylist.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View arg0) {
//                Intent i = new Intent(getApplicationContext(), PlayListActivity.class);
//                startActivityForResult(i, 100);
//            }
//        });

    }

    /**
     * Receiving song index from playlist view
     * and play the song
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 100) {
            String mediaPath = data.getExtras().getString("mediaPath");
            String mediaTitle = data.getExtras().getString("mediaTitle");
            playMedia(mediaPath, mediaTitle);
        }
    }

    /**
     * Function to play a media
     *
     * @param mediaPath - media path
     * @param mediaTitle - media title
     */
    public void playMedia(String mediaPath, String mediaTitle) {
        // Play song
        try {
            mp.reset();
            mp.setDataSource(mediaPath);
            mp.prepare();
            mp.start();

            // Displaying Song title
            mediaTitleLabel.setText(mediaTitle);

            // Changing Button Image to pause image
            btnPlay.setImageResource(R.drawable.btn_pause);

            // set Progress bar values
            mediaProgressBar.setProgress(0);
            mediaProgressBar.setMax(100);

            // Updating progress bar
            updateProgressBar();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Update timer on seekbar
     */
    public void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    /**
     *
     * */
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {

    }

    /**
     * When user starts moving the progress handler
     */
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        // remove message Handler from updating progress bar
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mHandler.removeCallbacks(mUpdateTimeTask);
    }

    /**
     * When user stops moving the progress hanlder
     */
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        mHandler.removeCallbacks(mUpdateTimeTask);
        int totalDuration = mp.getDuration();
        int currentPosition = utils.progressToTimer(seekBar.getProgress(), totalDuration);

        // forward or backward to certain seconds
        mp.seekTo(currentPosition);

        // update timer progress again
        updateProgressBar();
    }

    /**
     * On Song Playing completed
     * if repeat is ON play same song again
     * if shuffle is ON play random song
     */
    @Override
    public void onCompletion(MediaPlayer arg0) {

        // check for repeat is ON or OFF
        if (isRepeat) {
            // repeat is on play same song again
            playMedia(mediaPath, mediaTitle);
        } else if (isShuffle) {
            // shuffle is on - play a random song
            // Random rand = new Random();
            // currentSongIndex = rand.nextInt((songsList.size() - 1) - 0 + 1) + 0;
            // playSong(currentSongIndex);
        } else {
            // no repeat or shuffle ON - play next song
//            if (currentSongIndex < (songsList.size() - 1)) {
//                playSong(currentSongIndex + 1);
//                currentSongIndex = currentSongIndex + 1;
//            } else {
//                // play first song
//                playSong(0);
//                currentSongIndex = 0;
//            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mp.release();
    }

    public class Utilities {

        /**
         * Function to convert milliseconds time to
         * Timer Format
         * Hours:Minutes:Seconds
         */
        public String milliSecondsToTimer(long milliseconds) {
            String finalTimerString = "";
            String secondsString;

            // Convert total duration into time
            int hours = (int) (milliseconds / (1000 * 60 * 60));
            int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
            int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
            // Add hours if there
            if (hours > 0) {
                finalTimerString = hours + ":";
            }

            // Prepending 0 to seconds if it is one digit
            if (seconds < 10) {
                secondsString = "0" + seconds;
            } else {
                secondsString = "" + seconds;
            }

            finalTimerString = finalTimerString + minutes + ":" + secondsString;

            // return timer string
            return finalTimerString;
        }

        /**
         * Function to get Progress percentage
         *
         * @param currentDuration
         * @param totalDuration
         */
        public int getProgressPercentage(long currentDuration, long totalDuration) {
            Double percentage = (double) 0;

            long currentSeconds = (int) (currentDuration / 1000);
            long totalSeconds = (int) (totalDuration / 1000);

            // calculating percentage
            percentage = (((double) currentSeconds) / totalSeconds) * 100;

            // return percentage
            return percentage.intValue();
        }

        /**
         * Function to change progress to timer
         *
         * @param progress      -
         * @param totalDuration returns current duration in milliseconds
         */
        public int progressToTimer(int progress, int totalDuration) {
            int currentDuration = 0;
            totalDuration = totalDuration / 1000;
            currentDuration = (int) ((((double) progress) / 100) * totalDuration);

            // return current duration in milliseconds
            return currentDuration * 1000;
        }
    }

}