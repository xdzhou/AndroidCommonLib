package com.loic.common.requestProcessor;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.webkit.MimeTypeMap;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class ImageLoadProcessor extends MultiRequestProcessor<ImageLoadRequest, ImageLoadResponse>
{

    public ImageLoadProcessor()
    {
        super(RequestProcessMode.MODE_LIFO);
    }

    private boolean isValideURL(String url)
    {
        return url != null && Patterns.WEB_URL.matcher(url).matches();
    }

    private String getMimeType(String url)
    {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null)
        {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
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
            {
                ratio = Math.round((float) origHeight / (float) customedHeight);
            }
            else
            {
                ratio = Math.round((float) origWidth / (float) customedWidth);
            }

            options.inSampleSize = ratio;
        }
        options.inJustDecodeBounds = false;
    }

    @Override
    protected ImageLoadResponse onProcessRequest(ImageLoadRequest requestToReply)
    {
        Bitmap b = null;
        if(isValideURL(requestToReply.url))
        {
            b = loadWebImage(requestToReply);
        }
        else
        {
            String mimeType = getMimeType(requestToReply.url);
            if(mimeType != null && mimeType.contains("image"))
            {
                b = loadLocalImage(requestToReply);
            }
            else if(mimeType != null && mimeType.contains("video"))
            {
                b = loadLocalVideo(requestToReply);
            }
            else
            {
                Log.e(TAG, "NO media file, mime type : "+mimeType+", url : "+requestToReply.url);
            }
        }

        return b == null ? null : new ImageLoadResponse(b);
    }

    @Override
    protected ImageLoadResponse onPostProcess(@NonNull ImageLoadRequest request, ImageLoadResponse response)
    {
        ImageLoadResponse retVal = response;
        if(response != null && (request.width < response.mBitmap.getWidth() || request.height < response.mBitmap.getHeight()))
        {
            retVal = new ImageLoadResponse(Bitmap.createScaledBitmap(response.mBitmap, request.width, request.height, false));
        }
        return retVal;
    }

    private Bitmap loadWebImage(ImageLoadRequest request)
    {
        Log.d(TAG, "new img download request for url: " + request.url);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try
        {
            URL netUrl = new URL(request.url);
            return BitmapFactory.decodeStream(netUrl.openConnection().getInputStream());
        }
        catch (MalformedURLException e)
        {
            Log.e(TAG, e.toString());
        }
        catch (IOException e)
        {
            Log.e(TAG, e.toString());
        }
        return null;
    }

    private Bitmap loadLocalImage(ImageLoadRequest request)
    {
        Log.d(TAG, "Local Image load request for file path: "+request.url);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(request.url, options);
        setupBitmapSampleSize(options, request.width, request.height);
        return BitmapFactory.decodeFile(request.url, options);
    }

    private Bitmap loadLocalVideo(ImageLoadRequest request)
    {
        Log.d(TAG, "Local video load request for file path: "+request.url);

        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();

        try
        {
            retriever.setDataSource(request.url);
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

            if (origWidth > request.width || origHeight > request.height)
            {
                float scale = (origWidth > origHeight) ? (float) request.height / (float) origHeight : (float) request.width / (float) origWidth;
                int width = Math.round(scale * origWidth);
                int height = Math.round(scale * origHeight);
                bitmap = Bitmap.createScaledBitmap(bitmap, width, height, true);
            }
        }
        return bitmap;
    }
}
