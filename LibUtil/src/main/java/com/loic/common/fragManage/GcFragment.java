package com.loic.common.fragManage;

import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.loic.common.utils.R;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class GcFragment extends Fragment 
{
	private NiftyDialogBuilder dialogShowing;
	
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
	
	public GcActivity getGcActivity()
	{
		GcActivity gcActivity = null;
		if(getActivity() != null && getActivity() instanceof GcActivity)
			gcActivity = ((GcActivity) getActivity());
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
