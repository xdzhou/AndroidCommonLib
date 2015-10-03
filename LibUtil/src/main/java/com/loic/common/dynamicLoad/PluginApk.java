package com.loic.common.dynamicLoad;

import android.content.pm.PackageInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;

import com.loic.common.fragManage.GcFragment;

import dalvik.system.DexClassLoader;

public class PluginApk implements IAppModule
{
    private static final String TAG = PluginApk.class.getSimpleName();

    private String apkFilePath;

    private PackageInfo packageInfo;
    private Resources res;
    private DexClassLoader pluginClassLoader;

    private IAppModule pluginModule;

    public PluginApk(String apkFilePath, PackageInfo packageInfo, Resources res, DexClassLoader pluginClassLoader)
    {
        this.apkFilePath = apkFilePath;
        this.packageInfo = packageInfo;
        this.res = res;
        this.pluginClassLoader = pluginClassLoader;
    }

    public boolean checkPluginValide()
    {
        boolean valid = getPluginName() != null && getPluginModule() != null;
        if(! valid)
        {
            dispose();
        }
        return valid;
    }

    public boolean isPluginClass (GcFragment frag)
    {
        return isPluginClass(frag.getClass());
    }

    public boolean isPluginClass (Class<? extends GcFragment> fragClass)
    {
        return (fragClass != null) && (fragClass.getClassLoader() == pluginClassLoader);
    }

    private IAppModule getPluginModule()
    {
        if(pluginModule == null)
        {
            try
            {
                Class<?> mClass = pluginClassLoader.loadClass(getPluginFrModuleClassName());
                if(IAppModule.class.isAssignableFrom(mClass))
                {
                    pluginModule = (IAppModule) mClass.newInstance();
                }
                else
                {
                    Log.e(TAG, "Loaded class is not IAppModule : "+mClass.getName());
                }
            }
            catch (ClassNotFoundException e)
            {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return pluginModule;
    }

    /**
     *  plugin name is saved in manifest : application / label
     */
    private String getPluginName()
    {
        String pluginName = null;
        if(packageInfo != null && packageInfo.applicationInfo != null)
        {
            int labelResID = packageInfo.applicationInfo.labelRes;
            pluginName = res.getString(labelResID);
        }
        return pluginName;
    }

    /**
     *  plugin module Class Name is saved in manifest : application / name
     *  @see IAppModule
     */
    private String getPluginFrModuleClassName()
    {
        String moduleClassName = null;
        if(packageInfo != null && packageInfo.applicationInfo != null)
        {
            moduleClassName = packageInfo.applicationInfo.name;
        }
        return moduleClassName;
    }

    public void dispose()
    {
        res = null;
        pluginClassLoader = null;
        pluginModule = null;
        packageInfo = null;
    }

    /**
     *  IAppModule implementation
     *  @see IAppModule
     */
    @Override
    public int size()
    {
        return pluginModule.size();
    }

    @Override
    public int getItemID(int index)
    {
        return pluginModule.getItemID(index);
    }

    @Override
    public int getItemNameResId(int fragId)
    {
        throw new UnsupportedOperationException("Not support for plugin");
    }

    @Override
    public String getItemName(int fragId)
    {
        return res.getString(pluginModule.getItemNameResId(fragId));
    }

    @Override
    public int getItemIconResId(int fragId)
    {
        throw new UnsupportedOperationException("Not support for plugin");
    }

    @Override
    public Drawable getItemIcon(int fragId)
    {
        return res.getDrawable(pluginModule.getItemNameResId(fragId));
    }

    @Override
    public Class<? extends GcFragment> getItemFragClass(int fragId)
    {
        return pluginModule.getItemFragClass(fragId);
    }

    @Override
    public boolean getItemVisibility(int fragId)
    {
        return pluginModule.getItemVisibility(fragId);
    }

    @Override
    public GcFragment onInitedFragment(GcFragment frag)
    {
        GcFragment initedFrag = pluginModule.onInitedFragment(frag);
        initedFrag.setPluginResource(res);
        return initedFrag;
    }

    @Override
    public boolean isPlugin()
    {
        return true;
    }

    @Override
    public int getAppModuleID()
    {
        return getPluginName().hashCode();
    }

    @Override
    public int getModuleNameResId()
    {
        throw new UnsupportedOperationException("Not support for plugin");
    }

    @Override
    public String getModuleName()
    {
        return getPluginName();
    }
}
