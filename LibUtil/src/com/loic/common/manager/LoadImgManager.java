package com.loic.common.manager;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import com.loic.common.LibApplication;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.util.LruCache;

public class LoadImgManager 
{
	private static final String TAG = LoadImgManager.class.getSimpleName();
	
	private static final String urlRegex = "\\b(https?|ftp|file|ldap)://"
            + "[-A-Za-z0-9+&@#/%?=~_|!:,.;]"
            + "*[-A-Za-z0-9+&@#/%=~_|]";
	public static final String EXTERN_FOLDER_FILE_PATH;
	public static final String APP_FOLDER_FILE_PATH;
	
	private Map<String, Bitmap> drawableCache;
	private Set<String> invalideUrlList;
	private Set<String> urlInProcessList;
	private WeakReference<onDownloadImgReadyListener> listener;
	
	private int expectImgWidth = -1; //use origin dimension
	private int expectImgHeight = -1;
	
	static
	{
		APP_FOLDER_FILE_PATH = LibApplication.getAppContext().getFilesDir().getAbsolutePath();
		EXTERN_FOLDER_FILE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
	}
	
	public LoadImgManager()
	{
		this(20);
	}
	
	public LoadImgManager(int cacheSize)
	{
		if(cacheSize > 0)
			drawableCache = new HashMap<String, Bitmap>(cacheSize);
		else
			drawableCache = new HashMap<String, Bitmap>(20);
		
		this.listener = new WeakReference<onDownloadImgReadyListener>(null);
		urlInProcessList = new HashSet<String>();
		invalideUrlList = new HashSet<String>();
	}
	
	public boolean isInvalideUrl(String url)
	{
		return invalideUrlList.contains(url);
	}
	
	public Bitmap getBitmapByUrl(String urlOrPath, int customedWidth, int customedHeight)
	{
		Bitmap bitmap = drawableCache.get(urlOrPath);
		
		if(bitmap == null && !urlInProcessList.contains(urlOrPath) && !invalideUrlList.contains(urlOrPath))
		{
			urlInProcessList.add(urlOrPath);
			new LoadImgTask(customedWidth, customedHeight).execute(urlOrPath);
		}
		return bitmap;
	}
	
	public Bitmap getBitmapByUrl(String urlOrPath)
	{
		return getBitmapByUrl(urlOrPath, expectImgWidth, expectImgHeight);
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
	
	public static boolean isValideURL(String url)
	{
		boolean retVal = false;
		if(url != null)
			retVal = url.matches(urlRegex);
		
		return retVal;
	}
	
	private String getCompleteFilePath(String filePath)
	{
		if(filePath != null && !filePath.startsWith(APP_FOLDER_FILE_PATH) && !filePath.startsWith(EXTERN_FOLDER_FILE_PATH))
			return EXTERN_FOLDER_FILE_PATH + filePath;
		return filePath;
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
					String completeFilePath = getCompleteFilePath(urlOrPath);
					if(urlOrPath.toLowerCase().endsWith(".mp4"))
					{
						retVal = loadVideoThumbnail(completeFilePath, customedWidth, customedHeight);
					}
					else 
					{
				        options.inJustDecodeBounds = true;
				        BitmapFactory.decodeFile(completeFilePath, options);
				        setupBitmapSampleSize(options, customedWidth, customedHeight);
				        return BitmapFactory.decodeFile(completeFilePath, options);
					}
				}
				else // download image from Internet
				{
					Log.d(TAG, "new img download request for url: "+urlOrPath);
					try 
					{
						URL url ;
						if (isYoutubeUrl(urlOrPath))
							//url = new URL(ARImageUtils.getYoutubeVideoImgUrl(urlOrPath));
							url = new URL(urlOrPath);
						else
							url = new URL(urlOrPath);
						
						if(customedHeight == -1)
						{
							retVal = BitmapFactory.decodeStream(url.openConnection().getInputStream());
							Log.d(TAG, "use image origin size...");
						}
						else 
						{
							options.inJustDecodeBounds = true;
							BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
				            setupBitmapSampleSize(options, customedWidth, customedHeight);
				            retVal = BitmapFactory.decodeStream(url.openConnection().getInputStream(), null, options);
						}
			        }
					catch (Exception e) 
					{
			            Log.e(TAG, e.getMessage());
			            e.printStackTrace();
			        }
				}
			}
			return retVal;
		}

		@Override
		protected void onPostExecute(Bitmap result) 
		{
			if(urlOrPath != null && drawableCache != null && urlInProcessList != null)
			{
				Log.i(TAG, "Img Load request is finished for url: "+urlOrPath+" with result:"+(result!=null));
				if(result != null)
					drawableCache.put(urlOrPath, result);
				else
					invalideUrlList.add(urlOrPath);
				
				urlInProcessList.remove(urlOrPath);
				if(listener.get() != null)
					listener.get().onDownloadImgReady(urlOrPath, result);
			}
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
	
	public void setExpectImgDimension(int expectWidth, int expectHeight)
	{
		if(expectWidth > 0 && expectHeight > 0)
		{
			expectImgWidth = expectWidth;
			expectImgHeight = expectHeight;
		}
	}
	
	public void setListener(onDownloadImgReadyListener listener) 
	{
		this.listener = new WeakReference<onDownloadImgReadyListener>(listener);
	}
	
	public void dispose()
	{
		this.listener.clear();
		drawableCache.clear();
		drawableCache = null;
		urlInProcessList.clear();
		urlInProcessList = null;
	}
	
	public static interface onDownloadImgReadyListener
	{
		public boolean onDownloadImgReady(String url, Bitmap bitmap);
	}
}
