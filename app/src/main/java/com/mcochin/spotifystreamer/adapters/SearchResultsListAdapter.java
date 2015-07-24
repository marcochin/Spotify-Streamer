/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.mcochin.spotifystreamer.R;
import com.mcochin.spotifystreamer.pojos.SearchResultsItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter class to connect the list item data to the layout
 */
public class SearchResultsListAdapter extends ArrayAdapter<SearchResultsItem>{

    private static class ViewHolder {
        ImageView mThumbnailImage;
        TextView mArtistName;

        private ViewHolder(View v){
            mThumbnailImage = (ImageView) v.findViewById(R.id.thumbnail_image_view);
            mArtistName = (TextView) v.findViewById(R.id.artist_text_view);
        }
    }

    public SearchResultsListAdapter(Context context, List<SearchResultsItem> searchResultsList) {
        super(context, 0, searchResultsList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        SearchResultsItem searchItem = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder;
        if (convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.search_results_list_item, parent, false);
            viewHolder = new ViewHolder(convertView);

            // view lookup cache stored in tag
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        // Populate the data into the template view using the data object
        Picasso.with(getContext())
                .load(searchItem.getImageThumbnail())
                .error(R.drawable.no_image_200px)
                .into(viewHolder.mThumbnailImage);

        viewHolder.mArtistName.setText(searchItem.getArtistName());
        // Return the completed view to render on screen
        return convertView;
    }
}