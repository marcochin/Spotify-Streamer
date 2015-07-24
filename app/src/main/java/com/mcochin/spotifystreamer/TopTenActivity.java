/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;

import com.mcochin.spotifystreamer.fragments.TopTenFragment;
import com.mcochin.spotifystreamer.fragments.TrackPlayerFragment;
import com.mcochin.spotifystreamer.pojos.TopTenItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Class that displays the top ten tracks in a listView.
 */
public class TopTenActivity extends AppCompatActivity implements TopTenFragment.Callback{
    public static final String EXTRA_BUNDLE = "bundle";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_ten);

        if (savedInstanceState == null) {
            // Create the fragment and add it to the activity
            // using a fragment transaction.

            TopTenFragment fragment = new TopTenFragment();
            fragment.setArguments(getIntent().getBundleExtra(EXTRA_BUNDLE));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_ten_container, fragment)
                    .commit();
        }
    }

    //onTrackItemClick for singlePane
    @Override
    public void onTrackItemClick(List<TopTenItem> topTenItemList, int position, String artistName) {
        Bundle args = new Bundle();
        args.putParcelableArrayList(TrackPlayerFragment.EXTRA_TOP_TEN_ITEM_LIST,
                (ArrayList <? extends Parcelable>) topTenItemList);

        args.putInt(TrackPlayerFragment.EXTRA_ITEM_POSITION, position);
        args.putString(TrackPlayerFragment.EXTRA_ARTIST_NAME, artistName);

        Intent intent = new Intent(this, TrackPlayerActivity.class);
        intent.putExtra(TrackPlayerActivity.EXTRA_BUNDLE, args);

        startActivity(intent);
    }
}
