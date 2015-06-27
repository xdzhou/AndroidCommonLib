package com.loic.common.fragManage;

import com.loic.common.utils.R;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

public abstract class GcActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String NAV_ITEM_ID = "navItemId";

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationMenu;
    private ActionBarDrawerToggle mDrawerToggle;

    private SparseArray<Class<? extends GcFragment>> menuFragMap;
    private int mNavItemId;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load saved navigation state if present
        if (null == savedInstanceState) {
            mNavItemId = R.id.drawer_item_1;
        } else {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID);
        }

        // listen for navigation events
        navigationMenu = (NavigationView) findViewById(R.id.left_drawer);
        navigationMenu.setNavigationItemSelectedListener(this);

        initMenuItem(new MenuEditor());

        // select the correct nav menu item
        navigationMenu.getMenu().findItem(mNavItemId).setChecked(true);

        // set up the hamburger icon to open and close the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener


        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        /*
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
                if(mDrawerLayout.isDrawerOpen(navigationMenu))
                    closeMainMenu();
                else
                    openMainMenu();
            }
        });
        */
    }
    
    public void setLeftMenuEnable(boolean enable)
    {
        mDrawerLayout.setDrawerLockMode(enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }
    
    public void openMainMenu()
    {
        mDrawerLayout.openDrawer(navigationMenu);
    }
    
    public void closeMainMenu()
    {
        mDrawerLayout.closeDrawer(navigationMenu);
    }

    public GcFragment getCenterFragment()
    {
        return (GcFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }

    protected void setCenterFragment(@NonNull GcFragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }
    
    protected void setCenterFragment(Class<? extends GcFragment> fragmentClass)
    {
        try 
        {
            Fragment centerFragment = fragmentClass.newInstance();
            getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, centerFragment).commit();
        } 
        catch (InstantiationException e) 
        {
            e.printStackTrace();
        } 
        catch (IllegalAccessException e) 
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() != mNavItemId)
        {
            setCenterFragment(menuFragMap.get(menuItem.getItemId()));
            closeMainMenu();
        }
        return true;
    }
    
    @Override
    protected void onPostCreate(Bundle savedInstanceState) 
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }
    
    @Override
    public boolean onOptionsItemSelected (MenuItem item)
    {
        mDrawerToggle.onOptionsItemSelected(item);
        return super.onOptionsItemSelected (item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    @Override
    public void onBackPressed() 
    {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
        {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            GcFragment centerFragment = getCenterFragment();
            boolean consumed = (centerFragment != null) ? centerFragment.onBackPressed() : false;
            if(! consumed)
            {
                super.onBackPressed();
            }
        }
    }
    
    //abstract method
    protected abstract void initMenuItem(MenuEditor menuEditor);

    public class MenuEditor
    {
        boolean isCommited = false;

        public MenuEditor appendMenuItem(@NonNull String title, @NonNull Drawable icon, @NonNull Class<? extends GcFragment> contentFragClass)
        {
            int menuItemId = contentFragClass.getName().hashCode();
            menuFragMap.put(menuItemId, contentFragClass);
            MenuItem menuItem = navigationMenu.getMenu().add(Menu.NONE, menuItemId,Menu.NONE, title);
            menuItem.setIcon(icon);
            return this;
        }

        public MenuEditor appendMenuItem(int titleResId, int iconResId, @NonNull Class<? extends GcFragment> contentFragClass)
        {
            return appendMenuItem(getString(titleResId), getDrawable(iconResId), contentFragClass);
        }

        public MenuEditor appendMenuItem(@NonNull String title, int iconResId, @NonNull Class<? extends GcFragment> contentFragClass)
        {
            return appendMenuItem(title, getDrawable(iconResId), contentFragClass);
        }

        public MenuEditor appendMenuItem(int titleResId, @NonNull Drawable icon, @NonNull Class<? extends GcFragment> contentFragClass)
        {
            return appendMenuItem(getString(titleResId), icon, contentFragClass);
        }

        public void commit()
        {
            isCommited = true;
        }
    }
}
