package com.example.homework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

public class AsyncImageLoader extends AsyncTask<String, Integer, List<Map<String, Object>>> {
	private final String TAG = "com.example.homework.AsyncLoadImages";
	private Context mContext;
	private RecordAdapter mAdapter;
	private int targetW;
	private int targetH;
	
	// scale image to small size
	public AsyncImageLoader(Context context, RecordAdapter adapter, int scaledWidth, int scaledHeight) {
		mContext = context;
		mAdapter = adapter;
		targetW = scaledWidth;
		targetH = scaledHeight;
	}

	@Override
	protected List<Map<String, Object>> doInBackground(String... arg0) {
		// TODO Auto-generated method stub
		Log.i(TAG, "doInBackgroud");
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		
		File dir = new File(arg0[0]);
		if (dir.isDirectory()) {
			// load all images in given directory
			File[] files = dir.listFiles();
			for (File image : files) {
				if (!image.isFile()) {
					continue;
				}
				
				Map<String, Object> map = new TreeMap<String, Object>();
				Bitmap bitmap = readImage(image.getAbsolutePath());
				map.put(RecordAdapter.KEY_BITMAP, bitmap);
				
				String fileName = image.getAbsolutePath();
				int start = fileName.lastIndexOf("/");
				String name = fileName.substring(start + 1);
				map.put(RecordAdapter.KEY_NAME, name);
				
				list.add(map);
				
				// could not modify UI Thread
				// this will cause Exception
				// mAdapter.addRecord(map);
			}
		} else {
			// load image with given file path and name
			Map<String, Object> map = new TreeMap<String, Object>();
			Bitmap bitmap = readImage(dir.getAbsolutePath());
			map.put(RecordAdapter.KEY_BITMAP, bitmap);
			
			String fileName = dir.getAbsolutePath();
			int start = fileName.lastIndexOf("/");
			String name = fileName.substring(start + 1);
			map.put(RecordAdapter.KEY_NAME, name);
			
			list.add(map);
		}
		
		return list;
	}

	@Override
	protected void onPostExecute(List<Map<String, Object>> list) {
		//
		Log.i(TAG, "onPostExecute");
		for (Map<String, Object> map : list) {
			mAdapter.addRecord(map);
		}
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
