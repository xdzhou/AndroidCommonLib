package com.loic.common.fragManage;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MultiFragmentController extends Fragment
{
	private static final String TAG = MultiFragmentController.class.getSimpleName();
	
	private static final String Child_Fragment_Tags_Key = "Child_Fragment_Tags_Key";
	private ArrayList<String> childFragTags;

	public MultiFragmentController()
	{
		super();
		this.childFragTags = new ArrayList<String>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState)
	{	    
	    if(savedInstanceState != null)
	    {
	    	this.childFragTags = savedInstanceState.getStringArrayList(Child_Fragment_Tags_Key);
	    }
	    super.onCreate(savedInstanceState);
	    FragmentManager.enableDebugLogging(true);
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(Child_Fragment_Tags_Key, this.childFragTags);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		if (savedInstanceState != null)
		{
			this.childFragTags = savedInstanceState.getStringArrayList(Child_Fragment_Tags_Key);
		}
		return null;
	}

	/*
	 * _________________
	 * 
	 * Fragments handling methods : get
	 * _________________
	 */

	/**
	 * Return the first fragment of the corresponding class
	 * 
	 * @param fragClass
	 *            the class type of the fragment we are looking for
	 * @return Fragment the fragment of the given type
	 */
	public <T> T getFragmentOfClass(Class<T> fragClass)
	{
		T returnedFragment = null;
		for (int i = 0; (returnedFragment == null) && (i < this.childFragTags.size()); ++i)
		{
			String fragmentKey = this.childFragTags.get(i);
			Fragment frag = getChildFragmentManager().findFragmentByTag(fragmentKey);
			if (fragClass.isInstance(frag))
			{
				returnedFragment = (T) frag;
			}
		}
		return returnedFragment;
	}

	public void addFragment(Fragment frag, String tag)
	{
		FragmentTransaction ft = getChildFragmentManager().beginTransaction();
		ft.add(frag, tag).commit();
		this.childFragTags.add(tag);
	}
	
	public void removeFragment(String tag)
	{
		if(childFragTags.contains(tag))
		{
			Fragment frag = getChildFragmentManager().findFragmentByTag(tag);
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.remove(frag).commit();
			this.childFragTags.remove(tag);
		}
	}

    public void showFragment(String tag)
    {
    	if(childFragTags.contains(tag))
		{
			Fragment frag = getChildFragmentManager().findFragmentByTag(tag);
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.show(frag).commit();
			getChildFragmentManager().executePendingTransactions();
		}
    }

    public void hideFragment(String tag)
    {
    	if(childFragTags.contains(tag))
		{
			Fragment frag = getChildFragmentManager().findFragmentByTag(tag);
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.hide(frag).commit();
			getChildFragmentManager().executePendingTransactions();
		}
    }

    @Override
    public void onStop()
    {
        Log.v(TAG, this + ": onStop isRemoving=" + isRemoving());
        if (isRemoving())
        {
           
        }
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        Log.v(TAG, this + ": onDestroy isremoving=" + isRemoving());
        if (isRemoving())
        {
            for (String tag : childFragTags)
            {
                this.removeFragment(tag);
            }
        }
        super.onDestroy();
    }
}
