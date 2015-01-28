package com.example.asm;

public class NativeCode {
	static {
        System.loadLibrary("Native");
	}
	
	/*
	 * Canny edge detect
	 * threshold1 = 50
	 * threshold2 = 150
	 * aperatureSize = 3
	 */
	public static native void DoCanny(long matAddr_src, long matAddr_dst, double threshold1,
			double threshold2, int aperatureSize);
	
	/*
	 * do face detect
	 * scaleFactor = 1.1
	 * minNeighbors = 2
	 * minSize = 30 (30 * 30)
	 */
	public static native void FaceDetect(long matAddr_src, long matAddr_dst,
			double scaleFactor, int minNeighbors, int minSize);
	
	/*
	 * do ASM
	 * find landmarks
	 */
	public static native int[] FindFaceLandmarks(long matAddr, float ratioW, float ratioH);
}