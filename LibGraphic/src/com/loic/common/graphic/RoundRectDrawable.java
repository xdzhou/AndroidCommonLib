package com.loic.common.graphic;

import android.content.res.Resources;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;

public class RoundRectDrawable extends ShapeDrawable
{
    private AutoRoundRectState state;
    private RoundRectShape shape;

    public RoundRectDrawable()
    {
        this((AutoRoundRectState) null);
    }

    private RoundRectDrawable(AutoRoundRectState s)
    {
        super();
        state = new AutoRoundRectState(s);
        initNewShape();
        setShape(shape);
    }

    public void setBackgroundColor(int color)
    {
        getPaint().setColor(color);
        invalidateSelf();
    }

    @Override
    public ConstantState getConstantState()
    {
        return state;
    }

    @Override
    protected void onBoundsChange(Rect bounds)
    {
        super.onBoundsChange(bounds);
        initNewShape();
        setShape(shape);
    }
    
    private void initNewShape()
    {
        Rect bounds = getBounds();
        
        int w = bounds.right - bounds.left;
        int h = bounds.bottom - bounds.top;
        
        int radius = Math.min(w, h) / 2;
        
        float[] outerR = new float[]{radius, radius, radius, radius, radius, radius, radius, radius};
        shape = new RoundRectShape(outerR, null, null);
    }

    final static class AutoRoundRectState extends ConstantState
    {
        AutoRoundRectState(AutoRoundRectState orig)
        {
        }

        @Override
        public Drawable newDrawable() {
            return new RoundRectDrawable(this);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new RoundRectDrawable(this);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }
    }
}
