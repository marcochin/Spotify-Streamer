/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.fragments;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import com.mcochin.spotifystreamer.R;
import com.mcochin.spotifystreamer.pojos.TopTenItem;
import com.mcochin.spotifystreamer.services.TrackPlayerService;
import com.mcochin.spotifystreamer.utilities.ProgressVisibler;
import com.mcochin.spotifystreamer.utilities.TimeConverter;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * This fragment contains a media player that streams music
 */
public class TrackPlayerFragment extends DialogFragment implements View.OnClickListener, ServiceConnection, SeekBar.OnSeekBarChangeListener {
    public static final String TAG = TrackPlayerFragment.class.getSimpleName();

    public static final String EXTRA_TOP_TEN_ITEM_LIST = "topTenItemList";
    public static final String EXTRA_ITEM_POSITION = "position";
    public static final String EXTRA_ARTIST_NAME = "artistName";

    public static final String SAVE_CURRENT_TIME = "currentTime";
    public static final String SAVE_END_TIME = "endTime";
    public static final String SAVE_CURRENT_TIME_TEXT_VIEW = "currentTimeTextView";
    public static final String SAVE_END_TIME_TEXT_VIEW = "endTimeTextView";
    public static final String SAVE_CURRENT_TRACK_POSITION = "currentTrackPosition";
    public static final String SAVE_IS_PLAYING = "isPlaying";

    private List<TopTenItem> mTopTenItemList;
    private int mCurrentTrackPosition;

    private ImageButton mPlayPauseButton;
    private TextView mEndTimeTextView;
    private TextView mCurrentTimeTextView;
    private TextView mTrackNameTextView;
    private TextView mAlbumNameTextView;
    private ImageView mAlbumThumbnailImageView;

    private SeekBar mSeekBar;
    private ProgressBar mProgressWheel;

    private MediaPlayer mMediaPlayer;
    private TrackPlayerService mTrackPlayerService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @NonNull
    @Override // This is called only when this fragment is shown as a dialog
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        // Request a window without the title
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_track_player, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mTopTenItemList = getArguments().getParcelableArrayList(TrackPlayerFragment.EXTRA_TOP_TEN_ITEM_LIST);
        if (savedInstanceState == null) {
            mCurrentTrackPosition = getArguments().getInt(EXTRA_ITEM_POSITION);
        } else {
            mCurrentTrackPosition = savedInstanceState.getInt(SAVE_CURRENT_TRACK_POSITION);
        }

        TopTenItem currentTrackItem = mTopTenItemList.get(mCurrentTrackPosition);

        String artistName = getArguments().getString(EXTRA_ARTIST_NAME);
        String albumName = currentTrackItem.getAlbumName();
        String trackName = currentTrackItem.getTrackName();
        String thumbnailLarge = currentTrackItem.getImageThumbnailLarge();

        ((TextView) v.findViewById(R.id.artist_text_view)).setText(artistName);

        mTrackNameTextView = (TextView) v.findViewById(R.id.track_text_view);
        mTrackNameTextView.setText(trackName);

        mAlbumNameTextView = (TextView) v.findViewById(R.id.album_text_view);
        mAlbumNameTextView.setText(albumName);

        mCurrentTimeTextView = (TextView) v.findViewById(R.id.seekbar_current_time_text_view);
        mEndTimeTextView = (TextView) v.findViewById(R.id.seekbar_end_time_text_view);

        mAlbumThumbnailImageView = (ImageView) v.findViewById(R.id.thumbnail_image_view);
        Picasso.with(getActivity()).load(thumbnailLarge)
                .error(R.drawable.no_image_640px)
                .into(mAlbumThumbnailImageView);

        mSeekBar = (SeekBar) v.findViewById(R.id.seekbar);
        mSeekBar.setOnSeekBarChangeListener(this);

        mPlayPauseButton = (ImageButton) v.findViewById(R.id.media_play_pause_button);
        mPlayPauseButton.setOnClickListener(this);

        mProgressWheel = (ProgressBar) v.findViewById(R.id.progress_wheel);

