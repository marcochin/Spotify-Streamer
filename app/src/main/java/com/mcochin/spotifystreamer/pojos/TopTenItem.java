/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.pojos;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * The list item for the top tracks list.
 */
public class TopTenItem implements Parcelable {
    String mImageThumbnail;
    String mImageThumbnailLarge;
    String mAlbumName;
    String mTrackName;
    String mTrackPreviewUrl;

    public TopTenItem(String imageThumbnail, String imageThumbnailLarge, String albumName, String trackName, String trackPreviewUrl) {
        mImageThumbnail = imageThumbnail;
        mImageThumbnailLarge = imageThumbnailLarge;
        mAlbumName = albumName;
        mTrackName = trackName;
        mTrackPreviewUrl = trackPreviewUrl;
    }

    public String getImageThumbnail() {
        return mImageThumbnail;
    }

    public void setImageThumbnail(String imageThumbnail) {
        mImageThumbnail = imageThumbnail;
    }

    public String getImageThumbnailLarge() {
        return mImageThumbnailLarge;
    }

    public void setImageThumbnailLarge(String imageThumbnailLarge) {
        mImageThumbnailLarge = imageThumbnailLarge;
    }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        mAlbumName = albumName;
    }

    public String getTrackName() {
        return mTrackName;
    }

    public void setTrackName(String trackName) {
        mTrackName = trackName;
    }

    public String getTrackPreviewUrl() {
        return mTrackPreviewUrl;
    }

    public void setmTrackPreviewUrl(String trackPreviewUrl) {
        mTrackPreviewUrl = trackPreviewUrl;
    }

    protected TopTenItem(Parcel in) {
        mImageThumbnail = in.readString();
        mImageThumbnailLarge = in.readString();
        mAlbumName = in.readString();
        mTrackName = in.readString();
        mTrackPreviewUrl = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mImageThumbnail);
        dest.writeString(mImageThumbnailLarge);
        dest.writeString(mAlbumName);
        dest.writeString(mTrackName);
        dest.writeString(mTrackPreviewUrl);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<TopTenItem> CREATOR = new Parcelable.Creator<TopTenItem>() {
        @Override
        public TopTenItem createFromParcel(Parcel in) {
            return new TopTenItem(in);
        }

        @Override
        public TopTenItem[] newArray(int size) {
            return new TopTenItem[size];
        }
    };
}