/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The list item for the search results list.
 */
public class SearchResultsItem implements Parcelable {
    String mImageThumbnail;
    String mArtistName;
    String mSpotifyId;

    public SearchResultsItem(String imageThumbnail, String artistName, String spotifyId){
        mImageThumbnail = imageThumbnail;
        mArtistName = artistName;
        mSpotifyId = spotifyId;
    }

    public String getImageThumbnail() {
        return mImageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        mImageThumbnail = imageThumbnail;
    }

    public String getArtistName() {
        return mArtistName;
    }

    public void setArtistName(String artistName) {
        mArtistName = artistName;
    }

    public String getSpotifyId() {
        return mSpotifyId;
    }

    public void setSpotifyId(String spotifyid) {
        mSpotifyId = spotifyid;
    }

    protected SearchResultsItem(Parcel in) {
        mImageThumbnail = in.readString();
        mArtistName = in.readString();
        mSpotifyId = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageThumbnail);
        dest.writeString(mArtistName);
        dest.writeString(mSpotifyId);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SearchResultsItem> CREATOR = new Parcelable.Creator<SearchResultsItem>() {
        @Override
        public SearchResultsItem createFromParcel(Parcel in) {
            return new SearchResultsItem(in);
        }

        @Override
        public SearchResultsItem[] newArray(int size) {
            return new SearchResultsItem[size];
        }
    };
}