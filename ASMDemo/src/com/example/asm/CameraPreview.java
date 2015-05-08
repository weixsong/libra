package com.example.asm;

import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements
		SurfaceHolder.Callback {

	private final String TAG = "com.example.asm.CameraPreview";

	private SurfaceHolder surfaceHolder;
	private Camera camera;

	private Context context;

	public CameraPreview(Context context, Camera camera) {
		super(context);
		this.camera = camera;
		this.context = context;

		// add SurfaceHolder.Callback so we get notified when the
		// underlying surface is created and destroyed.
		surfaceHolder = getHolder();
		surfaceHolder.addCallback(this);
		// deprecated setting, but required on Android versions prior to 3.0
		surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/*
	 * SurfaceView has been created, now tell the camera where to draw the
	 * preview.
	 */
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, "SurfaceView Created!");
		try {
			camera.setPreviewDisplay(holder);
			setCameraDisplayOrientation(camera);
			camera.startPreview();
		} catch (Exception e) {
			Log.d(TAG, "Error setting camera preview: " + e.getMessage());
			releaseCamera(camera);
		}
	}

	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, "SurfaceView Destroy!");
		releaseCamera(camera);
	}

	/*
	 * If your preview can change or rotate, take care of those events here.
	 * Make sure to stop the preview before resizing or reformatting it.
	 */
	public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
		Log.i(TAG, "SurfaceView Changed!");
		if (surfaceHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// stop preview before making changes
		camera.stopPreview();

		// set preview size and make any resize, rotate or
		// reformatting changes here
		CameraUtils.setOptimalCameraPreviewSize(camera, h, w);

		// start preview with new settings
		try {
			camera.setPreviewDisplay(surfaceHolder);
			setCameraDisplayOrientation(camera);
			// set preview callback
			camera.setPreviewCallback((PreviewCallback) context);
			camera.startPreview();
		} catch (Exception e) {
			Log.d(TAG, "Error starting camera preview: " + e.getMessage());
			releaseCamera(camera);
		}
	}

	private void setCameraDisplayOrientation(Camera camera) {
		int orientation = this.getResources().getConfiguration().orientation;
		this.getResources().getConfiguration();
		if (orientation == Configuration.ORIENTATION_PORTRAIT) {
			camera.setDisplayOrientation(Params.CameraPreview.PREVIEW_DEGREE);
		} else {
			camera.setDisplayOrientation(0);
		}
	}

	private void releaseCamera(Camera camera) {
		if (camera != null) {
			try {
				camera.setPreviewDisplay(null);
			} catch (Exception e2) {
				e2.printStackTrace();
			} finally {
				camera.setPreviewCallback(null);
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		}
	}

	/*
	 * this will make the preview normal size, but could not center the preview
	 */
//	@Override
//	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		Log.d(TAG, "onMeasure");
//		final int width = resolveSize(getSuggestedMinimumWidth(),
//				widthMeasureSpec);
//		final int height = resolveSize(getSuggestedMinimumHeight(),
//				heightMeasureSpec);
//
//		Camera.Size previewSize = camera.getParameters().getPreviewSize();
//
//		float ratio;
//		if (previewSize.height >= previewSize.width) {
//			ratio = (float) previewSize.height / (float) previewSize.width;
//			setMeasuredDimension(width, (int) (width * ratio));
//		} else {
//			ratio = (float) previewSize.width / (float) previewSize.height;
//			setMeasuredDimension((int) (width * ratio), height);
//		}
//	}
}
