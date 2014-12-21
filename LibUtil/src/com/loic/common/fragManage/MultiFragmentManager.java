package com.loic.common.fragManage;

import java.util.ArrayList;
import java.util.List;

import com.loic.common.utils.DeviceUtils;
import com.loic.common.utils.R;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class MultiFragmentManager extends GcFragment
{
	private static final String TAG = MultiFragmentManager.class.getSimpleName();
	private static final String MFC_KEYS_ARRAY = "MFC_KEYS_ARRAY";
    private static final String MFC_FORCE_LANDSCAPE = "MFC_FORCE_LANDSCAPE";
	private static int keyindex = 0;

	private ArrayList<String> fragmentKeys;
	private List<GcFragment> preLoadedFragments;
	private boolean alreadyLoaded = false;
    private boolean forceLandscape = false;
	private FrameLayout parent;

    private FragmentManager mChildFragmentManager;

	/*
	 * _________________
	 *
	 * Static Methods
	 * _________________
	 */

	/**
	 * Returns a unique fragment tag
	 *
	 * @param frag
	 *            the fragment from which the tag will be created
	 * @return the unique tag
	 */
	private static String getTagForFragment(GcFragment frag)
	{
		String retVal;
		synchronized (MultiFragmentManager.class)
		{
			StringBuilder builder = new StringBuilder();
			builder.append(MultiFragmentManager.class.getName());
			builder.append(".");
			builder.append(frag.getClass().getSimpleName());
			builder.append(".");
			builder.append(MultiFragmentManager.keyindex);
			retVal = builder.toString();
			++MultiFragmentManager.keyindex;
		}
		return retVal;
	}

	/*
	 * _________________
	 *
	 * Constructors _________________
	 */

	/**
	 * MultiLayerFragment default constructor.
	 */
	public MultiFragmentManager()
	{
		super();
		this.preLoadedFragments = new ArrayList<GcFragment>();
		this.fragmentKeys = new ArrayList<String>();
	}

	/*
	 * _________________
	 *
	 * Fragment Lifecycle
	 * _________________
	 */

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	}

	/**
	 * Prepare from an orientation change
	 *
	 * @param outState
	 *            the bundle in which the private settings will be set
	 */
	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);
		outState.putStringArrayList(MFC_KEYS_ARRAY, this.fragmentKeys);
        outState.putBoolean(MFC_FORCE_LANDSCAPE, forceLandscape);
	}

	/**
	 * Life cycle method called when the view has to be created
	 *
	 * @param inflater
	 *            the layout inflater from the container
	 * @param container
	 *            the view which will contain this fragment's view
	 * @param savedInstanceState
	 *            the bundle from orientation change recovery
	 * @return View the created view.
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		View view = inflater.inflate(R.layout.multifragmentcontroller, container, false);
		//parent = container;
        parent = (FrameLayout) view.findViewById(R.id.mfc_relativelayout);

        mChildFragmentManager = getChildFragmentManager();

		if (savedInstanceState != null)
		{
            this.forceLandscape = savedInstanceState.getBoolean(MFC_FORCE_LANDSCAPE);
            this.fragmentKeys = savedInstanceState.getStringArrayList(MFC_KEYS_ARRAY);
            if (!this.forceLandscape || DeviceUtils.isLandscape())
            {
                refreshFragments();
            }
		}

        if (this.forceLandscape && !DeviceUtils.isLandscape())
        {
            //Do not load fragment
        }
        else
        {
            if ((this.preLoadedFragments != null) && (this.preLoadedFragments.size() > 0))
            {
                if (this.preLoadedFragments.size() != this.fragmentKeys.size())
                {
                    // Should never come here
                    Log.e(TAG, "Different number of preloaded fragments and keys");
                }

                for (int i = 0; i < this.preLoadedFragments.size(); ++i)
                {
                    GcFragment preloadedFrag = this.preLoadedFragments.get(i);
                    String fragTag = this.fragmentKeys.get(i);
                    this.attachFragmentWithTagAtPosition(preloadedFrag, fragTag, i, false);
                }
                this.mChildFragmentManager.executePendingTransactions();
                this.preLoadedFragments.clear();
            }
        }

		this.alreadyLoaded = true;
		return view;
	}

	private void refreshFragments()
    {
        for (String fragTag : this.fragmentKeys) 
        {
            GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragTag);
            if (frag != null) 
            {
                FragmentTransaction tr = this.mChildFragmentManager.beginTransaction();
                tr.detach(frag);
                tr.attach(frag);
                tr.commit();
            } 
            else 
            {
                Log.w(TAG, "Cannot refresh fragment " + fragTag + " it is null");
            }
        }
        this.mChildFragmentManager.executePendingTransactions();
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
	@SuppressWarnings("unchecked")
	public <T> T getFragmentOfClass(Class<T> fragClass)
	{
		T returnedFragment = null;
		for (int i = 0; (returnedFragment == null) && (i < this.fragmentKeys.size()); ++i)
		{
			String fragmentKey = this.fragmentKeys.get(i);
			GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragmentKey);
			if (fragClass.isInstance(frag))
			{
				returnedFragment = (T) frag;
			}
		}
		return returnedFragment;
	}
	
	public <T> String getFragmentTagOfClass(Class<T> fragClass)
	{
		String tag = null;
		for (int i = 0; (tag == null) && (i < this.fragmentKeys.size()); ++i)
		{
			String fragmentKey = this.fragmentKeys.get(i);
			GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragmentKey);
			if (fragClass.isInstance(frag))
			{
				tag = fragmentKey;
			}
		}
		return tag;
	}

	/**
	 * Return the fragment with the specified tag.
	 * 
	 * @param tag the tag identifying the fragment 
	 * @return the fragment associated with the given tag or null if no fragment
	 * exists with tag.
	 */
	public GcFragment getFragmentWithTag(String tag)
	{
	    GcFragment frag = null;
        if (this.fragmentKeys.contains(tag))
        {
            frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(tag);
        }
        return frag;
	}
	
	/*
	 * _________________
	 *
	 * Fragments handling methods : insert
	 * _________________
	 */

    /**
     * Insert a fragment at last position with the given tag
     *
     * @param frag
     *            the fragment to add
     * @return String the key off the added fragment
     */
    public String insertFragment(GcFragment frag, String tag)
    {
        return this.insertFragmentAtPosition(frag, 0, tag);
    }


	/**
	 * Insert a fragment at last position
	 *
	 * @param frag
	 *            the fragment to add
	 * @return String the key off the added fragment
	 */
	public String insertFragment(GcFragment frag)
	{
		return this.insertFragmentAtPosition(frag, 0);
	}

    /**
     * Insert a fragment at the given position with the given tag
     *
     * @param frag
     *            the fragment to add
     * @param position
     *            the position at which the fragment will be added
     * @return String the key of the added fragment
     */
    public String insertFragmentAtPosition(GcFragment frag, int position, String tag)
    {
        String fragTag = tag;
        if (frag != null)
        {
            // 0 <= position <= fragmentKeys.size()
            position = (position > this.fragmentKeys.size()) ? this.fragmentKeys.size() : ((position < 0) ? 0 : position);
            if (tag == null)
            {
                fragTag = getTagForFragment(frag);
            }

            if (this.alreadyLoaded)
            {
                this.attachFragmentWithTagAtPosition(frag, fragTag, position, false);
                this.fragmentKeys.add(position, fragTag);
                this.mChildFragmentManager.executePendingTransactions();
            }
            else
            {
                this.fragmentKeys.add(position, fragTag);
                this.preLoadedFragments.add(position, frag);
            }
        }
        return fragTag;
    }

	/**
	 * Insert a fragment at the given position
	 *
	 * @param frag
	 *            the fragment to add
	 * @param position
	 *            the position at which the fragment will be added
	 * @return String the key off the added fragment
	 */
	public String insertFragmentAtPosition(GcFragment frag, int position)
	{
		String fragTag = null;
		if (frag != null)
		{
			// 0 <= position <= fragmentKeys.size()
			position = (position > this.fragmentKeys.size()) ? this.fragmentKeys.size() : ((position < 0) ? 0 : position);
			fragTag = getTagForFragment(frag);

			if (this.alreadyLoaded)
			{
				this.attachFragmentWithTagAtPosition(frag, fragTag, position, false);
				this.fragmentKeys.add(position, fragTag);
                this.mChildFragmentManager.executePendingTransactions();
			}
			else
			{
				this.fragmentKeys.add(position, fragTag);
				this.preLoadedFragments.add(position, frag);
			}
		}
		return fragTag;
	}

	/**
	 * Attach a fragment at the given position with the given tag in the view
	 *
	 * @param frag
	 *            the fragment to add
	 * @param fragTag
	 *            the fragment's tag in fragmentManager
	 * @param position
	 *            the position at which the fragment will be added
	 * @param hasToDetach
	 *            specifies if previously added fragments have to be detached/reattached
	 */
    private void attachFragmentWithTagAtPosition(GcFragment frag, String fragTag, int position, boolean hasToDetach)
    {
        if (getActivity() != null)
        {
            ArrayList<GcFragment> savedFragments = new ArrayList<GcFragment>();
            if (hasToDetach)
            {
                for (int i = position; i < this.fragmentKeys.size(); ++i)
                {
                    FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
                    GcFragment detachedFrag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(this.fragmentKeys.get(i));
                    savedFragments.add(detachedFrag);
                    ft.detach(detachedFrag).commit();
                }
            }
            FragmentTransaction ft2 = this.mChildFragmentManager.beginTransaction();
            ft2.add(parent.getId(), frag, fragTag);
            ft2.show(frag);
            ft2.commit();
            if (hasToDetach)
            {
                for (GcFragment reattachedFrag : savedFragments)
                {
                    FragmentTransaction ft3 = this.mChildFragmentManager.beginTransaction();
                    ft3.attach(reattachedFrag).commit();
                }
            }
        }
        else
        {
            Log.e(TAG, "attachFragmentWithTagAtPosition failed : Activity is null");
        }
    }

	/*
	 * _________________
	 *
	 * Fragments handling methods : remove
	 * _________________
	 */

	/**
	 * Remove the fragment at last position
	 *
	 * @return String the key off the deleted fragment
	 */
	public String removeLastFragment()
	{
		return this.removeFragmentAtPosition(this.fragmentKeys.size());
	}

	/**
	 * Remove the fragment at given position position
	 *
	 * @param position
	 *            the position at which the fragment will be removed
	 * @return String the key off the deleted fragment
	 */
	public String removeFragmentAtPosition(int position)
	{
		String fragTag = null;
		if (this.fragmentKeys != null && this.fragmentKeys.size() > 0)
		{
			// 0 <= position <= fragmentKeys.size() - 1
			position = (position >= this.fragmentKeys.size()) ? (this.fragmentKeys.size() - 1) : ((position < 0) ? 0 : position);
			fragTag = this.fragmentKeys.get(position);

			if (this.alreadyLoaded)
			{
				GcFragment fragToDelete = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragTag);
				FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
			}
			else
			{
				this.preLoadedFragments.remove(position);
			}
			this.fragmentKeys.remove(position);
		}
		return fragTag;
	}

	/**
	 * Get the fragment at given position
	 *
	 * @param position
	 *            the position of the fragment
	 * @return String the returned fragment
	 */
	public GcFragment getFragmentAtPosition(int position)
	{
	    GcFragment retFrag = null;
	    if (this.fragmentKeys != null && this.fragmentKeys.size() > 0)
	    {
	        // 0 <= position <= fragmentKeys.size() - 1
	        position = (position >= this.fragmentKeys.size()) ? (this.fragmentKeys.size() - 1) : ((position < 0) ? 0 : position);
	        String fragTag = this.fragmentKeys.get(position);

	        if (this.alreadyLoaded)
	        {
	            GcFragment fragToDelete = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragTag);
	            if (fragToDelete != null)
	            {
	                retFrag = fragToDelete;
	            }
	            else
	            {
	                Log.w(TAG, "Can't get fragment at position: [" + position + "] with Tag: [" + fragTag + "]");
	            }

	        }
	        else
	        {
	            retFrag = this.preLoadedFragments.get(position);
	        }
	    }
	    return retFrag;
	}

    /**
     * Remove fragment with given tag
     *
     * @param tag
     *            the tag of the fragment to remove
     */
    public void removeFragmentWithTag(String tag)
    {
        if (this.fragmentKeys.contains(tag))
        {
            GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(tag);
            if ((frag != null) && !frag.isHidden())
            {
                this.fragmentKeys.remove(tag);
                FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
                ft.remove(frag).commit();
                this.mChildFragmentManager.executePendingTransactions();
            }
        }
    }

	/*
	 * _________________
	 *
	 * Fragments handling methods : show
	 * _________________
	 */

	/**
	 * Show all hidden fragments
	 */
	public void showAllFragments()
	{
		for (int i = 0; i < this.fragmentKeys.size(); ++i)
		{
			this.showFragmentAtIndex(i);
		}
	}

    /**
     * Show fragment at given position
     *
     * @param position
     *            the position at which the fragment will be shown
     */
    public void showFragmentAtIndex(int position)
    {
        if (position < 0)
        {
            position = 0;
        }
        if (position > this.fragmentKeys.size())
        {
            position = this.fragmentKeys.size() - 1;
        }

        String fragmentKey = this.fragmentKeys.get(position);
        GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragmentKey);
        if ((frag != null) && frag.isHidden())
        {
            FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
            ft.show(frag).commit();
            this.mChildFragmentManager.executePendingTransactions();
        }
    }

    /**
     * Show fragment with given tag
     *
     * @param tag
     *            the tag at of the fragment to show
     */
    public void showFragmentWithTag(String tag)
    {
        if (this.fragmentKeys.contains(tag))
        {
            GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(tag);
            if ((frag != null) && frag.isHidden())
            {
                FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
                ft.show(frag).commit();
                this.mChildFragmentManager.executePendingTransactions();
            }
        }
    }

	/*
	 * _________________
	 *
	 * Fragments handling methods : hide
	 * _________________
	 */

    /**
     * Hide fragment at given position
     *
     * @param position
     *            the position at which the fragment will be hidden
     */
    public void hideFragmentAtIndex(int position)
    {
        if (position < 0)
        {
            position = 0;
        }
        if (position > this.fragmentKeys.size())
        {
            position = this.fragmentKeys.size() - 1;
        }

        String fragmentKey = this.fragmentKeys.get(position);
        GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragmentKey);
        if ((frag != null) && !frag.isHidden())
        {
            FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
            ft.hide(frag).commit();
            this.mChildFragmentManager.executePendingTransactions();
        }
    }

    /**
     * Hide fragment with given tag
     *
     * @param tag
     *            the tag of the fragment to hide
     */
    public void hideFragmentWithTag(String tag)
    {
        if (this.fragmentKeys.contains(tag))
        {
            Log.v(TAG, this+": hiding fragment with tag:"+tag);
            GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(tag);
            if ((frag != null) && !frag.isHidden())
            {
                FragmentTransaction ft = this.mChildFragmentManager.beginTransaction();
                ft.hide(frag).commit();
                this.mChildFragmentManager.executePendingTransactions();
            }
        }
    }
    
    /*
	 * _________________
	 *
	 * new feature
	 * _________________
	 */
    public void showGcFragment(Class<? extends GcFragment> fragmentClass, boolean needRemoveAll, Bundle data)
    {
    	for(String tag : fragmentKeys)
    	{
    		if(needRemoveAll)
    			removeFragmentWithTag(tag);
    		else
    			hideFragmentWithTag(tag);
    	}
    	GcFragment fragment = getFragmentOfClass(fragmentClass);
    	if(fragment == null)
    	{
    		try 
    		{
				fragment = fragmentClass.newInstance();
				fragment.setArguments(data == null ? new Bundle() : data);
				insertFragment(fragment);
			} 
    		catch (java.lang.InstantiationException e) 
    		{
				e.printStackTrace();
			} 
    		catch (IllegalAccessException e) 
    		{
				e.printStackTrace();
			}
    	}
    	else 
    	{
    		Bundle oldData = fragment.getArguments();
    		oldData.clear();
    		oldData.putAll(data);
		}
    }
    
    public void gobackToGcFragment(Class<? extends GcFragment> fragmentClass)
    {
    	Class<? extends GcFragment> callerClass = null;
    	StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
    	for(StackTraceElement element : stackTraceElements)
    	{
    		String className = element.getClassName().split("\\$")[0];
    		try 
    		{
				if(GcFragment.class.isInstance(Class.forName(className)))
				{
					callerClass = (Class<? extends GcFragment>) Class.forName(className);
					break;
				}
			} 
    		catch (ClassNotFoundException e) 
    		{
				e.printStackTrace();
			}
    	}
    	if(callerClass != null)
    	{
    		Log.d(TAG, "find caller : "+callerClass.getSimpleName());
    		//remove caller fragment
    		String callerTag = getFragmentTagOfClass(callerClass);
    		if(callerTag != null)
    			removeFragmentWithTag(callerTag);
    		showGcFragment(fragmentClass, false, null);
    	}
    }
	/*
	 * _________________
	 *
	 * Fragments handling methods : dump
	 * _________________
	 */

	/**
	 * Prints the full state of the MultiFragmentController
	 */
	public void dump()
	{
		for (int i = 0; i < this.fragmentKeys.size(); ++i)
		{
			Log.v(TAG, "Layer n°"+i);
			String fragmentKey = this.fragmentKeys.get(i);
			Log.v(TAG, "    key = " + fragmentKey);
			GcFragment frag = (GcFragment) this.mChildFragmentManager.findFragmentByTag(fragmentKey);
			Log.v(TAG, "    fragment = " + frag);
        }
    }

    public void setForceLandscape(boolean forceLandscape)
    {
        this.forceLandscape = forceLandscape;
    }

    @Override
    public void onDestroy()
    {
        Log.v(TAG, this + ": onDestroy isremoving=" + isRemoving());
        if (isRemoving())
        {
            for (int i = 0; i < this.fragmentKeys.size(); ++i)
            {
                this.removeFragmentAtPosition(i);
            }
        }
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed()
    {
        Log.v(TAG, this+": MFC onBackPressed");
        boolean consumed = false;

        // Dispatch onBackPressed
        for (int i = 0; i < this.fragmentKeys.size(); ++i)
        {
            GcFragment frag = getFragmentAtPosition(i);
            if (frag != null)
            {
                consumed |= frag.onBackPressed();
                Log.v(TAG, "onBackPressed from:"+frag+" consumed:"+consumed);
                if (consumed) break;
            }
        }

        // if consumed = true, ARActivity onBackPressed is not called.
        return consumed;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        boolean consumed = false;

        // Dispatch
        for (int i = 0; i < this.fragmentKeys.size(); ++i)
        {
            GcFragment frag = getFragmentAtPosition(i);
            if (frag != null)
            {
                consumed |= frag.dispatchKeyEvent(event);
                if (consumed) break;
            }
        }

        return consumed;
    }

    @Override
    public boolean dispatchGenericMotionEvent(MotionEvent event)
    {
        boolean consumed = false;

        // Dispatch
        for (int i = 0; i < this.fragmentKeys.size(); ++i)
        {
            GcFragment frag = getFragmentAtPosition(i);
            if (frag != null)
            {
                consumed |= frag.dispatchGenericMotionEvent(event);
                if (consumed) break;
            }
        }

        return consumed;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        boolean consumed = false;

        // Dispatch
        for (int i = 0; i < this.fragmentKeys.size(); ++i)
        {
            GcFragment frag = getFragmentAtPosition(i);
            if (frag != null)
            {
                consumed |= frag.onOptionsItemSelected(item);
                if (consumed) break;
            }
        }

        return consumed;
    }
}
