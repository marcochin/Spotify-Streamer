/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.utilities;

import android.view.View;
import android.widget.ProgressBar;

/**
 * Utility class to reveal or hide progress wheel
 */
public class ProgressVisibler {
    public static void showProgressWheel(ProgressBar progressWheel){
        progressWheel.setVisibility(View.VISIBLE);
    }
    public static void hideProgressWheel(ProgressBar progressWheel){
        progressWheel.setVisibility(View.INVISIBLE);
    }

}
