/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.fragments;

import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.mcochin.spotifystreamer.R;
import com.mcochin.spotifystreamer.adapters.TopTenListAdapter;
import com.mcochin.spotifystreamer.pojos.TopTenItem;
import com.mcochin.spotifystreamer.utilities.ProgressVisibler;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The fragment that displays the top ten tracks of an artist
 */
public class TopTenFragment extends Fragment implements AdapterView.OnItemClickListener {
    public static final String TAG = TopTenFragment.class.getSimpleName();
    public static final String EXTRA_ARTIST_NAME = "artistName";
    public static final String EXTRA_SPOTIFY_ID = "spotifyId";
    private static final String SAVE_TOP_TEN_LIST = "topTenList";
    private static final String IMAGE_ERROR = "imageError";

    private ProgressBar mProgressWheel;
    private List<TopTenItem> mTopTenItemList;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback{
        /**
         * TopTenFragmentCallback for when an item has been selected.
         */
        void onTrackItemClick(List<TopTenItem> topTenItemList, int position, String artistName);
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_top_ten, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        //set subtitle to the artist name
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if(actionBar != null){
            actionBar.setSubtitle(getArguments().getString(EXTRA_ARTIST_NAME));
        }

        mProgressWheel = (ProgressBar)v.findViewById(R.id.progress_wheel);
        ListView topTenListView = (ListView)v.findViewById(R.id.top_ten_list_view);
        topTenListView.setOnItemClickListener(this);

        //save top ten list on rotation
        if(savedInstanceState == null){
            mTopTenItemList = new ArrayList<>();
            loadTopTen(topTenListView);
        } else {
            mTopTenItemList = savedInstanceState.getParcelableArrayList(SAVE_TOP_TEN_LIST);
            topTenListView.setAdapter(new TopTenListAdapter(getActivity(), mTopTenItemList));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVE_TOP_TEN_LIST, (ArrayList<? extends Parcelable>) mTopTenItemList);
        super.onSaveInstanceState(outState);
    }

    /**
     * Loads the top ten tracks of the artist into the listView passed in using the
     * Spotify-Wrapper-API.
     * @param topTenListView the listView to load the top ten tracks
     */
    private void loadTopTen(final ListView topTenListView){
        ProgressVisibler.showProgressWheel(mProgressWheel);

        Map<String, Object> params = new Hashtable<>();
        params.put("country", "US");

        new SpotifyApi().getService().getArtistTopTrack(getArguments().getString(EXTRA_SPOTIFY_ID), params,
                new retrofit.Callback<Tracks>() {
                    @Override
                    public void success(Tracks tracks, Response response) {
                        ProgressVisibler.hideProgressWheel(mProgressWheel);

                        if (response.getStatus() == 200) {
                            for (Track track : tracks.tracks) {
                                String thumbnailImage;
                                String thumbnailImageLarge;
                                String albumName;
                                String trackName;
                                String trackPreviewUrl;

                                //[1000px, 600px, 200px, 65px]
                                //the 2nd to last img is usually the one with width 200px(small)
                                //the 3rd to last img is usually the one with width 640px(large)
                                int imageListSize = track.album.images.size();

                                if (imageListSize > 2) {
                                    thumbnailImageLarge = track.album.images.get(imageListSize - 3).url;
                                    thumbnailImage = track.album.images.get(imageListSize - 2).url;

                                } else if (imageListSize == 2){
                                    thumbnailImageLarge = track.album.images.get(0).url;
                                    thumbnailImage = track.album.images.get(0).url;

                                } else if (imageListSize == 1) {
                                    thumbnailImageLarge = track.album.images.get(0).url;
                                    thumbnailImage = track.album.images.get(0).url;

                                } else {
                                    thumbnailImageLarge = IMAGE_ERROR;
                                    thumbnailImage = IMAGE_ERROR;
                                }

                                albumName = track.album.name;
                                trackName = track.name;
                                trackPreviewUrl = track.preview_url;

                                mTopTenItemList.add(new TopTenItem(thumbnailImage, thumbnailImageLarge, albumName, trackName, trackPreviewUrl));
                            }

                            topTenListView.setAdapter(new TopTenListAdapter(getActivity(), mTopTenItemList));
                        } else {
                            //if it is not a HTTP_OK response show toast error
                            Toast.makeText(getActivity(), R.string.toast_server_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        ProgressVisibler.hideProgressWheel(mProgressWheel);
                        //if it is a failure show toast error
                        Toast.makeText(getActivity(), R.string.toast_server_error, Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((Callback)getActivity())
                .onTrackItemClick(mTopTenItemList, position, getArguments().getString(EXTRA_ARTIST_NAME));
    }
}
