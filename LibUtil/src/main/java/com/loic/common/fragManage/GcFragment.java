package com.loic.common.fragManage;

import com.loic.common.LibApplication;
import com.loic.common.utils.R;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class GcFragment extends Fragment 
{
    private AlertDialog mAlertDialog;
    private Resources mRes;

    protected int getLayoutResID()
    {
        return R.layout.multifragmentcontroller;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(this.mRes == null)
        {
            Fragment parentFrag = getParentFragment();
            if(parentFrag != null && parentFrag instanceof GcFragment)
            {
                this.mRes = ((GcFragment)parentFrag).mRes;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View rootView = null;
        if(mRes == null)
        {
            rootView = inflater.inflate(getLayoutResID(), container, false);
        }
        else
        {
            final XmlResourceParser parser = mRes.getLayout(getLayoutResID());
            try
            {
                rootView = inflater.inflate(parser, container, false);
            } finally
            {
                parser.close();
            }
        }

        return rootView;
    }

    @Override
    public void onStop()
    {
        super.onStop();
        hideDialog();
    }

    public boolean onBackPressed()
    {
        return false;
    }

    public boolean dispatchKeyEvent(KeyEvent event)
    {
        return false;
    }

    public boolean dispatchGenericMotionEvent(MotionEvent event)
    {
        return false;
    }

    public boolean onMainMenuSelected(MenuItem menuItem)
    {
        return false;
    }

    public MultiFragmentManager getMultiFragmentManager()
    {
        Fragment parentFrag = getParentFragment();
        if(parentFrag != null && parentFrag instanceof MultiFragmentManager)
        {
            return (MultiFragmentManager) parentFrag;
        }
        return null;
    }

    public GcActivity getGcActivity()
    {
        Activity activity = getActivity();
        if(activity instanceof GcActivity)
        {
            return (GcActivity) activity;
        }
        return null;
    }

    public void setPluginResource(Resources res)
    {
        this.mRes = res;
    }

    protected Resources getMyResources()
    {
        return (mRes != null) ? mRes : LibApplication.getAppContext().getResources();
    }

    public int getFragID()
    {
        return this.getClass().getName().hashCode();
    }

    /******************************************************
     ********************* Dialog action ******************
     ******************************************************/
    protected DialogInterface.OnClickListener cancelDialogListener = new DialogInterface.OnClickListener()
    {
        @Override
        public void onClick(DialogInterface dialog, int which)
        {
            dialog.dismiss();
        }
    };

    public void hideDialog()
    {
        if(mAlertDialog != null)
        {
            mAlertDialog.dismiss();
            mAlertDialog = null;
        }
    }

    public void showDialog(@NonNull final AlertDialog.Builder builder)
    {
        Activity activity = getActivity();
        if(activity != null &&  !activity.isFinishing())
        {
            activity.runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    hideDialog();
                    mAlertDialog = builder.create();
                    mAlertDialog.show();
                }
            });
        }
    }

    public void showDialog(@NonNull String title, @Nullable String msg, @Nullable View customView)
    {
        if(getActivity() != null && (msg != null || customView != null))
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(title);
            if(msg != null)
            {
                builder.setMessage(msg);
            }
            if(customView != null)
            {
                builder.setView(customView);
            }
            builder.setPositiveButton(android.R.string.ok, cancelDialogListener);
            showDialog(builder);
        }
    }

    public void showDialog(@StringRes int titleRes, @StringRes int msgRes, @Nullable final View customView)
    {
        Resources res = mRes == null ? LibApplication.getAppContext().getResources() : mRes;
        showDialog(res.getString(titleRes), res.getString(msgRes), customView);
    }

    public void showDialog(@StringRes int titleRes, String msg, @Nullable final View customView)
    {
        Resources res = mRes == null ? LibApplication.getAppContext().getResources() : mRes;
        showDialog(res.getString(titleRes), msg, customView);
    }
}
