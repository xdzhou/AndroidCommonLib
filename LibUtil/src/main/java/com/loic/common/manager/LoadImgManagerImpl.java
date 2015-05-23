package com.loic.common.manager;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.util.Patterns;

public class LoadImgManagerImpl extends LoadImgManager
{
	private static final String TAG = LoadImgManagerImpl.class.getSimpleName();

	private static final int maxAsyncTaskNum = 15;
	
	private Set<onLoadImgReadyListener> listeners;
	//loaded urls
	private LruCache<String, Bitmap> loadedBitmaps;
	//wait for loading 
	private List<RequestInfo> inWaitUrlStack;
	//url in loading
	private List<String> loadingUrlList;
	
	public LoadImgManagerImpl()
	{
		int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
		int cacheSize = maxMemory >>> 4;
		loadedBitmaps = new LruCache<String, Bitmap>(cacheSize)
		{
			@Override  
	        protected int sizeOf(String key, Bitmap bitmap) 
			{
	            return bitmap == null ? 0 : bitmap.getByteCount() / 1024;  
	        }
		};
		
		this.listeners = new HashSet<onLoadImgReadyListener>();
		loadingUrlList = new ArrayList<String>(maxAsyncTaskNum);
		
		inWaitUrlStack = new ArrayList<RequestInfo>();
	}
	
	private static class RequestInfo
	{
		String url;
		int width;
		int height;
		
		public RequestInfo(String url, int width, int height) 
		{
			this.url = url;
			this.width = width;
			this.height = height;
		}
	}
	
	@Override
	public void addListener(onLoadImgReadyListener listener)
	{
		if(listener != null)
			listeners.add(listener);
	}
	
	@Override
	public void removeListener(onLoadImgReadyListener listener)
	{
		if(listener != null)
			listeners.remove(listener);
	}
	
	private void dispatchImgLoadedEvent(String url, Bitmap bitmap)
	{
		for(onLoadImgReadyListener listener : listeners)
		{
			listener.onDownloadImgReady(url, bitmap);
		}
	}
	
	@Override
	public Bitmap loadBitmapFor(String urlOrPath, int customedWidth, int customedHeight)
	{
		Bitmap bitmap = loadedBitmaps.get(urlOrPath);
		
		if(bitmap == null && !loadingUrlList.contains(urlOrPath))
		{
			int index = findUrlIndexInWaitList(urlOrPath);
			if(index >= 0)
			{
				Log.d(TAG, "request url : "+urlOrPath+" in wainting list...");
				RequestInfo requestInfo = inWaitUrlStack.remove(index);
				inWaitUrlStack.add(requestInfo);
			} else 
			{
				if(! isValideURL(urlOrPath) && ! new File(urlOrPath).exists())
				{
					Log.e(TAG, "not support URL or Path : "+urlOrPath);
				} else 
				{
					inWaitUrlStack.add(new RequestInfo(urlOrPath, customedWidth, customedHeight));
				}
			}
			startLoadImg();
		}
		return bitmap;
	}
	
	private void startLoadImg()
	{
		if(! inWaitUrlStack.isEmpty() && loadingUrlList.size() < maxAsyncTaskNum)
		{
			int index = loadImgOrder == LoadImgOrder.LoadImgOrder_FIFO ? 0 : inWaitUrlStack.size() - 1;
			RequestInfo requestInfo = inWaitUrlStack.remove(index);
			new LoadImgTask(requestInfo.width, requestInfo.height).execute(requestInfo.url);
			loadingUrlList.add(requestInfo.url);
		}
	}
	
	private int findUrlIndexInWaitList(String url)
	{
		int index = -1;
		for(int i = 0; i < inWaitUrlStack.size(); i++)
		{
			if(inWaitUrlStack.get(i).url.equals(url))
			{
				index = i;
				break;
			}
		}
		return index;
	}
	
	public static boolean isYoutubeUrl(String url)
	{
		boolean retVal = false;
		if(url != null && !url.isEmpty())
		{
			retVal = url.startsWith("http://www.youtube.com") || url.startsWith("https://www.youtube.com") || url.startsWith("www.youtube.com");
		}
		return retVal;
	}
	
	private boolean isValideURL(String url)
	{
		boolean retVal = false;
		if(url != null)
			retVal = Patterns.WEB_URL.matcher(url).matches();
		
		return retVal;
	}
	
	private String tryGetYouTubeThumbnailUrl(String url)
	{
		String thumbnail = null;
		if(isValideURL(url) && (url.startsWith("http://www.youtube.com") || url.startsWith("https://www.youtube.com") || url.startsWith("www.youtube.com")))
		{
			int start = url.indexOf("?v=");
			if(start > 0)
			{
				thumbnail = "http://img.youtube.com/vi/"+url.substring(start, url.length())+"/0.jpg";
			}
		}
		return thumbnail;
	}

	/****************************************************************/
	/**************************Async Task****************************/
	/****************************************************************/
	
	private class LoadImgTask extends AsyncTask<String, Void, Bitmap>
	{
		private int customedWidth;
		private int customedHeight;
		private String urlOrPath;

