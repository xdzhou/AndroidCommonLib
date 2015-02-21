package com.loic.common.fragManage;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public abstract class GcFragment extends Fragment 
{
	protected NiftyDialogBuilder dialogBuilder;
	
	@Override
	public void onDestroy() 
	{
		super.onDestroy();
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
		MultiFragmentManager manager = null;
		if(getParentFragment() != null && getParentFragment() instanceof MultiFragmentManager)
			manager = (MultiFragmentManager) getParentFragment();
		return manager;
	}
	
	public ActionBarActivity getGcActivity()
	{
		ActionBarActivity gcActivity = null;
		if(getActivity() != null && getActivity() instanceof ActionBarActivity)
			gcActivity = ((ActionBarActivity) getActivity());
		return gcActivity;
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
		if(dialogBuilder != null)
			dialogBuilder.dismiss();
	}
	
	public NiftyDialogBuilder createDialogBuilder(String title, String msg)
	{
		if(dialogBuilder == null)
			dialogBuilder = NiftyDialogBuilder.getInstance(getActivity());
		
		return dialogBuilder.withTitle(title)
	    .withMessage(msg).setCustomView(null);
	}
	
	public void showDialog(final String title, final String msg)
	{
		if(getActivity() != null)
		{
			getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
					hideDialog();
					createDialogBuilder(title, msg).show();
				}
			});
		}
		
	}
	
	public NiftyDialogBuilder createDialogBuilderWithCancel (String title, String msg)
	{
		if(dialogBuilder == null)
			dialogBuilder = NiftyDialogBuilder.getInstance(getActivity());
		
		return dialogBuilder.withTitle(title)
	    .withMessage(msg)
	    .withButton1Text(getString(android.R.string.cancel))
	    .setButton1Click(cancelDialogListener);
	}
	
	public void showDialogWithCancel (String title, String msg)
	{
		hideDialog();
		createDialogBuilderWithCancel(title, msg).show();
	}
	
}
