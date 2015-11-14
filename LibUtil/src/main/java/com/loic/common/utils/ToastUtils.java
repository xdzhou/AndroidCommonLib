package com.loic.common.utils;

import com.loic.common.LibApplication;

import android.widget.Toast;

/**
 * ToastUtils
 * 
 * @author <a href="http://www.trinea.cn" target="_blank">Trinea</a> 2013-12-9
 */
public class ToastUtils 
{

    public static void show(int resId) 
    {
        show(LibApplication.getContext().getResources().getText(resId), Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration) 
    {
        show(LibApplication.getContext().getResources().getText(resId), duration);
    }

    public static void show(CharSequence text) 
    {
        show(text, Toast.LENGTH_SHORT);
    }

    public static void show(CharSequence text, int duration)
    {
        Toast.makeText(LibApplication.getContext(), text, duration).show();
    }

    public static void show(int resId, Object... args) 
    {
        show(String.format(LibApplication.getContext().getResources().getString(resId), args), Toast.LENGTH_SHORT);
    }

    public static void show(String format, Object... args) 
    {
        show(String.format(format, args), Toast.LENGTH_SHORT);
    }

    public static void show(int resId, int duration, Object... args) 
    {
        show(String.format(LibApplication.getContext().getResources().getString(resId), args), duration);
    }

    public static void show(String format, int duration, Object... args) 
    {
        show(String.format(format, args), duration);
    }
}
