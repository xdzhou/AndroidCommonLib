package com.loic.common.manager;

import android.graphics.Bitmap;

public abstract class LoadImgManager extends BasicManager
{
    protected LoadImgOrder loadImgOrder = LoadImgOrder.LoadImgOrder_LIFO;
    
    public Bitmap loadBitmapFor(String url)
    {
        return loadBitmapFor(url , -1, -1);
    }
    
    public abstract Bitmap loadBitmapFor(String url, int width, int height);
    
    public abstract void addListener(onLoadImgReadyListener listener);
    
    public abstract void removeListener(onLoadImgReadyListener listener);
    
    public void setLoadImgOrder(LoadImgOrder loadImgOrderEnum)
    {
        loadImgOrder = loadImgOrderEnum;
    }
    
    public enum LoadImgOrder
    {
        LoadImgOrder_FIFO, //first in first out
        LoadImgOrder_LIFO  //last in first out
    }
    
    public static interface onLoadImgReadyListener
    {
        public boolean onDownloadImgReady(String url, Bitmap bitmap);
    }
}
