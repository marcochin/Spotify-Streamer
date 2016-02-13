package com.mcochin.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;

import com.mcochin.spotifystreamer.fragments.TopTenFragment;
import com.mcochin.spotifystreamer.fragments.TrackPlayerFragment;
import com.mcochin.spotifystreamer.services.TrackPlayerService;

/**
 * Created by Marco on 7/14/2015.
 */
public class TrackPlayerActivity extends AppCompatActivity {
    public static final String EXTRA_BUNDLE = "bundle";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track_player);

        if (savedInstanceState == null) {
            // Create the fragment and add it to the activity
            // using a fragment transaction.

            TrackPlayerFragment fragment = new TrackPlayerFragment();
            fragment.setArguments(getIntent().getBundleExtra(EXTRA_BUNDLE));

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.track_player_container, fragment)
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                stopService(new Intent(this, TrackPlayerService.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        stopService(new Intent(this, TrackPlayerService.class));
        super.onBackPressed();
    }
}
