package com.example.facedetectdemo;

public class NativeFaceDetect {
	static {
        System.loadLibrary("FaceDetect");
	}

	
	/*
	 * using native code to detect face
	 */
	public static native void FaceDetect(long matAddr, long matAddr_face);
}