        v.findViewById(R.id.media_previous_track_button).setOnClickListener(this);
        v.findViewById(R.id.media_next_track_button).setOnClickListener(this);

        if (savedInstanceState == null) {
            showProgressWheel();
            startTrackPlayerService(mCurrentTrackPosition);

        } else {
            mSeekBar.setProgress(savedInstanceState.getInt(SAVE_CURRENT_TIME));
            mSeekBar.setMax(savedInstanceState.getInt(SAVE_END_TIME));
            mCurrentTimeTextView.setText(savedInstanceState.getString(SAVE_CURRENT_TIME_TEXT_VIEW));
            mEndTimeTextView.setText(savedInstanceState.getString(SAVE_END_TIME_TEXT_VIEW));

            if (savedInstanceState.getBoolean(SAVE_IS_PLAYING)) {
                showPauseButton();
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //save the time so there are no ui inconsistencies on rotation
        outState.putInt(SAVE_CURRENT_TIME, mSeekBar.getProgress());
        outState.putInt(SAVE_END_TIME, mSeekBar.getMax());
        outState.putString(SAVE_CURRENT_TIME_TEXT_VIEW, mCurrentTimeTextView.getText().toString());
        outState.putString(SAVE_END_TIME_TEXT_VIEW, mEndTimeTextView.getText().toString());
        outState.putInt(SAVE_CURRENT_TRACK_POSITION, mCurrentTrackPosition);

        //If music is playing show the pause button on rotation
        outState.putBoolean(SAVE_IS_PLAYING, mMediaPlayer.isPlaying());
    }

    @Override
    public void onResume() {
        super.onResume();

        //bind service on resume
        getActivity().bindService(
                new Intent(getActivity(), TrackPlayerService.class),
                this,
                Context.BIND_AUTO_CREATE);

        //for tablets (sizing the dialog)
        if (getDialog() != null) {
            int screenWidth = (int) getResources().getDimension(R.dimen.track_player_dialog_width);
            int screenHeight = (int) getResources().getDimension(R.dimen.track_player_dialog_height);
            getDialog().getWindow().setLayout(screenWidth, screenHeight);
        }
    }

    @Override
    public void onStop() {
        super.onStop();

        //cleanup and unbind service
        mTrackPlayerService.unsetTrackPlayerFragment();
        getActivity().unbindService(this);
    }

    @Override // This is called only when this fragment is shown as a dialog
    public void onDismiss(DialogInterface dialog) {
        getActivity().stopService(new Intent(getActivity(), TrackPlayerService.class));
        super.onDismiss(dialog);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.media_play_pause_button:
                if (mMediaPlayer != null) {
                    if (mMediaPlayer.isPlaying()) {
                        showPlayButton();
                        mMediaPlayer.pause();

                    } else if (mTrackPlayerService.isMediaPlayerPrepared()) {
                        showPauseButton();
                        mMediaPlayer.start();
                        mTrackPlayerService.startTimeUpdaterTask();
                    }

                } else {
                    loadTrack(mCurrentTrackPosition);
                }
                break;

            case R.id.media_previous_track_button:
                if (mCurrentTrackPosition == 0) {
                    mCurrentTrackPosition = mTopTenItemList.size() - 1;
                } else {
                    mCurrentTrackPosition--;
                }

                loadTrack(mCurrentTrackPosition);
                break;

            case R.id.media_next_track_button:
                if (mCurrentTrackPosition == mTopTenItemList.size() - 1) {
                    mCurrentTrackPosition = 0;
                } else {
                    mCurrentTrackPosition++;
                }

                loadTrack(mCurrentTrackPosition);
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        //Update endTimeTextView to display end time of the song
        if (mTrackPlayerService != null && mTrackPlayerService.isMediaPlayerPrepared()) {
            Pair<Integer, Integer> minAndSecPair = TimeConverter.convertSecToMinAndSec(progress);
            mCurrentTimeTextView.setText(String.format("%d:%s",
                    minAndSecPair.first,
                    String.format("%02d", minAndSecPair.second)));
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        if (mTrackPlayerService != null && mTrackPlayerService.isMediaPlayerPrepared()) {
            mMediaPlayer.pause();
            showPlayButton();
        }
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        //Seek to song position on stop touch
        if (mTrackPlayerService != null && mTrackPlayerService.isMediaPlayerPrepared()) {
            int currentSeekBarPosition = TimeConverter.convertSecToMilli(mSeekBar.getProgress());
            mMediaPlayer.seekTo(currentSeekBarPosition);
        }
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {
        mTrackPlayerService = ((TrackPlayerService.MyLocalBinder) service).getService();
        mTrackPlayerService.setTrackPlayerFragment(this);
        mMediaPlayer = mTrackPlayerService.getMediaPlayer();

        // Update time after connected just in case song finishes while onStop
        updateCurrentTime(TimeConverter.convertMilliToSec(mMediaPlayer.getCurrentPosition()));
        if (!mMediaPlayer.isPlaying()) {
            showPlayButton();
        }

        //Log.d(TAG, "service connected");
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {
        // This is called when the connection with the service has been unexpectedly disconnected - process crashed.
        //Log.d(TAG, "service disconnected");
    }

    /**
     * Loads a specified track in the top ten list
     *
     * @param trackPosition The item position in the top ten list to be loaded
     */
    private void loadTrack(int trackPosition) {
        if (mMediaPlayer != null) {
            TopTenItem currentTrackItem = mTopTenItemList.get(trackPosition);
            String trackName = currentTrackItem.getTrackName();
            String albumName = currentTrackItem.getAlbumName();
            String thumbnailLarge = currentTrackItem.getImageThumbnailLarge();
            String trackPreviewUrl = currentTrackItem.getTrackPreviewUrl();

            mTrackNameTextView.setText(trackName);
            mAlbumNameTextView.setText(albumName);

            Picasso.with(getActivity()).load(thumbnailLarge)
                    .error(R.drawable.no_image_640px)
                    .into(mAlbumThumbnailImageView);

            resetTime();
            showPlayButton();
            mTrackPlayerService.setDataAndPrepareTrack(trackPreviewUrl);

        } else {
            startTrackPlayerService(trackPosition);
        }
    }

    /**
     * Starts the TrackPlayerService class and automatically plays the track
     */
    private void startTrackPlayerService(int trackPosition) {
        TopTenItem currentTrackItem = mTopTenItemList.get(trackPosition);

        Intent intent = new Intent(getActivity(), TrackPlayerService.class);
        intent.putExtra(TrackPlayerService.EXTRA_TRACK_PREVIEW_URL, currentTrackItem.getTrackPreviewUrl());
        getActivity().startService(intent);
    }

    /**
     * Set the duration of the song
     *
     * @param sec the duration of the song in seconds
     */
    public void updateEndTime(int sec) {
        //Configure seekBar max number
        mSeekBar.setMax(sec);

        //Update endTimeTextView to display end time of the song
        Pair<Integer, Integer> minAndSecPair = TimeConverter.convertSecToMinAndSec(sec);
        mEndTimeTextView.setText(String.format("%d:%s",
                minAndSecPair.first,
                String.format("%02d", minAndSecPair.second)));
    }

    /**
     * Set the current progess of the song
     *
     * @param sec the current progress of the song in seconds
     */
    public void updateCurrentTime(int sec) {
        //Update seekBar progress. When seekBar updates, so will currentTimeTextView
        mSeekBar.setProgress(sec);
    }

    /**
     * Resets time of the song to 00:00
     */
    public void resetTime() {
        mSeekBar.setProgress(0);
    }

    public void showPlayButton() {
        mPlayPauseButton.setImageResource(R.drawable.ic_media_play);
    }

    public void showPauseButton() {
        mPlayPauseButton.setImageResource(R.drawable.ic_media_pause);
    }

    public void showProgressWheel() {
        ProgressVisibler.showProgressWheel(mProgressWheel);
    }

    public void hideProgressWheel() {
        ProgressVisibler.hideProgressWheel(mProgressWheel);
    }

    public SeekBar getSeekBar() {
        return mSeekBar;
    }
}
