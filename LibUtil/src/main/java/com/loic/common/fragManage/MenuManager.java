package com.loic.common.fragManage;

import android.view.MenuItem;

public interface MenuManager
{
    MenuItem findMenuItemById(int id);
    void refreshItem();
    boolean onMenuItemSelected(MenuItem menuItem);

    boolean isMenuOpen();
    void openMenu();
    void closeMenu();

    boolean isMenuEnable();
    void setMenuEnable(boolean enable);

    MenuTransaction beginTransaction();
}