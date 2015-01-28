package com.example.asm;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {
	private final String TAG = "com.example.asm.CameraPreview";

	private SurfaceHolder mHolder;
	private Camera mCamera;

	private Context context;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		mCamera = camera;
		this.context = context;

		// Install a SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		mHolder = getHolder();
		mHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void surfaceCreated(SurfaceHolder holder) {
		// The Surface has been created, now tell the camera where to draw the
		// preview.
		Log.d(TAG, "SurfaceView Created!");
		try {
			mCamera.setPreviewDisplay(holder);
			mCamera.setDisplayOrientation(Params.CameraPreview.PREVIEW_DEGREE);
			mCamera.startPreview();
		} catch (Exception e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			if (mCamera != null) {
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "SurfaceView Destroy!");
		if (mCamera != null) {
			try {
				mCamera.setPreviewCallback(null);
				mCamera.setPreviewDisplay(null);
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		// If your preview can change or rotate, take care of those events here.
		// Make sure to stop the preview before resizing or reformatting it.
		Log.i(TAG, "SurfaceView Changed!");
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		try {
			mCamera.stopPreview();
		} catch (Exception e) {
			// ignore: tried to stop a non-existent preview
			Log.d(TAG, "Error stop camera preview: " + e.getMessage());
		}

		// set preview size and make any resize, rotate or
		// reformatting changes here
		CameraUtils.setCameraPreviewParameters(mCamera, w);

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);
			mCamera.setDisplayOrientation(Params.CameraPreview.PREVIEW_DEGREE);
			// set preview callback
			mCamera.setPreviewCallback((PreviewCallback) context);
			mCamera.startPreview();

		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			if (mCamera != null) {
				try {
					mCamera.setPreviewDisplay(null);
				} catch (Exception e2) {
					e2.printStackTrace();
				}
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	/* this will make the preview normal size, but could not center the preview
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		final int width = resolveSize(getSuggestedMinimumWidth(),
				widthMeasureSpec);
		final int height = resolveSize(getSuggestedMinimumHeight(),
				heightMeasureSpec);
		
		Size mPreviewSize = mCamera.getParameters().getPreviewSize();

		float ratio;
		if (mPreviewSize.height >= mPreviewSize.width)
			ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
		else
			ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

		// One of these methods should be used, second method squishes preview
		// slightly
		setMeasuredDimension(width, (int) (width * ratio));
	}
	*/
}
