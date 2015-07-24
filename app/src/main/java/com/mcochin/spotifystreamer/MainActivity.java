/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer;

import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;

import com.mcochin.spotifystreamer.fragments.SearchFragment;
import com.mcochin.spotifystreamer.fragments.TopTenFragment;
import com.mcochin.spotifystreamer.fragments.TrackPlayerFragment;
import com.mcochin.spotifystreamer.pojos.TopTenItem;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements SearchFragment.Callback, TopTenFragment.Callback{
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(findViewById(R.id.top_ten_container) != null){
            mTwoPane = true;
        } else{
            mTwoPane = false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSearchItemClick(String artistName, String spotifyId) {
        Bundle args = new Bundle();
        args.putString(TopTenFragment.EXTRA_ARTIST_NAME, artistName);
        args.putString(TopTenFragment.EXTRA_SPOTIFY_ID, spotifyId);

        if(mTwoPane){
            //if the top ten tracks of that artist is already loaded, don't load again.
            if(getSupportActionBar() != null){
                String subtitle = (String)getSupportActionBar().getSubtitle();
                if(subtitle != null && subtitle.equals(artistName)){
                    return;
                }
            }

            TopTenFragment topTenFragment = new TopTenFragment();
            topTenFragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_ten_container, topTenFragment, TopTenFragment.TAG)
                    .commit();

        }else{
            Intent intent = new Intent(this, TopTenActivity.class);
            intent.putExtra(TopTenActivity.EXTRA_BUNDLE, args);
            startActivity(intent);
        }
    }

    //onTrackItemClick for twoPane
    @Override
    public void onTrackItemClick(List<TopTenItem> topTenItemList, int position, String artistName){
        Bundle args = new Bundle();
        args.putParcelableArrayList(TrackPlayerFragment.EXTRA_TOP_TEN_ITEM_LIST,
                (ArrayList<? extends Parcelable>) topTenItemList);

        args.putInt(TrackPlayerFragment.EXTRA_ITEM_POSITION, position);
        args.putString(TrackPlayerFragment.EXTRA_ARTIST_NAME, artistName);

        TrackPlayerFragment trackPlayerFragment = new TrackPlayerFragment();
        trackPlayerFragment.setArguments(args);

        trackPlayerFragment.show(getSupportFragmentManager(), TrackPlayerFragment.TAG);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if(ev.getAction() == MotionEvent.ACTION_DOWN){
            //Calculations to detect if keyboard is present or not
            Rect rect = new Rect();
            View decorView = getWindow().getDecorView();
            decorView.getWindowVisibleDisplayFrame(rect);
            int displayHeight = rect.bottom - rect.top;
            int height = decorView.getHeight();

            //If the displayHeight to actualHeight ratio is less than 0.8, hide search keyboard
            if((float)displayHeight / height < 0.8) {
                ((SearchFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_search))
                        .hideKeyboard();
                return true;
            }
        }
        return super.dispatchTouchEvent(ev);
    }
}
