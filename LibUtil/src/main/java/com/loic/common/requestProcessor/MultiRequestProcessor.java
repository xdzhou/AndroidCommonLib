package com.loic.common.requestProcessor;

import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Log;
import android.support.v4.util.LruCache;

import java.util.LinkedList;
import java.util.NoSuchElementException;

public abstract class MultiRequestProcessor<T extends MultiRequestProcessor.Request, V extends MultiRequestProcessor.Response>
{
    private static HandlerThread MULTI_REQUEST_PROCESSOR_THREAD;
    private static Handler MULTI_REQUEST_PROCESSOR_HANDLER;
    private static LruCache<String, Response> MULTI_REQUEST_PROCESSOR_CACHE;

    private void checkWorkThread()
    {
        if(MULTI_REQUEST_PROCESSOR_THREAD == null)
        {
            synchronized (MultiRequestProcessor.class)
            {
                if(MULTI_REQUEST_PROCESSOR_THREAD == null)
                {
                    MULTI_REQUEST_PROCESSOR_THREAD = new HandlerThread("MultiRequestProcessor");
                    MULTI_REQUEST_PROCESSOR_THREAD.start();
                    MULTI_REQUEST_PROCESSOR_HANDLER = new Handler(MULTI_REQUEST_PROCESSOR_THREAD.getLooper());

                    int maxMemory = (int) Runtime.getRuntime().maxMemory();
                    int cacheSize = maxMemory >>> 4;
                    MULTI_REQUEST_PROCESSOR_CACHE = new LruCache<String, Response>(cacheSize)
                    {
                        @Override
                        protected int sizeOf(String key, Response value)
                        {
                            return value == null ? 0 : value.sizeOf();
                        }
                    };
                }
            }
        }
    }

    public enum RequestProcessMode
    {
        /** each time take the last request to process, ignore all the older request */
        MODE_IGNORE_OLD_REQUEST,
        /** request in queue, first in first out */
        MODE_FIFO,
        /** request in queue, last in first out */
        MODE_LIFO
    }

    protected final String TAG;
    private T mLastProcessedRequest;

    private final LinkedList<T> mRequestTodoQueue;

    private RequestProcessMode mHandleMode;

    public MultiRequestProcessor()
    {
        this(RequestProcessMode.MODE_IGNORE_OLD_REQUEST);
    }

    public MultiRequestProcessor(RequestProcessMode handleMode)
    {
        this.TAG = this.getClass().getSimpleName();

        this.mHandleMode = handleMode;

        this.mRequestTodoQueue = new LinkedList<T>();

        checkWorkThread();
    }

    @SuppressWarnings("unchecked")
    public T getLastProcessedRequest()
    {
        return (T) mLastProcessedRequest.clone();
    }

    public V postNewRequest(@NonNull T request)
    {
        V result = getCacheResponse(request);
        if(result == null)
        {
            switch (mHandleMode)
            {
                case MODE_IGNORE_OLD_REQUEST:
                    synchronized (this.mRequestTodoQueue)
                    {
                        //to ensure only the last request in the queue
                        this.mRequestTodoQueue.clear();
                        this.mRequestTodoQueue.add(request);
                    }
                    if(request.equals(mLastProcessedRequest))
                    {
                        Log.d(TAG, "postNewRequest : the request posted is current processed request : "+ mLastProcessedRequest);
                    }
                    else
                    {
                        MULTI_REQUEST_PROCESSOR_HANDLER.removeCallbacks(mUpdateRunnable);
                        MULTI_REQUEST_PROCESSOR_HANDLER.post(mUpdateRunnable);
                    }
                    break;
                case MODE_FIFO:
                    synchronized (this.mRequestTodoQueue)
                    {
                        /** FIFO mode, add request in the end of the queue */
                        this.mRequestTodoQueue.add(request);
                    }
                    MULTI_REQUEST_PROCESSOR_HANDLER.post(mUpdateRunnable);
                    break;
                case MODE_LIFO:
                    synchronized (this.mRequestTodoQueue)
                    {
                        /** LIFO mode, add request in the beginning of the queue */
                        this.mRequestTodoQueue.addFirst(request);
                    }
                    MULTI_REQUEST_PROCESSOR_HANDLER.post(mUpdateRunnable);
                    break;
            }
        }
        return onPostProcess(request, result);
    }

    @SuppressWarnings("unchecked")
    private V getCacheResponse(T request)
    {
        V result = request.isCachable() ? (V) MULTI_REQUEST_PROCESSOR_CACHE.get(request.getKey()) : null;
        return result;
    }

    private T popFirstRequest()
    {
        T request;
        synchronized (mRequestTodoQueue)
        {
            try
            {
                request = mRequestTodoQueue.pop();
            }
            catch (NoSuchElementException e)
            {
                request = null;
            }
        }
        return request;
    }

    private T getFirstRequest()
    {
        T request;
        synchronized (mRequestTodoQueue)
        {
            try
            {
                request = mRequestTodoQueue.getFirst();
            }
            catch (NoSuchElementException e)
            {
                request = null;
            }
        }
        return request;
    }

    protected void updateCurrentDoneRequest(T newRequest)
    {
        Log.d(TAG, mLastProcessedRequest + " -> " + newRequest);
        mLastProcessedRequest = newRequest;
    }

    private final Runnable mUpdateRunnable = new Runnable()
    {
        @Override
        public void run()
        {
            final T newRequest = popFirstRequest();
            if(newRequest != null && !newRequest.equals(mLastProcessedRequest))
            {
                V result = onProcessRequest(newRequest);
                if(result != null)
                {
                    updateCurrentDoneRequest(newRequest);
                    if(newRequest.isCachable())
                    {
                        MULTI_REQUEST_PROCESSOR_CACHE.put(newRequest.getKey(), result);
                    }
                }
                else
                {
                    Log.e(TAG, "Sorry, failed to process request : "+newRequest);
                }

                if(mHandleMode == RequestProcessMode.MODE_IGNORE_OLD_REQUEST && ! newRequest.equals(getFirstRequest()))
                {
                    Log.d(TAG, "last request is changed during the process of current request");
                    MULTI_REQUEST_PROCESSOR_HANDLER.removeCallbacks(this);
                    MULTI_REQUEST_PROCESSOR_HANDLER.post(this);
                }
            }
        }
    };

    public void dispose()
    {
        MULTI_REQUEST_PROCESSOR_HANDLER.removeCallbacks(mUpdateRunnable);
        synchronized (this.mRequestTodoQueue)
        {
            this.mRequestTodoQueue.clear();
        }
    }

    protected abstract V onProcessRequest(T requestToReply);

    protected V onPostProcess(T request, V response)
    {
        return response;
    }

    /**
     * General Request
     */
    public abstract static class Request<K> implements Comparable<K>, Cloneable
    {
        @Override
        public K clone()
        {
            return null;
        }

        public String getKey()
        {
            return this.getClass().getSimpleName()+"_"+this.hashCode();
        }

        @Override
        public int compareTo(K another)
        {
            return 0;
        }

        public abstract boolean isCachable();
    }

    /**
     * General Response
     */
    public static class Response
    {
        public int sizeOf()
        {
            return 1;
        }
    }
}
