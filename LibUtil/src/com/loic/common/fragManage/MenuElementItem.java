package com.loic.common.fragManage;

public class MenuElementItem 
{
	public Class<? extends GcFragment> fragmentClass;
	public String title;
	public boolean isForSection;
	public int iconResId = -1;
	
	public MenuElementItem(Class<? extends GcFragment> fragmentClass,String title, boolean isForSection, int iconResId) 
	{
		this.fragmentClass = fragmentClass;
		this.title = title;
		this.isForSection = isForSection;
		this.iconResId = iconResId;
	}
}
