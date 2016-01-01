package com.loic.common.utils;

import java.util.Locale;

import com.loic.common.LibApplication;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.wifi.WifiManager;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.inputmethod.InputMethodManager;

public class AndroidUtils 
{
    public static void closeSoftKeyboard(Activity activity)
    {
        if (activity != null)
        {
            View view = activity.getCurrentFocus();
            if(view != null)
            {
                InputMethodManager imm = (InputMethodManager)LibApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }
    /**
     * 把一个view转换为Bitmap，可用于屏幕截图
     * 
     * @param view
     *          需要转换的view
     */
    public static Bitmap ViewToBitmap(View view)
    {
        if(view == null) 
            return null;
        view.measure(MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), 
                     MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        //Define a bitmap with the same size as the view        
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(bitmap);
        //Get the view's background
        Drawable bgDrawable =view.getBackground();
        if (bgDrawable != null) 
            bgDrawable.draw(canvas);
        else canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        return bitmap;
    } 
    
    /**
     * 把 dip 数值转换为 px（像素值）
     * @param dipValue    
     *             dip 数值 
     */
    public static float dip2px(int dipValue) 
    {
        Resources r = LibApplication.getContext().getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    }
    
    /**
     * 得到当前应用的 版本名 Version Name
     */
    public static @Nullable String getVersionName()
    {
        String versionName = null;
        try 
        {
            PackageInfo pInfo = LibApplication.getContext().getPackageManager().getPackageInfo(LibApplication.getContext().getPackageName(), 0);
            versionName = pInfo.versionName;
        } 
        catch (NameNotFoundException e) 
        {
            e.printStackTrace();
        }
        return versionName;
    }
    
    /**
     * 得到当前应用的 版本号 Version Code
     * 
     */
    public static int getVersionCode()
    {
        int versionCode = -1;
        try 
        {
            PackageInfo pInfo = LibApplication.getContext().getPackageManager().getPackageInfo(LibApplication.getContext().getPackageName(), 0);
            versionCode = pInfo.versionCode;
        } 
        catch (NameNotFoundException e) 
        {
            e.printStackTrace();
        }
        return versionCode;
    }
    
    /**
     * 得到当前的语言
     * 
     */
    public static String getLocalLanguage()
    {
        //return LibApplication.getContext().getResources().getConfiguration().locale.getDisplayLanguage();
        return Locale.getDefault().getLanguage();
    }
    
    /**
     * 得到当前应用的 IMEI
     */
    public static String getIMEI() 
    {
        String imei = null;
        try 
        {
            TelephonyManager telephonyManager = (TelephonyManager) LibApplication.getContext().getSystemService(Context.TELEPHONY_SERVICE);
            imei = telephonyManager.getDeviceId();
        } 
        catch (Exception e) 
        {    
            e.printStackTrace();
        }
        return imei;
    }

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
