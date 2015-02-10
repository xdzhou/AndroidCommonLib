package com.loic.common.fragManage;

import java.util.ArrayList;
import java.util.List;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

import com.loic.common.LibApplication;

public class MenuCreater 
{
	private List<MenuElementItem> menuElementItems;
	
	public MenuCreater()
	{
		menuElementItems = new ArrayList<MenuElementItem>();
	}
	
	public MenuCreater appendAutoAppMenuSection()
	{
		try 
		{
			PackageManager pm = LibApplication.getAppContext().getPackageManager();
			PackageInfo pInfo = pm.getPackageInfo(LibApplication.getAppContext().getPackageName(), 0);
			String appName = pm.getApplicationLabel(LibApplication.getAppContext().getApplicationInfo()).toString();
			
			menuElementItems.add(new MenuElementItem(null, appName.concat(" ").concat(pInfo.versionName), true, -1));
		}
		catch (NameNotFoundException e) 
		{
			e.printStackTrace();
		}
		return this;
	}
	
	public MenuCreater appendMenuSection(String title, int iconResId)
	{
		menuElementItems.add(new MenuElementItem(null, title, true, iconResId));
		return this;
	}
	
	public MenuCreater appendMenuElement(String title, Class<? extends GcFragment> fragmentClass, int iconResId)
	{
		menuElementItems.add(new MenuElementItem(fragmentClass, title, false, iconResId));
		return this;
	}
	
	public void resetCreater()
	{
		menuElementItems.clear();
	}
	
	public List<MenuElementItem> getMenuItems()
	{
		return menuElementItems;
	}
	
	public int getMenuItemCount()
	{
		return menuElementItems.size();
	}
}
