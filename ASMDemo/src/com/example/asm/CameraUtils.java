package com.example.asm;

import java.util.List;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.util.Log;

/**
 * Camera Utility for open camera, check camera, set camera parameters
 * 
 * @author weixsong
 */
public class CameraUtils {
	private static String TAG = "com.example.asm.CameraUtils";
	private static final double RATIO_TOLERANCE = 0.1;

	/**
	 * A safe way to get an instance of the Camera object.
	 */
	public static Camera getCameraInstance(Context context, int CameraId) {
		Camera c = null;
		if (!checkCameraHardware(context)) {
			return c; // device has no camera
		}

		try {
			c = Camera.open(CameraId); // attempt to get a Camera instance
			Log.d(TAG, "open camera succeed");
		} catch (Exception e) {
			// Camera is not available (in use or does not exist)
			Log.d(TAG, "open camera failed");
		}
		return c; // returns null if camera is unavailable
	}

	/** Check if this device has a camera */
	private static boolean checkCameraHardware(Context context) {
		if (context.getPackageManager().hasSystemFeature(
				PackageManager.FEATURE_CAMERA)) {
			// this device has a camera
			return true;
		} else {
			// no camera on this device
			Log.d(TAG, "no camera on this device");
			return false;
		}
	}

	/*
	 * Set Camera Preview parameters to fit the ImageView Size for we need to
	 * rotate the captured image by 90 degrees, so the passed in parameter width
	 * is used as height no fault. Be noticed.
	 */
	public static boolean setOptimalCameraPreviewSize(Camera camera, int width, int height) {
		if (camera == null) {
			return false;
		}

		double targetRatio = (double) height / width;

		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> sizes = params.getSupportedPreviewSizes();

		Camera.Size optimalSize = null;

		// selecting optimal camera preview size
		int minDiff = Integer.MAX_VALUE;
		for (Camera.Size size : sizes) {
			double ratio = (double) size.height / size.width;
			if (Math.abs(ratio - targetRatio) > RATIO_TOLERANCE) continue;
			if (Math.abs(size.height - height) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - height);
			}
		}

        if (optimalSize == null) {
            minDiff = Integer.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }

		params.setPreviewSize(optimalSize.width, optimalSize.height);

		List<String> focusModes = params.getSupportedFocusModes();
		if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
			params.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
		}

		// for Mi3, only support ImageFormat.NV21
		// for most cameras, ImageFormat.NV21 are support worldwide
		// so use default preview format
		// params.setPreviewFormat(ImageFormat.JPEG);
		camera.setParameters(params);
		return true;
	}
	
	public static Camera.Size getOptimalCameraPreviewSize(Camera camera, int width, int height) {
		if (camera == null) {
			return null;
		}

		double targetRatio = (double) height / width;

		Camera.Parameters params = camera.getParameters();
		List<Camera.Size> sizes = params.getSupportedPreviewSizes();

		Camera.Size optimalSize = null;

		// selecting optimal camera preview size
		int minDiff = Integer.MAX_VALUE;
		for (Camera.Size size : sizes) {
			double ratio = (double) size.height / size.width;
			if (Math.abs(ratio - targetRatio) > RATIO_TOLERANCE) continue;
			if (Math.abs(size.height - height) < minDiff) {
				optimalSize = size;
				minDiff = Math.abs(size.height - height);
			}
		}

        if (optimalSize == null) {
            minDiff = Integer.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - height) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - height);
                }
            }
        }
		return optimalSize;
	}
}
