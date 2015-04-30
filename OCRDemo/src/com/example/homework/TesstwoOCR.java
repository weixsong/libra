package com.example.homework;

import android.graphics.Bitmap;
import android.util.Log;

import com.googlecode.tesseract.android.TessBaseAPI;

public class TesstwoOCR {
	
	private static final String TAG = "TesstwoOCR";

	private TessBaseAPI ocr_eng;
	private TessBaseAPI ocr_chi;
	
	public TesstwoOCR() {
		Log.v(TAG, "BaseApi initializing...");

		ocr_eng = new TessBaseAPI();
		ocr_eng.setDebug(true);
		ocr_eng.init(MainActivity.DATA_PATH, MainActivity.LANG_EN);
		
		ocr_chi = new TessBaseAPI();
		ocr_chi.setDebug(true);
		ocr_chi.init(MainActivity.DATA_PATH, MainActivity.LANG_ZH);
	}

	public String doOCR(Bitmap bitmap, String lang) {
		
		String result = "";
		
		if (lang.equals(MainActivity.LANG_EN)) {
			ocr_eng.setImage(bitmap);
			result = ocr_eng.getUTF8Text();
		} else if (lang.equals(MainActivity.LANG_ZH)) {
			ocr_chi.setImage(bitmap);
			result = ocr_chi.getUTF8Text();
		} else {
			//nothing
		}

		return result.trim();
	}
}
