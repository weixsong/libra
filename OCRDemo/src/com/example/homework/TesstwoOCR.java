package com.example.homework;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TesstwoOCR {
	
	private static final String TAG = "TesstwoOCR";
	private TessBaseAPI baseApi;
	
	public TesstwoOCR() {
		Log.v(TAG, "BaseApi initializing...");
		baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(MainActivity.DATA_PATH, MainActivity.LANG);
	}

	public String doOCR(Bitmap bitmap) {
		
		baseApi.setImage(bitmap);
		String result = baseApi.getUTF8Text();

		return result.trim();
	}
}
