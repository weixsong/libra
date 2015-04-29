package com.example.homework;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

public class ResultViewer extends Activity {
	
	private static final String TAG = "ResultViewer";
	
	private TextView tv;
	private ImageView iv;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate");
		this.setContentView(R.layout.result_view);
		tv = (TextView) findViewById(R.id.ocr_results);
		iv = (ImageView) findViewById(R.id.cropped_image);
		
		String msg = this.getIntent().getStringExtra(MainActivity.OCR_CONTENT);
		tv.setText(msg);
		
		new AsyncImageLoader().execute(MainActivity.IMAGE_PATH);
	}

	private class AsyncImageLoader extends AsyncTask<String, Integer, Bitmap> {
		private final String TAG = "com.example.homework.AsyncLoadImages";

		private final int targetW = 500;
		private final int targetH = 500;

		@Override
		protected Bitmap doInBackground(String... arg0) {
			Log.i(TAG, "doInBackgroud");
			Bitmap b = readImage(arg0[0]);
			return b;
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			Log.i(TAG, "onPostExecute");
			iv.setImageBitmap(bitmap);
		}

		private Bitmap readImage(String file) {
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
			bmOptions.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(file, bmOptions);
			int photoW = bmOptions.outWidth;
			int photoH = bmOptions.outHeight;

			// Determine how much to scale down the image
			int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

			// Decode the image file into a Bitmap sized to fill the View
			bmOptions.inJustDecodeBounds = false;
			bmOptions.inSampleSize = scaleFactor;
			bmOptions.inPurgeable = true;

			Bitmap bitmap = BitmapFactory.decodeFile(file, bmOptions);
			return bitmap;
		}
	}
}
