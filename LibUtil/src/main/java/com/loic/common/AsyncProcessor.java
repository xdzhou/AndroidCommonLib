package com.loic.common;

import android.support.annotation.NonNull;
import android.support.v4.util.SimpleArrayMap;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AsyncProcessor
{
    private static final String TAG = AsyncProcessor.class.getSimpleName();

    private static AsyncProcessor instance = null;

    private final ExecutorService mExecutor;

    private final SimpleArrayMap<String, Future> mRequestMap;

    public static synchronized AsyncProcessor getInstance()
    {
        if (instance == null)
        {
            synchronized (AsyncProcessor.class)
            {
                if(instance == null)
                {
                    instance = new AsyncProcessor();
                }
            }
        }
        return instance;
    }

    private AsyncProcessor()
    {
        mExecutor = Executors.newSingleThreadExecutor();
        mRequestMap = new SimpleArrayMap<>();
    }

    /**
     * push a new runnable to be run in background Thread, the previous runnable with the same token will be canceled
     * @param runnable new runnable
     * @param token runnable's token
     */
    public void pushRequest(@NonNull final Runnable runnable, @NonNull final String token)
    {
        Log.d(TAG, "push new runnable with token : "+token);
        Future<?> newFuture = instance.mExecutor.submit(new Runnable()
        {
            @Override
            public void run()
            {
                runnable.run();
                synchronized (mRequestMap)
                {
                    mRequestMap.remove(token);
                }
            }
        });

        Future<?> oldFuture = null;
        synchronized (mRequestMap)
        {
            oldFuture =mRequestMap.put(token, newFuture);
        }
        if(oldFuture != null)
        {
            oldFuture.cancel(true);
        }
    }

    public void cancelRequest(@NonNull String token)
    {
        Future<?> future = null;
        synchronized (mRequestMap)
        {
            future = mRequestMap.remove(token);
        }
        if(future != null)
        {
            Log.d(TAG, "cancel a runnable with token : "+token);
            future.cancel(true);
        }
    }
}

