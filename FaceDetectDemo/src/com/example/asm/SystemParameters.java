package com.example.asm;

import android.hardware.Camera;

/*
 * System parameters
 * such as native cpp code or jave detector
 * front or back camera to use
 * java = 0
 * cpp = 1
 */
public class SystemParameters {

	public int CameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
	public int CodeType = CodeTypeEnum.JAVA; // java:0  cpp:1
	public boolean DoASM = false;
	public boolean DoAlign = false;
}