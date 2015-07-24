/*
 * Copyright (C) 2015 Marco Chin
 */

package com.mcochin.spotifystreamer.utilities;

import android.support.v4.util.Pair;

/**
 * Utility class to convert time from one unit to another
 */
public class TimeConverter {
    public static final int SECONDS = 1000;
    public static final int MINUTES = 60;


    /**
     * Converts milliseconds into seconds
     * @param milli the milliseconds to be converted
     * @return the seconds converted from milliseconds
     */
    public static int convertMilliToSec(int milli){
        return milli/SECONDS;
    }

    /**
     * Converts milliseconds into seconds
     * @param sec the seconds to be converted
     * @return the milliseconds converted from seconds
     */
    public static int convertSecToMilli(int sec){
        return sec*SECONDS;
    }

    /**
     * Converts seconds into minutes and seconds
     * @param rawSeconds the seconds to be converted
     * @return a pair object where the first and second variables are minutes and seconds respectively
     */
    public static Pair<Integer, Integer> convertSecToMinAndSec(int rawSeconds){
        int minutes = rawSeconds/MINUTES;
        int seconds = rawSeconds%MINUTES;

        return new Pair<>(minutes, seconds);
    }
}
