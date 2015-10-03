package com.loic.common.dynamicLoad;

import android.graphics.drawable.Drawable;

import com.loic.common.fragManage.GcFragment;

public interface IAppModule
{
    /** get App Module ID */
    int getAppModuleID();

    /** get App Module Name */
    int getModuleNameResId();
    String getModuleName();

    /** get App Module sous Frag count */
    int size();

    /** get App Module sous Frag ID */
    int getItemID(int index);

    /** get App Module sous Frag Name */
    int getItemNameResId(int fragId);
    String getItemName(int fragId);

    /** get App Module sous Frag Icon */
    int getItemIconResId(int fragId);
    Drawable getItemIcon(int fragId);

    /** get App Module sous Frag Class */
    Class<? extends GcFragment> getItemFragClass(int fragId);

    /** get App Module sous Frag Visibility */
    boolean getItemVisibility(int fragId);

    /** do some preparation for fragment */
    GcFragment onInitedFragment(GcFragment frag);

    boolean isPlugin();
}