		public LoadImgTask(int customedWidth, int customedHeight) 
		{
			this.customedWidth = customedWidth;
			this.customedHeight = customedHeight;
		}

		@Override
		protected Bitmap doInBackground(String... params) 
		{
			Bitmap retVal = null;
			if(params != null && params.length > 0)
			{
				urlOrPath = params[0];
				BitmapFactory.Options options = new BitmapFactory.Options();

				if(!isValideURL(urlOrPath)) // load image from SD card
				{
					Log.d(TAG, "Local Img load request for file path: "+urlOrPath);
					
					if(urlOrPath.toLowerCase(Locale.US).endsWith(".mp4"))
					{
						retVal = loadVideoThumbnail(urlOrPath, customedWidth, customedHeight);
					}
					else 
					{
				        options.inJustDecodeBounds = true;
				        BitmapFactory.decodeFile(urlOrPath, options);
				        setupBitmapSampleSize(options, customedWidth, customedHeight);
				        return BitmapFactory.decodeFile(urlOrPath, options);
					}
				} else // download image from Internet
				{
					Log.d(TAG, "new img download request for url: "+urlOrPath);
					try 
					{
						URL url ;
						String realImgUrl = tryGetYouTubeThumbnailUrl(urlOrPath);
						if (realImgUrl != null)
						{
							url = new URL(realImgUrl);
						} else
						{
							url = new URL(urlOrPath);
						}
						
						if(customedHeight == -1)
						{
							retVal = BitmapFactory.decodeStream(url.openConnection().getInputStream());
							Log.d(TAG, "use image origin size...");
						} else 
						{
							options.inJustDecodeBounds = true;
							BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
				            setupBitmapSampleSize(options, customedWidth, customedHeight);
				            retVal = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
						}
			        }
					catch (Exception e) 
					{
			            e.printStackTrace();
			        }
				}
			}
			return retVal;
		}

		@Override
		protected void onPostExecute(Bitmap result) 
		{
			if(urlOrPath != null)
			{
				Log.i(TAG, "Img Load request is finished for url: "+urlOrPath+" with result:"+(result!=null));
				if(result != null)
					loadedBitmaps.put(urlOrPath, result);
				
				loadingUrlList.remove(urlOrPath);
				dispatchImgLoadedEvent(urlOrPath, result);
			}
			startLoadImg();
		}
	}
	
	/**
     * Loads a thumbnail out of a video. The thumbnail is downscaled if necessary.
     * @param path Video path
     * @return Requested bitmap or null if video cannot be decoded
     */
	@SuppressLint("NewApi")
	private Bitmap loadVideoThumbnail(String path, int customedWidth, int customedHeight) 
	{
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        
        try 
        {
            retriever.setDataSource(path);
            bitmap = retriever.getFrameAtTime();
        } 
        catch (IllegalArgumentException e) {} // unreadable file
        catch (RuntimeException e) {} // unreadable file
        finally 
        {
            try {
                retriever.release();
            }
            catch (RuntimeException e) {} // Ignore at cleanup
        }
        
        if (bitmap != null) 
        {
            // Thumbnail is loaded from the Android retriever; it has to be downscaled
            int origWidth = bitmap.getWidth();
            int origHeight = bitmap.getHeight();
            
            if (origWidth > customedWidth || origHeight > customedHeight) 
            {
                float scale;
                if (origWidth > origHeight) {
                    scale = (float) customedHeight / (float) origHeight;
                } else {
                    scale = (float) customedWidth / (float) origWidth;
                }
                int width = Math.round(scale * origWidth);
                int height = Math.round(scale * origHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
        }
        return bitmap;
    }
	
	/**
     * Loads a bitmap to be shown on a thumbnail. Takes care of subsampling the image being loaded when possible
     * in order to optimize memory usage.
     * @param data Byte array containing bitmap data
     * @return Requested bitmap
     */
    private Bitmap loadThumbnailBitmap(byte[] data, int customedWidth, int customedHeight) 
    {
        Bitmap result = null;
        if (data != null) 
        {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(data, 0, data.length);
            setupBitmapSampleSize(options, customedWidth, customedHeight);
            result = BitmapFactory.decodeByteArray(data, 0, data.length);
        }
        return result;
    }
	
	private void setupBitmapSampleSize(BitmapFactory.Options options, int customedWidth, int customedHeight) 
	{
        int origWidth = options.outWidth;
        int origHeight = options.outHeight;
        
        // Get a subsampled image if possible in order to save memory
        if (origWidth > customedWidth || origHeight > customedHeight) 
        {
            int ratio;
            if (origWidth > origHeight)
                ratio = Math.round((float) origHeight / (float) customedHeight);
            else
                ratio = Math.round((float) origWidth / (float) customedWidth);
            
            options.inSampleSize = ratio;
        }
        options.inJustDecodeBounds = false;
    }
	
	@Override
	public void dispose()
	{
		if(listeners != null)
			listeners.clear();
		if(loadedBitmaps != null)
			loadedBitmaps.evictAll();
		if(inWaitUrlStack != null)
			inWaitUrlStack.clear();
		if(loadingUrlList != null)
			loadingUrlList.clear();
	}
}
