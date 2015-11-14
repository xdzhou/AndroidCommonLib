package com.loic.common.utils;

import com.loic.common.LibApplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetWorkUtils 
{

    public static boolean isNetworkAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) LibApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }
    
    public static boolean isWifiAvailable()
    {
        ConnectivityManager cm = (ConnectivityManager) LibApplication.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting() && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI;
    }
}
