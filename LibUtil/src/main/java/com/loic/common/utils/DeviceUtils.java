package com.loic.common.utils;

import android.content.res.Configuration;

import com.loic.common.LibApplication;

public class DeviceUtils 
{

    public static boolean isLandscape()
    {
        return LibApplication.getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }
    
    public static boolean isLargeScreen() 
    {
        int layout = LibApplication.getContext().getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        return layout == Configuration.SCREENLAYOUT_SIZE_XLARGE || layout == Configuration.SCREENLAYOUT_SIZE_LARGE;
    }
}
