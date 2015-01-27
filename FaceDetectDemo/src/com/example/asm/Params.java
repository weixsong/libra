package com.example.asm;

import android.os.Environment;

public class Params {
	public static int DO_FACE_DETECT = 0;
	public static int FACE_DETECT_DONE = 1;
	
	public static class CameraPreview {
		public static final int PREVIEW_DEGREE = 90;
	}
	
	public static class CannyParams {
		public static int THRESHOLD1 = 50;
		public static int THRESHOLD2 = 150;
	}
	
	public static class FaceDetectParams {
		public static double SCALE_FACTOR = 1.2;
		public static int MIN_NEIGHBORS = 3;
		public static int MIN_SIZE = 30;
	}
	
	public static class ASMError {
		public static int BAD_INPUT = -1;
		public static int INIT_FAIL = -2;
		public static int NO_FACE_FOUND = -3;
	}
	
	public static class ASMActivity {
		public static String BITAMP = "bitmap";
		public static String LOCAL_FILE = Environment.getExternalStorageDirectory()
				.getAbsolutePath().toString() + "/asm.png";
	}
}
