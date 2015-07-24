/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.SeekBar;
import android.widget.Toast;

import com.mcochin.spotifystreamer.R;
import com.mcochin.spotifystreamer.fragments.TrackPlayerFragment;
import com.mcochin.spotifystreamer.utilities.TimeConverter;

import java.io.IOException;

/**
 * Service that holds the MediaPlayer object and connects to the web to stream music
 */
public class TrackPlayerService extends Service {
    public static final String TAG = TrackPlayerService.class.getSimpleName();
    public static final String EXTRA_TRACK_PREVIEW_URL= "trackPreviewUrl";


    private final IBinder serviceBinder = new MyLocalBinder();
    private TrackPlayerFragment mTrackPlayerFragment;

    private MediaPlayer mMediaPlayer;
    private boolean mMediaPlayerPrepared;

    public class MyLocalBinder extends Binder{
        public TrackPlayerService getService(){
            return TrackPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        mMediaPlayer = new MediaPlayer();
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        setupMediaPlayer(intent.getStringExtra(EXTRA_TRACK_PREVIEW_URL));
        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        //Log.d(TAG, "Service onBind");
        return serviceBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        //Log.d(TAG, "Service onUnbind");
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //Log.d(TAG, "Service onDestroy");

        if (mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
        }
        mMediaPlayerPrepared = false;
    }

    /**
     * Sets up the MediaPlayer and starts playing on service creation
     */
    private void setupMediaPlayer(String mediaPath){
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {

                mMediaPlayerPrepared = true;
                showPauseButton();
                mMediaPlayer.start();

                //Updates current time by sleeping 1 sec
                startTimeUpdaterTask();

                //set the media file duration
                int seconds = TimeConverter.convertMilliToSec(mMediaPlayer.getDuration());
                updateEndTime(seconds);
            }
        });

        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                showPlayButton();
                resetTime();
            }
        });

        mMediaPlayer.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer mp, int percent) {
                if(percent == 100){
                    hideProgressWheel();
                    return;
                }

                if(mTrackPlayerFragment != null){
                    SeekBar seekBar = mTrackPlayerFragment.getSeekBar();
                    int songProgress = seekBar.getProgress()/seekBar.getMax();

                    if(percent > songProgress){
                        hideProgressWheel();
                    }else {
                        showProgressWheel();
                    }
                }
            }
        });

        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                switch(extra){
                    case MediaPlayer.MEDIA_ERROR_IO:
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        showServerErrorToast();
                        break;
                }

                if(what == MediaPlayer.MEDIA_ERROR_SERVER_DIED){
                    showServerErrorToast();
                }

                Log.e(TAG, "What: " + what + " Extra: " + extra);
                return false;
            }
        });

        setDataAndPrepareTrack(mediaPath);
    }

    public void setDataAndPrepareTrack(String mediaPath){
        showProgressWheel();
        try {
            mMediaPlayer.setDataSource(mediaPath);
            mMediaPlayer.prepareAsync();
        } catch(IOException | IllegalStateException | IllegalArgumentException | SecurityException e){
            Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
        }
    }

    private void showServerErrorToast(){
        hideProgressWheel();
        Toast.makeText(TrackPlayerService.this,
                R.string.toast_server_error, Toast.LENGTH_LONG).show();
    }

    private void showPlayButton(){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.showPlayButton();
        }
    }

    private void showPauseButton(){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.showPauseButton();
        }
    }

    private void showProgressWheel(){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.showProgessWheel();
        }
    }

    private void hideProgressWheel(){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.hideProgessWheel();
        }
    }

    private void resetTime(){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.resetTime();
        }
    }

    private void updateCurrentTime(int sec){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.updateCurrentTime(sec);
        }
    }

    private void updateEndTime(int sec){
        if(mTrackPlayerFragment != null){
            mTrackPlayerFragment.updateEndTime(sec);
        }
    }

    public void startTimeUpdaterTask(){
        new CurrentTimeUpdaterTask().execute();
    }

    public void setTrackPlayerFragment(TrackPlayerFragment trackPlayerFragment){
        mTrackPlayerFragment = trackPlayerFragment;
    }

    public void unsetTrackPlayerFragment(){
        mTrackPlayerFragment = null;
    }

    public MediaPlayer getMediaPlayer(){
        return mMediaPlayer;
    }

    public boolean isMediaPlayerPrepared(){
        return mMediaPlayerPrepared;
    }

    public void setMediaPlayerPrepared(boolean mediaPlayerPrepared){
        mMediaPlayerPrepared = mediaPlayerPrepared;
    }

    /**
     * A background task to update current position of the song
     */
    private class CurrentTimeUpdaterTask extends AsyncTask<Void, Integer, Void>{
        private int mSeconds;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            int currentPosition = mMediaPlayer.getCurrentPosition();
            mSeconds = TimeConverter.convertMilliToSec(currentPosition);
            publishProgress(mSeconds);
        }

        @Override
        protected Void doInBackground(Void... params) {
            try{
                while(mMediaPlayer != null && mMediaPlayer.isPlaying() ){
                    int currentPosition = mMediaPlayer.getCurrentPosition();

                    //If start Position is 2000, then it will increment if current position is >= 3000
                    // then at 3000 it will increment, if current position >= 4000, etc.
                    if(currentPosition >= ((mSeconds + 1) * TimeConverter.SECONDS)) {
                        mSeconds = TimeConverter.convertMilliToSec(currentPosition);
                        publishProgress(mSeconds);
                    }
                }
            }catch (IllegalStateException e){
                Log.e(TAG, e.getMessage() + "\n" + Log.getStackTraceString(e));
                return null;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... time) {
            super.onProgressUpdate(time);
            updateCurrentTime(time[0]);
        }
    }
}
