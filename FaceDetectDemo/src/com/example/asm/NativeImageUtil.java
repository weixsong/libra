package com.example.asm;

import org.opencv.core.Mat;
import android.util.Log;

public class NativeImageUtil {

	private final static String TAG = "com.example.asm.NativeImageUtil";

	public static Mat CannyDetect(Mat src, double threshold1,
			double threshold2, int aperatureSize) {
		Log.i(TAG, "do CannyDetect");
		Mat mat = new Mat();
		NativeCode.DoCanny(src.getNativeObjAddr(), mat.getNativeObjAddr(),
				threshold1, threshold2, aperatureSize);
		return mat;
	}

	public static Mat FaceDetect(Mat src, double scaleFactor, int minNeighbors, int minSize) {
		Log.i(TAG, "do FaceDetect");
		Mat mat = new Mat();
		NativeCode.FaceDetect(src.getNativeObjAddr(), mat.getNativeObjAddr(), scaleFactor, minNeighbors, minSize);
		return mat;
	}
	
	public static int[] FindFaceLandmarks(Mat src, float ratioW, float ratioH) {
		Log.i(TAG, "do ASM landmarks location");
		return NativeCode.FindFaceLandmarks(src.getNativeObjAddr(), ratioW, ratioH);
	}
}
