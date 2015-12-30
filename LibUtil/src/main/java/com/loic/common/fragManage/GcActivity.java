package com.loic.common.fragManage;

import com.loic.common.utils.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.HeaderViewListAdapter;
import android.widget.ListView;

public abstract class GcActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = GcActivity.class.getSimpleName();

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationMenu;
    private ActionBarDrawerToggle mDrawerToggle;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        // listen for navigation events
        navigationMenu = (NavigationView) findViewById(R.id.left_drawer);
        navigationMenu.setNavigationItemSelectedListener(this);
        onInitMainMenu(navigationMenu);

        onNavigationItemSelected(navigationMenu.getMenu().findItem(getInitMenuId()));

        // set up the hamburger icon to open and close the drawer
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, null, R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        // enable ActionBar app icon to behave as action to toggle nav drawer
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }
    }

    public GcFragment getCenterFragment()
    {
        return (GcFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }
    
    private void setCenterFragment(GcFragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    private void setCenterFragment(Class<? extends GcFragment> fragClass)
    {
        if(fragClass != null)
        {
            try
            {
                setCenterFragment(fragClass.newInstance());
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
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        menuItem.setChecked(true);
        GcFragment centerFragment = getCenterFragment();
        boolean consumed = (centerFragment != null) && centerFragment.onMainMenuSelected(menuItem);
        if(! consumed)
        {
            Class<? extends GcFragment> fragToShow = getFragClassForMenuItem(menuItem);
            if(fragToShow != null && (centerFragment == null || !fragToShow.equals(centerFragment.getClass())))
            {
                setCenterFragment(fragToShow);
            }
        }

        mDrawerLayout.closeDrawer(navigationMenu);
        return true;
    }

    abstract protected void onInitMainMenu(NavigationView navigationMenu);
    abstract protected @IdRes int getInitMenuId();
    abstract protected Class<? extends GcFragment> getFragClassForMenuItem(MenuItem menuItem);

    public void refreshMainMenu()
    {
        for (int i = 0, count = navigationMenu.getChildCount(); i < count; i++)
        {
            final View child = navigationMenu.getChildAt(i);
            if (child != null && child instanceof ListView)
            {
                final ListView menuView = (ListView) child;
                final HeaderViewListAdapter adapter = (HeaderViewListAdapter) menuView.getAdapter();
                final BaseAdapter wrapped = (BaseAdapter) adapter.getWrappedAdapter();
                wrapped.notifyDataSetChanged();
            }
        }
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
            boolean consumed = (centerFragment != null) && centerFragment.onBackPressed();
            if(! consumed)
            {
                super.onBackPressed();
            }
        }
    }

    public NavigationView getNavigationMenu()
    {
        return navigationMenu;
    }

    public void setActionBarVisible(boolean visible)
    {
        ActionBar actionBar = getSupportActionBar();
        if(actionBar != null)
        {
            if(visible)
            {
                actionBar.show();
            }
            else
            {
                actionBar.hide();
            }
        }
    }
}
