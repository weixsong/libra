package com.example.ndk;

public class CannyDetect {
	static {
		System.loadLibrary("native");
	}

	/**
	 * @param width
	 *            the current view width
	 * @param height
	 *            the current view height
	 */
	public static native int[] cannyDetect(int[] buf, int w, int h);
}
