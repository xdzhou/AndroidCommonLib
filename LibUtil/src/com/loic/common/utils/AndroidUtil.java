package com.loic.common.utils;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.TelephonyManager;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.Toast;

public class AndroidUtil {
	/**
     * 检测网络是否可用
     * 
     */
	public static boolean isNetworkAvailable() 
	{
        ConnectivityManager mgr = (ConnectivityManager) LibApplication.getAppContext().getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = mgr.getAllNetworkInfo();
        if (info != null) 
        {
            for (int i = 0; i < info.length; i++) 
            {
                if (info[i].getState() == NetworkInfo.State.CONNECTED) 
                {
                    return true;
                }
            }
        }
        return false;
	}
	
	/**
     * 在屏幕上层显示一个提示框
     * @param title
     * 			提示框标题
     * @param msg    
     * 			提示框内显示的信息 
     */
	public static void showInfo(String title, String msg) 
	{
        new AlertDialog.Builder(LibApplication.getAppContext()).setTitle(title).setMessage(msg)
            .setPositiveButton(android.R.string.ok, null).show();
	}
	
	/**
     * 在屏幕上显示一个稍后自动消失的提示信息
     * @param msg    
     * 			显示的信息 
     */
	public static void showInfo(final String msg) 
	{
        new Thread(new Runnable() 
        {		
			@Override
			public void run()
			{
				Looper.prepare();
				Toast.makeText(LibApplication.getAppContext(), msg, Toast.LENGTH_SHORT).show();
				Looper.loop();
			}
		}).start();
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
     * 			dip 数值 
     */
	public static float dip2px(int dipValue) 
	{
		Resources r = LibApplication.getAppContext().getResources();
		float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, r.getDisplayMetrics());
		return px;
	}
	
	/**
     * 得到手机屏幕的宽度
     * 
     * @param activity
     *          环境，为activity
     */
	public static int getScreenWidth(Activity activity)
	{
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		return display.getWidth();
	}
	
	/**
     * 得到手机屏幕的高度
     * 
     * @param activity
     *          环境，为activity
     */
	public static int getScreenHeight(Activity activity)
	{
		WindowManager manager = activity.getWindowManager();
		Display display = manager.getDefaultDisplay();
		return display.getHeight();
	}
	
	/**
     * 得到当前应用的 版本名 Version Name
     */
	public static String getVersionName()
	{
		String version_name = null;
		try 
		{
			PackageInfo pInfo = LibApplication.getAppContext().getPackageManager().getPackageInfo(LibApplication.getAppContext().getPackageName(), 0);
			version_name = pInfo.versionName;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return version_name;
	}
	
	/**
     * 得到当前应用的 版本号 Version Code
     * 
     */
	public static int getVersionCode()
	{
		int version_code = -1;
		try 
		{
			PackageInfo pInfo = LibApplication.getAppContext().getPackageManager().getPackageInfo(LibApplication.getAppContext().getPackageName(), 0);
			version_code = pInfo.versionCode;
		} 
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return version_code;
	}
	
	/**
     * 得到当前的语言
     * 
     */
	public static String getLocalLanguage()
	{
		//return LibApplication.getAppContext().getResources().getConfiguration().locale.getDisplayLanguage();
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
			TelephonyManager telephonyManager = (TelephonyManager) LibApplication.getAppContext().getSystemService(LibApplication.getAppContext().TELEPHONY_SERVICE);
			imei = telephonyManager.getDeviceId();
		} 
		catch (Exception e) 
		{	
			e.printStackTrace();
		}
		return imei;
	}
	
	public static String getImsi() 
	{
		TelephonyManager tm = (TelephonyManager) LibApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId();
	}
	
	public static String getSimSerialNumber() 
	{
		TelephonyManager tm = (TelephonyManager) LibApplication.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSimSerialNumber();
	}
	/**
     * 得到当前手机的 WIFI MAC
     */
    public static String getWifiMacAddress() 
    {
        try 
        {
            WifiManager wifimanager = (WifiManager) LibApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
            String mac = wifimanager.getConnectionInfo().getMacAddress();
            if (mac.isEmpty()) 
            	return null;
            return mac;
        } 
        catch (Exception e) 
        {
        	return null;
        }
    }
    // 打印所有的 intent extra 数据
    public static String printBundle(Bundle bundle) 
    {
        StringBuilder sb = new StringBuilder();
        for (String key : bundle.keySet()) 
        {
            sb.append("\nkey:" + key + ", value:" + bundle.getString(key));
        }
        return sb.toString();
    }
    
    public static boolean is2gNetwork() 
    {
        TelephonyManager tm = (TelephonyManager) LibApplication.getAppContext().
		getSystemService(Context.TELEPHONY_SERVICE);
	    int type = tm.getNetworkType();
	    if (type == TelephonyManager.NETWORK_TYPE_GPRS || type == TelephonyManager.NETWORK_TYPE_EDGE) 
	    {
	    	return true;
	    }
	    return false;
    }
}
