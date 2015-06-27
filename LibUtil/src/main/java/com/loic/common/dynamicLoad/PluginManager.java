package com.loic.common.dynamicLoad;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

import com.loic.common.LibApplication;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class PluginManager
{
    private static final String TAG = PluginManager.class.getSimpleName();

    private static PluginManager instance;

    private Context mContext;
    private String optimizedDirectory = null;
    private String libraryPath = null;
    private Map<String, PluginApk> pluginApks;

    public static PluginManager getInstance()
    {
        if(instance == null)
        {
            synchronized (PluginManager.class)
            {
                if(instance == null)
                {
                    instance = new PluginManager();
                }
            }
        }
        return instance;
    }

    private PluginManager()
    {
        mContext = LibApplication.getAppContext();
        optimizedDirectory = mContext.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();
        libraryPath = mContext.getDir("lib", Context.MODE_PRIVATE).getAbsolutePath();
        pluginApks = new HashMap<String, PluginApk>();
    }

    public PluginApk getPluginApk(String apkFilePath)
    {
        PluginApk plugin = null;
        File apkFile = new File(apkFilePath);
        if(apkFile.exists() && apkFile.getName().endsWith("apk"))
        {
            plugin = pluginApks.get(apkFile.getName());
            if(plugin == null)
            {
                plugin = loadApk(apkFilePath);
            }
        }
        return plugin;
    }

    private PluginApk loadApk(String apkFilePath)
    {
        PluginApk plugin = null;
        PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(apkFilePath, PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES);
        if(packageInfo != null)
        {
            DexClassLoader loader = new DexClassLoader(apkFilePath, optimizedDirectory, libraryPath, mContext.getClassLoader());
            AssetManager assetManager = createAssetManager(apkFilePath);
            Resources res = createResources(assetManager);
            plugin = new PluginApk(apkFilePath, packageInfo, res, loader);

            pluginApks.put(new File(apkFilePath).getName(), plugin);

        } else
        {
            Log.e(TAG, "get packageInfo failed for "+apkFilePath);
        }
        return plugin;
    }

    private AssetManager createAssetManager(String dexPath)
    {
        try
        {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, dexPath);
            return assetManager;
        } catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "create AssetManager Failed : "+e);
            return null;
        }
    }

    private Resources createResources(AssetManager assetManager)
    {
        Resources superRes = mContext.getResources();
        Resources resources = new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
        return resources;
    }
}
