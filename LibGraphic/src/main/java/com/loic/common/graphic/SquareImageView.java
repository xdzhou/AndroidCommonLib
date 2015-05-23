package com.loic.common.graphic;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class SquareImageView extends ImageView 
{
	private float heightWidthRatio = 1.0f;
	
	public SquareImageView(Context context) 
	{
		super(context);
	}
	
	public SquareImageView(Context context, AttributeSet attrs) 
	{
		super(context, attrs);
	}
	
	public float getHeightWidthRatio() 
	{
		return heightWidthRatio;
	}

	public void setHeightWidthRatio(float heightWidthRatio)
	{
		this.heightWidthRatio = heightWidthRatio;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) 
	{
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		setMeasuredDimension(getMeasuredWidth(), (int) (heightWidthRatio * getMeasuredWidth()));
	}
}
