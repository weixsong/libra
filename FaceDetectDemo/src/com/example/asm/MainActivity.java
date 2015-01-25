package com.example.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends Activity implements PreviewCallback {
	private final String TAG = "com.example.asm.MainActivity";
	private final int BUFFER_SIZE = 4096;

	static {
		System.loadLibrary("opencv_java");
	}

	private CameraPreview mPreview;
	private TextView tv_info;
	private Camera mCamera;
	
	private ImageView iv_canny;
	
	private ImageUtils imageUtils;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "on Create");
		
		tv_info = (TextView) findViewById(R.id.text_view_info);
		iv_canny = (ImageView) findViewById(R.id.image_view_canny);
		
		copyDataFile2LocalDir();
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "on resume");
		super.onResume();
		
        // Create an instance of Camera
        mCamera = CameraUtils.getCameraInstance(this, Camera.CameraInfo.CAMERA_FACING_BACK);
        
        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.addView(mPreview);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "on pause");
	}
	
	@Override
	protected void onStop() {
		super.onPause();
		Log.d(TAG, "on stop");
		
		if (mPreview != null) {
        	FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        	preview.removeView(mPreview);
        	mPreview = null;
        }
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG, "on Destroy");
		super.onDestroy();
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		Log.d(TAG, "onPreviewFrame");

		Size size = camera.getParameters().getPreviewSize();
		Bitmap bitmap = ImageUtils.yuv2bitmap(data, size.width, size.height);
		iv_canny.setImageBitmap(bitmap);
	}
	
	private void copyDataFile2LocalDir() {
		try {
			File dataDir = this.getDir("data", Context.MODE_PRIVATE);
			File f_frontalface = new File(dataDir,
					"haarcascade_frontalface_alt2.xml");
			File f_lefteye = new File(dataDir, "haarcascade_mcs_lefteye.xml");
			File f_righteye = new File(dataDir, "haarcascade_mcs_righteye.xml");

			if (!isDataFileInLocalDir(f_frontalface, f_lefteye, f_righteye)) {
				boolean f1, f2, f3;

				f1 = putDataFileInLocalDir(MainActivity.this,
						R.raw.haarcascade_frontalface_alt2, f_frontalface);
				f2 = putDataFileInLocalDir(MainActivity.this,
						R.raw.haarcascade_mcs_lefteye, f_lefteye);
				f3 = putDataFileInLocalDir(MainActivity.this,
						R.raw.haarcascade_mcs_righteye, f_righteye);

				if (f1 && f2 && f3) {
					tv_info.setText("load cascade file successed");
				} else {
					tv_info.setText("load cascade file failed");
				}
			}
		} catch (IOError e) {
			e.printStackTrace();
		}
	}

	private boolean isDataFileInLocalDir(File f_frontalface, File f_lefteye, File f_righteye) {
		boolean ret = false;
		try {
			ret = f_frontalface.exists() && f_lefteye.exists() && f_righteye.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * put raw data into local DIR
	 * /data/data/com.example.asm/app_data/
	 */
	private boolean putDataFileInLocalDir(Context context, int id, File f) {
		Log.d(TAG, "putDataFileInLocalDir: " + f.toString());
		try {
			InputStream is = context.getResources().openRawResource(id);
			FileOutputStream os = new FileOutputStream(f);
			byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			tv_info.setText("load cascade file failed");
			return false;
		}
		Log.d(TAG, "putDataFileInLocalDir: done!");
		return true;
	}
}
