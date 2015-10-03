package com.loic.common.fragManage;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MenuItem;

import com.loic.common.LibApplication;
import com.loic.common.dynamicLoad.IAppModule;

public interface MenuTransaction
{
    MenuTransaction loadPluginMenu();
    MenuTransaction addAppModule(IAppModule module);

    MenuTransaction addMenuItem(String title, Drawable icon, int itemId);
    MenuTransaction addMenuItem(int titleResId, int iconResId, int itemId);
    MenuTransaction addMenuItem(String title, int iconResId, int itemId);
    MenuTransaction addMenuItem(int titleResId, Drawable icon, int itemId);

    MenuTransaction removeMenuitem(MenuItem menuItem);
    MenuTransaction removeMenuitem(int itemId);

    MenuTransaction hideMenuItem(MenuItem menuItem);
    MenuTransaction hideMenuItem(int itemId);

    MenuTransaction showMenuItem(MenuItem menuItem);
    MenuTransaction showMenuItem(int itemId);

    void commit();
}

class MenuTransactionImpl implements MenuTransaction, Runnable
{
    private static final String TAG = MenuTransactionImpl.class.getSimpleName();
    private GcActivity.MenuManagerImpl mMenuManager;
    private OpRecord mOpRecordHead;
    private OpRecord mOpRecordTail;
    private boolean mIsCommited = false;

    public MenuTransactionImpl(@NonNull GcActivity.MenuManagerImpl menuManager)
    {
        this.mMenuManager = menuManager;
    }

    private enum OpType
    {
        Op_Load_Plugin,
        OP_Add_Module,
        Op_Add_Item,
        Op_Remove,
        Op_Show,
        Op_Hide
    }

    private static class OpRecord
    {
        OpRecord mNextOp;

        OpType mOpType;
        IAppModule mModule;
        String mTitle;
        Drawable mIcon;
        int mItemId;
    }

    private void addOpRecord(@NonNull OpRecord op)
    {
        if(mOpRecordHead == null)
        {
            mOpRecordHead = op;
            mOpRecordTail = mOpRecordHead;
        }
        else
        {
            mOpRecordTail.mNextOp = op;
            mOpRecordTail = op;
        }
    }

    @Override
    public MenuTransaction loadPluginMenu()
    {
        OpRecord op = new OpRecord();
        op.mOpType = OpType.Op_Load_Plugin;
        addOpRecord(op);
        return this;
    }

    @Override
    public MenuTransaction addAppModule(IAppModule module)
    {
        OpRecord op = new OpRecord();
        op.mOpType = OpType.OP_Add_Module;
        addOpRecord(op);
        return this;
    }

    @Override
    public MenuTransaction addMenuItem(String title, Drawable icon, int itemId)
    {
        OpRecord op = new OpRecord();
        op.mOpType = OpType.Op_Add_Item;
        op.mTitle = title;
        op.mIcon = icon;
        op.mItemId = itemId;
        addOpRecord(op);
        return this;
    }

    @Override
    public MenuTransaction addMenuItem(int titleResId, int iconResId, int itemId)
    {
        Resources res = LibApplication.getAppContext().getResources();
        return addMenuItem(res.getString(titleResId), res.getDrawable(iconResId), itemId);
    }

    @Override
    public MenuTransaction addMenuItem(String title, int iconResId, int itemId)
    {
        Resources res = LibApplication.getAppContext().getResources();
        return addMenuItem(title, res.getDrawable(iconResId), itemId);
    }

    @Override
    public MenuTransaction addMenuItem(int titleResId, Drawable icon, int itemId)
    {
        Resources res = LibApplication.getAppContext().getResources();
        return addMenuItem(res.getString(titleResId), icon, itemId);
    }

    @Override
    public MenuTransaction removeMenuitem(MenuItem menuItem)
    {
        return removeMenuitem(menuItem.getItemId());
    }

    @Override
    public MenuTransaction removeMenuitem(int itemId)
    {
        OpRecord op = new OpRecord();
        op.mOpType = OpType.Op_Remove;
        op.mItemId = itemId;
        addOpRecord(op);
        return this;
    }

    @Override
    public MenuTransaction hideMenuItem(MenuItem menuItem)
    {
        return hideMenuItem(menuItem.getItemId());
    }

    @Override
    public MenuTransaction hideMenuItem(int itemId)
    {
        OpRecord op = new OpRecord();
        op.mOpType = OpType.Op_Hide;
        op.mItemId = itemId;
        addOpRecord(op);
        return this;
    }

    @Override
    public MenuTransaction showMenuItem(MenuItem menuItem)
    {
        return showMenuItem(menuItem.getItemId());
    }

    @Override
    public MenuTransaction showMenuItem(int itemId)
    {
        OpRecord op = new OpRecord();
        op.mOpType = OpType.Op_Show;
        op.mItemId = itemId;
        addOpRecord(op);
        return this;
    }

    @Override
    public void run()
    {
        OpRecord currentRecord = mOpRecordHead;
        while (currentRecord != null)
        {
            switch (currentRecord.mOpType)
            {
                case Op_Load_Plugin:
                    mMenuManager.loadPlugin();
                    break;
                case OP_Add_Module:
                    mMenuManager.addModule(currentRecord.mModule);
                    break;
                case Op_Add_Item:
                    mMenuManager.addMenuItem(currentRecord.mTitle, currentRecord.mIcon, currentRecord.mItemId);
                    break;
                case Op_Remove:
                    mMenuManager.removeMenuItem(currentRecord.mItemId);
                    break;
                case Op_Hide:
                    mMenuManager.setMenuItemVisibility(currentRecord.mItemId, false);
                    break;
                case Op_Show:
                    mMenuManager.setMenuItemVisibility(currentRecord.mItemId, true);
                    break;
            }
            currentRecord = currentRecord.mNextOp;
        }
    }

    @Override
    public void commit()
    {
        if(mIsCommited)
        {
            Log.e(TAG, "Already committed !");
        }
        else
        {
            if(mOpRecordHead == null)
            {
                Log.d(TAG, "nothing to committed !");
            }
            else
            {
                mMenuManager.enqueueAction(this);
            }
        }
    }
}
