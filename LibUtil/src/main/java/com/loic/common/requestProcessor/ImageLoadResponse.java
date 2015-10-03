package com.loic.common.requestProcessor;

import android.graphics.Bitmap;

public class ImageLoadResponse extends MultiRequestProcessor.Response
{
    public final Bitmap mBitmap;

    public ImageLoadResponse(Bitmap mBitmap)
    {
        this.mBitmap = mBitmap;
    }

    public int sizeOf()
    {
        return mBitmap.getByteCount();
    }
}
