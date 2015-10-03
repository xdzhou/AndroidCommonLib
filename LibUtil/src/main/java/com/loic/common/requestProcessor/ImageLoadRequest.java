package com.loic.common.requestProcessor;

public class ImageLoadRequest extends MultiRequestProcessor.Request<ImageLoadRequest>
{
    public final String url;
    public final int height;
    public final int width;

    public ImageLoadRequest(String url, int height, int width)
    {
        this.url = url;
        this.height = height;
        this.width = width;
    }

    @Override
    public ImageLoadRequest clone()
    {
        return new ImageLoadRequest(this.url, this.height, this.width);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ImageLoadRequest that = (ImageLoadRequest) o;

        return url.equals(that.url);

    }

    @Override
    public int hashCode()
    {
        return url.hashCode();
    }

    @Override
    public boolean isCachable()
    {
        return true;
    }
}
