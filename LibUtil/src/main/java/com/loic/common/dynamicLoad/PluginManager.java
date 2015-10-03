package com.loic.common.dynamicLoad;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.util.Log;

import com.loic.common.LibApplication;
import com.loic.common.utils.FileUtils;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dalvik.system.DexClassLoader;

public class PluginManager
{
    private static final String TAG = PluginManager.class.getSimpleName();

    private static PluginManager instance;

    private Context mContext;
    private String pluginFolder = null;
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
        //optimizedDirectory = mContext.getDir("dex", Context.MODE_PRIVATE).getAbsolutePath();
        //libraryPath = mContext.getDir("lib", Context.MODE_PRIVATE).getAbsolutePath();
        pluginApks = new HashMap<String, PluginApk>();
        String[] packages = LibApplication.getAppContext().getPackageName().split(".");
        setAppName(packages[packages.length - 1]);
    }

    private void setAppName(@NonNull String appName)
    {
        String appFolder = Environment.getExternalStorageDirectory().getAbsolutePath()+"/"+appName;
        pluginFolder = appFolder+"/plugins";
        File file = new File(pluginFolder);
        if(! file.isDirectory())
        {
            file.mkdir();
        }
        optimizedDirectory = appFolder+"/dex";
        file = new File(optimizedDirectory);
        if(! file.isDirectory())
        {
            file.mkdir();
        }
        libraryPath = appFolder+"/lib";
        file = new File(libraryPath);
        if(! file.isDirectory())
        {
            file.mkdir();
        }

        initManagerFromPath(pluginFolder);
    }

    private void initManagerFromPath(@NonNull String pluginFloderPath)
    {
        initManagerFromPath(new File(pluginFloderPath));
    }

    private void initManagerFromPath(@NonNull File pluginDirectory)
    {
        if(pluginDirectory.isDirectory())
        {
            for(File pluginFile : pluginDirectory.listFiles())
            {
                loadApk(pluginFile);
            }
        }
        else
        {
            Log.e(TAG, "Can't find directory : "+pluginDirectory.getAbsolutePath());
        }
    }

    public int size()
    {
        int size = 0;
        if(pluginApks != null)
        {
            size = pluginApks.size();
        }
        return size;
    }

    public List<PluginApk> getAllPlugins()
    {
        return new ArrayList<PluginApk>(pluginApks.values());
    }

    public PluginApk loadApk(@NonNull File apkFile)
    {
        PluginApk plugin = null;
        if(apkFile.exists() && apkFile.getName().endsWith("apk"))
        {
            plugin = pluginApks.get(apkFile.getAbsolutePath());
            if(plugin == null)
            {
                PackageInfo packageInfo = mContext.getPackageManager().getPackageArchiveInfo(apkFile.getAbsolutePath(), PackageManager.GET_ACTIVITIES);
                if(packageInfo != null)
                {
                    DexClassLoader loader = new DexClassLoader(apkFile.getAbsolutePath(), optimizedDirectory, libraryPath, mContext.getClassLoader());
                    AssetManager assetManager = createAssetManager(apkFile.getAbsolutePath());
                    if(assetManager != null)
                    {
                        Resources res = createResources(assetManager);
                        plugin = new PluginApk(apkFile.getAbsolutePath(), packageInfo, res, loader);
                        if(! plugin.checkPluginValide())
                        {
                            plugin = null;
                        }
                        else
                        {
                            pluginApks.put(apkFile.getAbsolutePath(), plugin);
                        }
                    }
                    else
                    {
                        Log.e(TAG, "Oops, can't create AssetManager");
                    }
                }
                else
                {
                    Log.e(TAG, "get packageInfo failed for "+apkFile.getAbsolutePath());
                }
            }
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
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Log.e(TAG, "create AssetManager Failed : "+e);
            return null;
        }
    }

    private Resources createResources(AssetManager assetManager)
    {
        Resources superRes = mContext.getResources();
        return new Resources(assetManager, superRes.getDisplayMetrics(), superRes.getConfiguration());
    }
}
