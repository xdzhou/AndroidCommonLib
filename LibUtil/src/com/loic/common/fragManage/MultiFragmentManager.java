package com.loic.common.fragManage;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MultiFragmentManager extends GcFragment
{
	private static final String TAG = MultiFragmentManager.class.getSimpleName();
	
	private static final String Child_Fragment_Tags_Key = "Child_Fragment_Tags_Key";
	private ArrayList<String> childFragTags;
	private FragmentManager fm;

	public MultiFragmentManager()
	{
		super();
		this.childFragTags = new ArrayList<String>();
		fm = getChildFragmentManager();
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
			GcFragment frag = (GcFragment) fm.findFragmentByTag(fragmentKey);
			if (fragClass.isInstance(frag))
			{
				returnedFragment = (T) frag;
			}
		}
		return returnedFragment;
	}
	
	public GcFragment getFragmentByTag(String tag)
	{
		GcFragment fragment = null;
		if(childFragTags.contains(tag))
		{
			fragment = (GcFragment) fm.findFragmentByTag(tag);
		}
		return fragment;
	}

	public void addFragment(GcFragment frag, String tag)
	{
		if(!childFragTags.contains(tag))
		{
			FragmentTransaction ft = fm.beginTransaction();
			ft.add(frag, tag).commit();
			fm.executePendingTransactions();
			this.childFragTags.add(tag);
		}
		else 
		{
			throw new IllegalArgumentException("GcFragment Tag :"+tag+" Exist...");
		}
	}
	
	public void addFragment(GcFragment frag)
	{
		StringBuilder sb = new StringBuilder(frag.getClass().getSimpleName());
		sb.append(".").append(Math.random());
		addFragment(frag, sb.toString());
	}
	
	public void removeFragment(String tag)
	{
		if(childFragTags.contains(tag))
		{
			GcFragment frag = (GcFragment) fm.findFragmentByTag(tag);
			FragmentTransaction ft = fm.beginTransaction();
			ft.remove(frag).commit();
			this.childFragTags.remove(tag);
		}
	}

    public void showFragment(String tag)
    {
    	if(childFragTags.contains(tag))
		{
			Fragment frag = fm.findFragmentByTag(tag);
			FragmentTransaction ft = getChildFragmentManager().beginTransaction();
			ft.show(frag).commit();
			fm.executePendingTransactions();
		}
    }

    public void hideFragment(String tag)
    {
    	if(childFragTags.contains(tag))
		{
			Fragment frag = fm.findFragmentByTag(tag);
			FragmentTransaction ft = fm.beginTransaction();
			ft.hide(frag).commit();
			fm.executePendingTransactions();
		}
    }
    
    public int getFragmentSize()
    {
    	return childFragTags.size();
    }
    
    public List<String> getFragmentTags()
    {
    	List<String> tagsList = new ArrayList<String>();
    	tagsList.addAll(childFragTags);
    	return tagsList;
    }

    @Override
    public void onDestroy()
    {
        Log.v(TAG, this + ": onDestroy isremoving=" + isRemoving());
        for (String tag : childFragTags)
        {
            this.removeFragment(tag);
        }
        super.onDestroy();
    }
    
    @Override
    public boolean onBackPressed()
    {
    	Log.v(TAG, this+": MFM onBackPressed");
        boolean consumed = false;
        
        // Dispatch onBackPressed
        for (String tag : childFragTags)
        {
            GcFragment frag = getFragmentByTag(tag);
            if (frag != null)
            {
                consumed |= frag.onBackPressed();
                Log.v(TAG, "onBackPressed from:"+frag+" consumed:"+consumed);
                if (consumed) 
                	break;
            }
        }
        
        // if consumed = true, ARActivity onBackPressed is not called.
        return consumed;
    }
}
