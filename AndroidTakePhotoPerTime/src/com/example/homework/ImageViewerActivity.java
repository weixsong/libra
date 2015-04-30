package com.example.homework;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

public class ImageViewerActivity extends Activity {
	private final String TAG = "com.example.homework.ImageViewerActivity";
	private String imagePath;
	private ImageView iv;
	private int viewWidth;
	private int viewHeight;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.image_view);
		
		iv = (ImageView)findViewById(R.id.imageView);
		
		
		Toast.makeText(this, "image loading", Toast.LENGTH_SHORT).show();
		imagePath = MainActivity.FOLDERPATH + this.getIntent().getStringExtra(RecordAdapter.DATA);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		// TODO	
	}
	
	@Override
	 public void onWindowFocusChanged(boolean hasFocus) {
		// TODO Auto-generated method stub
		super.onWindowFocusChanged(hasFocus);
		// Here you can get the size!
		viewWidth = iv.getWidth();
		viewHeight = iv.getHeight();
		Log.i(TAG, String.valueOf(viewWidth));
		Log.i(TAG, String.valueOf(viewHeight));
		
		// async load images
		AsyncImageLoader ail = new AsyncImageLoader(this);
		ail.execute(imagePath);
	 }
	
	private class AsyncImageLoader extends AsyncTask<String, Integer, Bitmap> {
		private Context mContext;
		
		public AsyncImageLoader(Context context) {
			mContext = context;
		}

		@Override
		protected Bitmap doInBackground(String... params) {
			Bitmap bitmap = loadImage(params[0]);
			return bitmap;
		}
		
		@Override
		protected void onPostExecute(Bitmap result) {
			iv.setImageBitmap(result);
			Toast.makeText(mContext, "image load done", Toast.LENGTH_SHORT).show();
		}
		
		private Bitmap loadImage(String filePath) {
			BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		    bmOptions.inJustDecodeBounds = true;
		    BitmapFactory.decodeFile(filePath, bmOptions);
		    int photoW = bmOptions.outWidth;
		    int photoH = bmOptions.outHeight;

		    // Determine how much to scale down the image
		    int scaleFactor = Math.min(photoW/viewWidth, photoH/viewHeight);

		    // Decode the image file into a Bitmap sized to fill the View
		    bmOptions.inJustDecodeBounds = false;
		    bmOptions.inSampleSize = scaleFactor;
		    bmOptions.inPurgeable = true;

		    Bitmap bitmap = BitmapFactory.decodeFile(filePath, bmOptions);
		    return bitmap;
		}
	}
}
