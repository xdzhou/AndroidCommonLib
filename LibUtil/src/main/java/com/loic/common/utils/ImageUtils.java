package com.loic.common.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.Log;

public class ImageUtils 
{
    private static final String TAG = ImageUtils.class.getSimpleName();
    
    public static boolean saveImage(Bitmap bitmap, String filePath)
    {  
        boolean retVal = false;
        File file = new File(filePath);
        if(!file.getParentFile().isDirectory())
            file.getParentFile().mkdir();
        try 
        {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);// 把数据写入文件
            retVal = true;
            fos.close();
        } 
        catch (FileNotFoundException e) 
        {
            e.printStackTrace();
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }
        return retVal;     
    }
    
    /** 
     * 获得圆角图片的方法 
     *  
     * @param bitmap 
     * @param roundPx 一般设成14 
     * @return 
     */  
    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, float roundPx) 
    {  
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);  
        Canvas canvas = new Canvas(output);  
  
        final int color = 0xff424242;  
        final Paint paint = new Paint();  
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());  
        final RectF rectF = new RectF(rect);  
  
        paint.setAntiAlias(true);  
        canvas.drawARGB(0, 0, 0, 0);  
        paint.setColor(color);  
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);  
  
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));  
        canvas.drawBitmap(bitmap, rect, rect, paint);  
        return output;  
    } 
    
    /** 
     * 获得带倒影的图片方法 
     *  
     * @param bitmap 
     * @return 
     */  
    public static Bitmap createReflectionImage(Bitmap bitmap) 
    {  
        final int reflectionGap = 4;  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        Matrix matrix = new Matrix();  
        matrix.preScale(1, -1);  
        Bitmap reflectionImage = Bitmap.createBitmap(bitmap, 0, height / 2, width, height / 2, matrix, false);    
        Bitmap bitmapWithReflection = Bitmap.createBitmap(width, (height + height / 2), Config.ARGB_8888);  
        Canvas canvas = new Canvas(bitmapWithReflection);  
        canvas.drawBitmap(bitmap, 0, 0, null);  
        Paint deafalutPaint = new Paint();  
        canvas.drawRect(0, height, width, height + reflectionGap, deafalutPaint);  
  
        canvas.drawBitmap(reflectionImage, 0, height + reflectionGap, null);  
  
        Paint paint = new Paint();  
        LinearGradient shader = new LinearGradient(0, bitmap.getHeight(), 0,  
                bitmapWithReflection.getHeight() + reflectionGap, 0x70ffffff,  
                0x00ffffff, TileMode.CLAMP);  
        paint.setShader(shader);  
        // Set the Transfer mode to be porter duff and destination in  
        paint.setXfermode(new PorterDuffXfermode(Mode.DST_IN));  
        // Draw a rectangle using the paint with our linear gradient  
        canvas.drawRect(0, height, width, bitmapWithReflection.getHeight() + reflectionGap, paint);  
        return bitmapWithReflection;  
    }
    
    /**  
     * 将彩色图转换为灰度图  
     * @param img 位图  
     * @return  返回转换好的位图  
     */    
    public static Bitmap getGreyImage(Bitmap img) 
    {    
        int width = img.getWidth();         //获取位图的宽    
        int height = img.getHeight();       //获取位图的高    
            
        int[] pixels = new int[width * height]; //通过位图的大小创建像素点数组    
            
        img.getPixels(pixels, 0, width, 0, 0, width, height);    
        int alpha = 0xFF << 24;     
        for(int i = 0; i < height; i++)  
        {    
            for(int j = 0; j < width; j++) 
            {    
                int grey = pixels[width * i + j];    
                
                int red = ((grey  & 0x00FF0000 ) >> 16);
                int green = ((grey & 0x0000FF00) >> 8);
                int blue = (grey & 0x000000FF);    
                    
                grey = (int)((float) red * 0.3 + (float)green * 0.59 + (float)blue * 0.11);    
                grey = alpha | (grey << 16) | (grey << 8) | grey;    
                pixels[width * i + j] = grey;    
            }    
        }    
        Bitmap result = Bitmap.createBitmap(width, height, Config.RGB_565);    
        result.setPixels(pixels, 0, width, 0, 0, width, height);    
        return result;
    }
    
    public static Bitmap getGreyImageByMatrix(Bitmap bitmap) 
    { 
        // 得到图片的长和宽  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        // 创建目标灰度图像  
        Bitmap bmpGray = null;  
        bmpGray = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);  
        // 创建画布  
        Canvas c = new Canvas(bmpGray);  
        Paint paint = new Paint();  
        ColorMatrix cm = new ColorMatrix();  
        cm.setSaturation(0);  
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);  
        paint.setColorFilter(f);  
        c.drawBitmap(bitmap, 0, 0, paint);  
        return bmpGray;
    }
    
    public static Bitmap getLineGreyImage(Bitmap bitmap)   
    {  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        //创建线性拉升灰度图像  
        Bitmap linegray = null;  
        linegray = bitmap.copy(Config.ARGB_8888, true);  
        //依次循环对图像的像素进行处理  
        for (int i = 0; i < width; i++) 
        {  
            for (int j = 0; j < height; j++) 
            {  
                //得到每点的像素值  
                int col = bitmap.getPixel(i, j);  
                int alpha = col & 0xFF000000;  
                int red = (col & 0x00FF0000) >> 16;  
                int green = (col & 0x0000FF00) >> 8;  
                int blue = (col & 0x000000FF);  
                // 增加了图像的亮度  
                red = (int) (1.1 * red + 30);  
                green = (int) (1.1 * green + 30);  
                blue = (int) (1.1 * blue + 30);  
                //对图像像素越界进行处理  
                if (red >= 255)   
                    red = 255;
  
                if (green >= 255)
                    green = 255;
  
                if (blue >= 255) 
                    blue = 255;
                // 新的ARGB  
                int newColor = alpha | (red << 16) | (green << 8) | blue;  
                //设置新图像的RGB值  
                linegray.setPixel(i, j, newColor);  
            }  
        }  
        return linegray;  
    }
    
    // 该函数实现对图像进行二值化处理
    public static Bitmap getGreyBinaryImage(Bitmap bitmap, int threshold)
    {  
        //得到图形的宽度和长度  
        int width = bitmap.getWidth();  
        int height = bitmap.getHeight();  
        //创建二值化图像  
        Bitmap binarymap = null;  
        binarymap = bitmap.copy(Config.ARGB_8888, true);  
        //依次循环，对图像的像素进行处理  
        for (int i = 0; i < width; i++) 
        {  
            for (int j = 0; j < height; j++) 
            {  
                //得到当前像素的值  
                int col = binarymap.getPixel(i, j);  
                //得到alpha通道的值  
                int alpha = col & 0xFF000000;  
                //得到图像的像素RGB的值  
                int red = (col & 0x00FF0000) >> 16;  
                int green = (col & 0x0000FF00) >> 8;  
                int blue = (col & 0x000000FF);  
                // 用公式X = 0.3×R+0.59×G+0.11×B计算出X代替原来的RGB  
                int gray = (int) ((float) red * 0.3 + (float) green * 0.59 + (float) blue * 0.11);  
                //对图像进行二值化处理  
                if (gray <= threshold)  
                    gray = 0;  
                else 
                    gray = 255;
                // 新的ARGB  
                int newColor = alpha | (gray << 16) | (gray << 8) | gray;  
                //设置新图像的当前像素值  
                binarymap.setPixel(i, j, newColor);  
            }  
        }  
        return binarymap;  
    }
    
    public static List<Bitmap> splitImage(Bitmap bitmap, int xPiece, int yPiece) 
    {
        List<Bitmap> pieces = new ArrayList<Bitmap>(xPiece * yPiece);    
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();    
        int pieceWidth = width / 3;    
        int pieceHeight = height / 3;    
        for (int i = 0; i < yPiece; i++) 
        {    
            for (int j = 0; j < xPiece; j++) 
            { 
                int xValue = j * pieceWidth;    
                int yValue = i * pieceHeight;    
                Bitmap pieceBitmap = Bitmap.createBitmap(bitmap, xValue, yValue, pieceWidth, pieceHeight);    
                pieces.add(pieceBitmap);  
            }    
        }   
        return pieces;    
    }
    
    public static Bitmap getImageFromUrl(String url) 
    {  
        Bitmap image = null;
        try 
        {
            InputStream in = new java.net.URL(url).openStream();
            image = BitmapFactory.decodeStream(in);
        } 
        catch (Exception e) 
        {
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
        return image; 
    }
    
    /** 
    * 图片透明度处理 
    * @param sourceImg  原始图片 
    * @param number 透明度 
    * @return 
    */  
    public static Bitmap setImageAlpha(Bitmap sourceImg, int alpha) 
    {  
        int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
        // 获得图片的ARGB值  
        sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,sourceImg.getWidth(), sourceImg.getHeight());        
        alpha = alpha * 255 / 100;  
        for (int i = 0; i < argb.length; i++) 
        {  
            argb[i] = (alpha << 24) | (argb[i] & 0x00FFFFFF);// 修改最高2位的值  
        }  
        sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(), sourceImg.getHeight(), Config.ARGB_8888);  
        return sourceImg;  
    } 
}
