package com.loic.common;

import java.io.IOException;
import java.util.Collection;

import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;

public class CameraController 
{
	private static final String TAG = CameraController.class.getSimpleName();
	private Camera camera;
	
	public CameraController()
	{
		
	}
	
	public boolean openDriver(SurfaceHolder holder)
	{
		boolean retVal = false;
		if(camera == null)
			camera = Camera.open();
		
		if(camera != null)
		{
			try 
			{
				camera.setPreviewDisplay(holder);
				retVal = true;
			} 
			catch (IOException e) 
			{
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
		}
		return retVal;
	}
	
	public void closeDriver()
	{
		if(camera != null)
		{
			camera.cancelAutoFocus();
			camera.release();
			camera = null;
		}
	}

	public boolean startAutoFocus()
	{
		boolean retVal = false;
		if(camera != null)
		{
			//camera.autoFocus(this);
		}
		return retVal;
	}
	
	public boolean setFocusMode()
	{
		boolean retVal = false;
		if(camera != null)
		{
			String focusMode = findSettableValue(camera.getParameters().getSupportedFocusModes(), 
					"continuous-video", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO in 4.0+
					"continuous-picture", // Camera.Paramters.FOCUS_MODE_CONTINUOUS_PICTURE in 4.0+
					Camera.Parameters.FOCUS_MODE_AUTO,
					Camera.Parameters.FOCUS_MODE_MACRO,
					Camera.Parameters.FOCUS_MODE_EDOF);
			if(focusMode != null)
			{
				Camera.Parameters parameters = camera.getParameters();
				parameters.setFocusMode(focusMode);
				camera.setParameters(parameters);
				retVal = true;
			}
		}
		return retVal;
	}
	
	private String findSettableValue(Collection<String> supportedValues, String... desiredValues)
	{
		String retVal = null;
		if(supportedValues != null)
		{
			for(String value : desiredValues)
			{
				if(supportedValues.contains(value))
				{
					retVal = value;
					break;
				}
			}
		}
		return retVal;
	}
}
