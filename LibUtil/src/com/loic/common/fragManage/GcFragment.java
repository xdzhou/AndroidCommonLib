package com.loic.common.fragManage;

import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.MotionEvent;

public abstract class GcFragment extends Fragment 
{
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
}
