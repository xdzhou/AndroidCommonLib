package com.loic.common.dynamicLoad;

import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.os.Parcel;
import android.util.Log;

import com.loic.common.fragManage.GcFragment;

import dalvik.system.DexClassLoader;

public class PluginApk
{
    private static final String TAG = PluginApk.class.getSimpleName();

    private String apkFilePath;

    private PackageInfo packageInfo;
    private Resources res;
    private DexClassLoader pluginClassLoader;

    private Class<? extends GcFragment> pluginFragmentClass;

    public PluginApk(String apkFilePath, PackageInfo packageInfo, Resources res, DexClassLoader pluginClassLoader)
    {
        this.apkFilePath = apkFilePath;
        this.packageInfo = packageInfo;
        this.res = res;
        this.pluginClassLoader = pluginClassLoader;
    }

    /*
        plugin name is saved in manifest : application / activity / label
     */
    public String getPluginName()
    {
        String pluginName = null;
        if(packageInfo != null && packageInfo.activities != null)
        {
            int labelResID = packageInfo.activities[0].labelRes;
            pluginName = res.getString(labelResID);
        }
        return pluginName;
    }

    public Class<? extends GcFragment> getPluginFragmentClass()
    {
        if(pluginFragmentClass == null)
        {
            try
            {
                Class<?> mClass = pluginClassLoader.loadClass(getPluginFragmentClassName());
                if(GcFragment.class.isAssignableFrom(mClass))
                {
                    pluginFragmentClass = (Class<? extends GcFragment>) mClass;
                } else
                {
                    Log.e(TAG, "Loaded class is not GcFragment : "+mClass.getName());
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }
        return pluginFragmentClass;
    }

    public GcFragment getPluginFragment()
    {
        if(pluginFragmentClass == null)
        {
            try
            {
                Class<?> mClass = pluginClassLoader.loadClass(getPluginFragmentClassName());
                if(GcFragment.class.isAssignableFrom(mClass))
                {
                    pluginFragmentClass = (Class<? extends GcFragment>) mClass;
                } else
                {
                    Log.e(TAG, "Loaded class is not GcFragment : "+mClass.getName());
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            }
        }

        GcFragment frag = null;
        if(pluginFragmentClass != null)
        {
            try
            {
                frag = pluginFragmentClass.newInstance();
                frag.setPluginResource(this.res);
            } catch (InstantiationException e)
            {
                e.printStackTrace();
            } catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
        }

        return frag;
    }

    /*
        plugin Fragment Class Name is saved in manifest : application / activity / name
     */
    private String getPluginFragmentClassName()
    {
        String fragClassName = null;
        if(packageInfo != null && packageInfo.activities != null)
        {
            fragClassName = packageInfo.activities[0].name;
        }
        fragClassName = "com.sky.plugin.MainPlugin";
        return fragClassName;
    }

}
