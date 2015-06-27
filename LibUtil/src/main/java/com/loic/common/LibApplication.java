package com.loic.common;

import android.app.Application;
import android.content.Context;

public class LibApplication extends Application 
{
    private static Context context;
    private static LibApplication globalApplication;

    public void onCreate()
    {
        super.onCreate();
        LibApplication.context = getApplicationContext();
        LibApplication.globalApplication = this;
    }

    public static Context getAppContext()
    {
        return LibApplication.context;
    }
    
    public static LibApplication getApp()
    {
        return LibApplication.globalApplication;
    }

}
