package com.example.facedetectdemo;

public class NativeCode {
	static {
        System.loadLibrary("Native");
	}
	
	/*
	 * Canny edge detect
	 */
	public static native void DoCanny(long matAddr_src, long matAddr_dst);
	
	/*
	 * do face detect
	 */
	public static native void FaceDetect(long matAddr_src, long matAddr_dst);
	
	/*
	 * do ASM
	 * find landmarks
	 */
	public static native int[] FindFaceLandmarks(long matAddr, float ratioW, float ratioH);
}