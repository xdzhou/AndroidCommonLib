package com.loic.common;

import android.os.Handler;
import android.os.Looper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class AyncLoader
{
    private static final String TAG = AyncLoader.class.getSimpleName();

    private final Handler mUIHandler;

    private final List<LoadRequest> mPendingRequests;

    private static AyncLoader mBitmapLoaderInstance = null;

    private final ExecutorService mExecutor;

    public static synchronized AyncLoader getInstance()
    {
        if (mBitmapLoaderInstance == null)
        {
            mBitmapLoaderInstance = new AyncLoader();
        }
        return mBitmapLoaderInstance;
    }

    private AyncLoader()
    {
        mExecutor = Executors.newSingleThreadExecutor();
        mUIHandler = new Handler(Looper.getMainLooper());

        mPendingRequests = new LinkedList<LoadRequest>();
    }

    public static void runOnUIThread(Runnable runnable)
    {
        AyncLoader.getInstance().mUIHandler.post(runnable);
    }

    public static void runOnBackgroundThread(final Runnable runnable, final String token)
    {
        final AyncLoader instance = AyncLoader.getInstance();
        final LoadRequest request = new LoadRequest(token, null);
        synchronized (instance.mPendingRequests)
        {
            Iterator<LoadRequest> iter = instance.mPendingRequests.iterator();
            while (iter.hasNext())
            {
                final LoadRequest pendingRequest = iter.next();
                if (pendingRequest.id.equals(token))
                {
                    pendingRequest.task.cancel(false);
                    iter.remove();
                }
            }
            instance.mPendingRequests.add(request);
        }

        request.task = instance.mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                runnable.run();
                synchronized (instance.mPendingRequests)
                {
                    Iterator<LoadRequest> iter = instance.mPendingRequests.iterator();
                    boolean found = false;
                    while (iter.hasNext() && !found)
                    {
                        if (iter.next().equals(request))
                        {
                            found = true;
                            iter.remove();
                        }
                    }
                }
            }
        });
    }

    public static void cancelBackgroundTask(final String token)
    {
        final AyncLoader instance = AyncLoader.getInstance();
        synchronized (instance.mPendingRequests)
        {
            Iterator<LoadRequest> iter = instance.mPendingRequests.iterator();
            while (iter.hasNext())
            {
                final LoadRequest pendingRequest = iter.next();
                if (pendingRequest.id.equals(token))
                {
                    pendingRequest.task.cancel(false);
                    iter.remove();
                }
            }
        }
    }

    private static class LoadRequest
    {
        private final String id;
        private final Object clientRef;
        private Future task;

        LoadRequest(String id, Object clientRef)
        {
            this.id = id;
            this.clientRef = clientRef;
        }

    }
}

