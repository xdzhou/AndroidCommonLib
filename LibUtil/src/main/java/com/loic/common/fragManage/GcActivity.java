package com.loic.common.fragManage;

import com.loic.common.utils.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

public abstract class GcActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = GcActivity.class.getSimpleName();
    private static final String NAV_ITEM_ID = "navItemId";

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationMenu;
    private ActionBarDrawerToggle mDrawerToggle;

    private int curMenuItemId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // load saved navigation state if present
        if (savedInstanceState != null)
        {
            curMenuItemId = savedInstanceState.getInt(NAV_ITEM_ID, -1);
        }

        // listen for navigation events
        navigationMenu = (NavigationView) findViewById(R.id.left_drawer);
        navigationMenu.setNavigationItemSelectedListener(this);
        onInitMainMenu(navigationMenu);

        int menuIdToShow = -1;
        if (savedInstanceState != null)
        {
            menuIdToShow = savedInstanceState.getInt(NAV_ITEM_ID, -1);
        }
        if(menuIdToShow == -1)
        {
            menuIdToShow = getInitMenuId();
        }
        onNavigationItemSelected(navigationMenu.getMenu().findItem(menuIdToShow));

        // set up the hamburger icon to open and close the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, null, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
    }

    public GcFragment getCenterFragment()
    {
        return (GcFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }
    
    public void setCenterFragment(GcFragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }



    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() != curMenuItemId)
        {
            menuItem.setChecked(true);
            GcFragment centerFragment = getCenterFragment();
            boolean consumed = (centerFragment != null) && centerFragment.onMainMenuSelected(menuItem);
            if(! consumed)
            {
                onMainMenuSelected(menuItem);
            }
            curMenuItemId = menuItem.getItemId();
        }
        mDrawerLayout.closeDrawer(navigationMenu);
        return true;
    }

    abstract protected void onInitMainMenu(NavigationView navigationMenu);
    abstract protected @IdRes int getInitMenuId();

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
            boolean consumed = (centerFragment != null) && centerFragment.onBackPressed();
            if(! consumed)
            {
                super.onBackPressed();
            }
        }
    }

    protected void onMainMenuSelected(MenuItem menuItem)
    {
    }

    public NavigationView getNavigationMenu()
    {
        return navigationMenu;
    }
}
