package com.loic.common.fragManage;

import com.loic.common.utils.R;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

public abstract class GcActivity extends ActionBarActivity 
{
    private DrawerLayout mDrawerLayout;
    private ListView menuDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    protected MenuDrawerAdapter menuAdapter;
    private MenuCreater menuCreater;
    protected GcFragment centerFragment;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        menuDrawerList = (ListView) findViewById(R.id.left_drawer);

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener
        menuCreater = new MenuCreater();
        initMenuItem(menuCreater);
        menuAdapter = new MenuDrawerAdapter(getApplicationContext(), menuCreater.getMenuItems());
        menuDrawerList.setAdapter(menuAdapter);
        menuDrawerList.setOnItemClickListener(new OnItemClickListener() 
        {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
			{
				if(menuAdapter.getItemViewType(position) == 1)
				{
					menuDrawerList.setItemChecked(position, true);
					onOpenElement(menuAdapter.getItem(position), position);
					closeMainMenu();
				}
			}
		});

        // enable ActionBar app icon to behave as action to toggle nav drawer
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.drawable.ic_drawer, R.string.drawer_open, R.string.drawer_close) 
        {
            public void onDrawerClosed(View view) 
            {
            }

            public void onDrawerOpened(View drawerView) 
            {
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        setCenterFragment(getInitCenterFragment(GcFragment.class));
    }
    
    public void openMainMenu()
    {
    	mDrawerLayout.openDrawer(menuDrawerList);
    }
    
    public void closeMainMenu()
    {
    	mDrawerLayout.closeDrawer(menuDrawerList);
    }
    
    protected void onOpenElement(MenuElementItem menuElementItem, int position)
    {
    	if(MultiFragmentManager.class.isAssignableFrom(menuElementItem.fragmentClass))
    	{
    		setCenterFragment(menuElementItem.fragmentClass);
    	}
    	else if(centerFragment != null && centerFragment instanceof MultiFragmentManager)
    	{
    		((MultiFragmentManager)centerFragment).onOpenElement(menuElementItem, position);
		}
    }
    
    protected void setCenterFragment(Class<? extends GcFragment> fragmentClass)
    {
    	try 
    	{
    		centerFragment = fragmentClass.newInstance();
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
    protected void onPostCreate(Bundle savedInstanceState) 
    {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) 
    {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }
    
    private Class<? extends GcFragment> getInitCenterFragment(Class<? extends GcFragment> defaultFragClass)
    {
    	Class<? extends GcFragment> fragClass = getInitCenterFragment();
    	return fragClass == null ? defaultFragClass : fragClass;
    }
    
    //abstract method
    protected abstract Class<? extends GcFragment> getInitCenterFragment();
    protected abstract void initMenuItem(MenuCreater menuCreater);
}
