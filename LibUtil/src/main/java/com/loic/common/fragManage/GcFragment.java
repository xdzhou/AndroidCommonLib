package com.loic.common.fragManage;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.loic.common.LibApplication;
import com.loic.common.utils.R;

import android.app.Activity;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

public class GcFragment extends Fragment 
{
    private NiftyDialogBuilder dialogShowing;
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
        } else
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
    public void onDestroyView()
    {
        super.onDestroyView();
        hideDialog();
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
        if(activity != null && activity instanceof GcActivity)
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
    private View.OnClickListener cancelDialogListener = new View.OnClickListener()
    {
        @Override
        public void onClick(View v)
        {
            hideDialog();
        }
    };

    public void hideDialog()
    {
        if(dialogShowing != null && dialogShowing.isShowing() && getActivity() != null)
        {
            getActivity().runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    dialogShowing.dismiss();
                }
            });
        }
    }

    protected int getInitDialogBackgroundColor()
    {
        return -1;
    }

    private NiftyDialogBuilder initAlertDialog()
    {
        NiftyDialogBuilder dialogBuilder = new NiftyDialogBuilder(getActivity(), R.style.dialog_untran);
        int color = getInitDialogBackgroundColor();
        if(color != -1)
            dialogBuilder.withDialogColor(color);
        return dialogBuilder;
    }

    public NiftyDialogBuilder createDialogBuilder(String title, String msg)
    {
        hideDialog();
        dialogShowing = initAlertDialog();

        return dialogShowing.withTitle(title).withMessage(msg).setCustomView(null, null);
    }

    public NiftyDialogBuilder createDialogBuilderWithCancel (String title, String msg)
    {
        hideDialog();
        dialogShowing = initAlertDialog();

        return dialogShowing.withTitle(title)
        .withMessage(msg)
        .withButton1Text(getString(android.R.string.cancel))
        .setButton1Click(cancelDialogListener);
    }
}
