/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mcochin.spotifystreamer.R;
import com.mcochin.spotifystreamer.adapters.SearchResultsListAdapter;
import com.mcochin.spotifystreamer.pojos.SearchResultsItem;
import com.mcochin.spotifystreamer.utilities.ProgressVisibler;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * The fragment that holds the search results and search functionality
 */
public class SearchFragment extends Fragment implements TextView.OnEditorActionListener, AdapterView.OnItemClickListener {
    public static final String TAG = SearchFragment.class.getSimpleName();
    private static final String SAVE_SEARCH_RESULTS_LIST = "searchResultsList";

    private EditText mSearchEditText;
    private ProgressBar mProgressWheel;
    private SpotifyService mSpotifyService;
    private ArrayAdapter mSearchResultsListAdapter;
    private List<SearchResultsItem> mSearchResultsList;

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback{
        /**
         * SearchFragmentCallback for when an item has been selected.
         */
        void onSearchItemClick(String artistName, String spotifyId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(View v, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(v, savedInstanceState);

        mProgressWheel = (ProgressBar)v.findViewById(R.id.progress_wheel);
        mSearchEditText = (EditText) v.findViewById(R.id.search_edit_text);
        mSearchEditText.setOnEditorActionListener(this);

        ListView searchResultsListView = (ListView) v.findViewById(R.id.search_results_list_view);
        searchResultsListView.setOnItemClickListener(this);

        //save search result list on roation
        if (savedInstanceState == null){
            mSearchResultsList = new ArrayList<>();
        } else {
            mSearchResultsList = savedInstanceState.getParcelableArrayList(SAVE_SEARCH_RESULTS_LIST);
        }

        //setting list adapter
        mSearchResultsListAdapter = new SearchResultsListAdapter(getActivity(), mSearchResultsList);
        searchResultsListView.setAdapter(mSearchResultsListAdapter);

        //initialize spotifyService
        mSpotifyService = new SpotifyApi().getService();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(SAVE_SEARCH_RESULTS_LIST,
                (ArrayList<? extends Parcelable>) mSearchResultsList);
        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        //Load search results when you hit the search button on keyboard;
        if (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            ProgressVisibler.showProgressWheel(mProgressWheel);

            mSpotifyService.searchArtists(mSearchEditText.getText().toString(),
                    new retrofit.Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    Log.d(TAG, "HTTP Status: " + response.getStatus());
                    ProgressVisibler.hideProgressWheel(mProgressWheel);
                    if (response.getStatus() == 200) {
                        String thumbnailImage;
                        String artistName;
                        String spotifyId;

                        mSearchResultsList.clear();
                        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
                        if(actionBar != null){
                            actionBar.setSubtitle(null);
                        }

                        Fragment topTenFragment = getActivity().getSupportFragmentManager()
                                .findFragmentByTag(TopTenFragment.TAG);
                        if(topTenFragment != null) {
                            getActivity().getSupportFragmentManager().beginTransaction().remove(topTenFragment).commit();
                        }

                        for (Artist artist : artistsPager.artists.items) {

                            //[1000px, 600px, 200px, 65px]
                            //the 2nd to last img is usually the one with width of 200px
                            int imageListSize = artist.images.size();

                            if (imageListSize > 1) {
                                thumbnailImage = artist.images.get(imageListSize - 2).url;

                            } else if (imageListSize == 1) {
                                thumbnailImage = artist.images.get(0).url;

                            } else {
                                thumbnailImage = "error";
                            }

                            artistName = artist.name;
                            spotifyId = artist.id;

                            //create list item to be added to layout
                            mSearchResultsList.add(new SearchResultsItem(thumbnailImage, artistName, spotifyId));
                        }

                        //if the results are empty show toast error
                        if (mSearchResultsList.isEmpty()) {
                            Toast.makeText(getActivity(), R.string.toast_artist_not_found, Toast.LENGTH_SHORT).show();
                        }
                        mSearchResultsListAdapter.notifyDataSetChanged();
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
            });

            //hide keyboard explicitly since were basically intercepting down event if keyboard is shown
            //resulting in the inability to dismiss the keyboard if you hit the search button
            hideKeyboard();
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String artistName = mSearchResultsList.get(position).getArtistName();
        String spotifyId = mSearchResultsList.get(position).getSpotifyId();
        ((Callback)getActivity()).onSearchItemClick(artistName, spotifyId);
    }

    /**
     * Hides soft input keyboard
     */
    public void hideKeyboard(){
        ((InputMethodManager) getActivity().getSystemService(Activity.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(mSearchEditText.getWindowToken(), 0);
    }
}
