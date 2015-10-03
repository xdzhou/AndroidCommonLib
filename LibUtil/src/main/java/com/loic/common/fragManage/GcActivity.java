package com.loic.common.fragManage;

import com.loic.common.dynamicLoad.IAppModule;
import com.loic.common.dynamicLoad.PluginApk;
import com.loic.common.dynamicLoad.PluginManager;
import com.loic.common.utils.R;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;

public abstract class GcActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static final String TAG = GcActivity.class.getSimpleName();
    private static final String NAV_ITEM_ID = "navItemId";

    private DrawerLayout mDrawerLayout;
    private NavigationView navigationMenu;
    private ActionBarDrawerToggle mDrawerToggle;

    private MenuManager mMenuManager;
    private int mNavItemId = -1;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // load saved navigation state if present
        if (savedInstanceState != null)
        {
            mNavItemId = savedInstanceState.getInt(NAV_ITEM_ID, -1);
        }

        // listen for navigation events
        navigationMenu = (NavigationView) findViewById(R.id.left_drawer);
        navigationMenu.setNavigationItemSelectedListener(this);
        mMenuManager = new MenuManagerImpl(navigationMenu.getMenu());

        if(mNavItemId == -1)
        {
            mNavItemId = navigationMenu.getMenu().getItem(0).getItemId();
        }

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
    }

    public GcFragment getCenterFragment()
    {
        return (GcFragment)getSupportFragmentManager().findFragmentById(R.id.content_frame);
    }
    
    private void setCenterFragment(GcFragment fragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem)
    {
        if (menuItem.getItemId() != mNavItemId)
        {
            if(!mMenuManager.onMenuItemSelected(menuItem))
            {
                onMenuItemSelected(menuItem);
            }
        }
        mMenuManager.closeMenu();
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
            boolean consumed = (centerFragment != null) && centerFragment.onBackPressed();
            if(! consumed)
            {
                super.onBackPressed();
            }
        }
    }

    protected void onMenuItemSelected(MenuItem menuItem)
    {
    }

    public MenuManager getMenuManager()
    {
        return mMenuManager;
    }

    /**
     * Used to create main menu
     * */
    public class MenuManagerImpl implements MenuManager
    {
        private Menu mMainMenu;
        private SparseArray<IAppModule> appModuleMap;
        private boolean mIsPluginLoaded = false;

        public MenuManagerImpl(@NonNull Menu menu)
        {
            this.mMainMenu = menu;
        }

        @Override
        public boolean onMenuItemSelected(MenuItem menuItem)
        {
            IAppModule module = appModuleMap.get(menuItem.getGroupId());
            if(module != null)
            {
                try
                {
                    GcFragment centerFragment = module.getItemFragClass(menuItem.getItemId()).newInstance();
                    centerFragment = module.onInitedFragment(centerFragment);
                    setCenterFragment(centerFragment);
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
            return module != null;
        }

        @Override
        public MenuItem findMenuItemById(int id)
        {
            return mMainMenu.findItem(id);
        }

        @Override
        public void refreshItem()
        {
            for(int i = 0; i < mMainMenu.size(); i++)
            {
                MenuItem menuItem = mMainMenu.getItem(i);
                IAppModule appModule = appModuleMap.get(menuItem.getGroupId());
                if(appModule != null)
                {
                    mMainMenu.getItem(i).setVisible(appModule.getItemVisibility(menuItem.getItemId()));
                }
                else
                {
                    Log.e(TAG, "Oops, can't find AppModule for menuItem : "+menuItem);
                }
            }
        }

        @Override
        public boolean isMenuOpen()
        {
            return false;
        }

        @Override
        public void openMenu()
        {
            mDrawerLayout.openDrawer(navigationMenu);
        }

        @Override
        public void closeMenu()
        {
            mDrawerLayout.closeDrawer(navigationMenu);
        }

        @Override
        public boolean isMenuEnable()
        {
            return false;
        }

        @Override
        public void setMenuEnable(boolean enable)
        {
            mDrawerLayout.setDrawerLockMode(enable ? DrawerLayout.LOCK_MODE_UNLOCKED : DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        }

        @Override
        public MenuTransaction beginTransaction()
        {
            return new MenuTransactionImpl(this);
        }

        protected void loadPlugin()
        {
            if(! mIsPluginLoaded)
            {
                PluginManager pluginManager = PluginManager.getInstance();
                if(pluginManager.size() > 0)
                {
                    for(PluginApk plugin : pluginManager.getAllPlugins())
                    {
                        addModule(plugin);
                    }
                }
                mIsPluginLoaded = true;
            }
            else
            {
                Log.e(TAG, "Plugin has be loaded !");
            }
        }

        protected void addModule(@NonNull IAppModule module)
        {
            if(appModuleMap == null)
            {
                appModuleMap = new SparseArray<>();
            }

            if(appModuleMap.indexOfKey(module.getAppModuleID()) >= 0)
            {
                Log.e(TAG, "App Module has been loaded : "+module);
            }
            else
            {
                appModuleMap.put(module.getAppModuleID(), module);
                int size = module.size();
                for(int i = 0; i < size; i++)
                {
                    int itemId = module.getItemID(i);
                    addMenuItem(module.getItemName(itemId), module.getItemIcon(itemId), module.getAppModuleID(), itemId, module.getItemVisibility(itemId));
                }
            }
        }

        protected void addMenuItem(String title, Drawable icon, int groupId, int itemId, boolean visibility)
        {
            MenuItem menuItem = mMainMenu.add(groupId, itemId, Menu.NONE, title);
            menuItem.setIcon(icon);
            menuItem.setVisible(visibility);
        }

        protected void addMenuItem(String title, Drawable icon, int itemId)
        {
            addMenuItem(title, icon, Menu.NONE, itemId, true);
        }

        protected void removeMenuItem(int itemId)
        {
            if(mMainMenu.findItem(itemId) == null)
            {
                Log.e(TAG, "removeMenuItem fail : can't find menu item with id "+itemId);
            }
            else
            {
                mMainMenu.removeItem(itemId);
            }
        }

        protected void setMenuItemVisibility(int itemId, boolean visible)
        {
            MenuItem menuItem = mMainMenu.findItem(itemId);
            if(menuItem == null)
            {
                Log.e(TAG, "setMenuItemVisibility fail : can't find menu item with id "+itemId);
            }
            else
            {
                menuItem.setVisible(visible);
            }
        }

        protected void enqueueAction(Runnable runnable)
        {
            if(! isFinishing())
            {
                runOnUiThread(runnable);
            }
        }
    }
}
