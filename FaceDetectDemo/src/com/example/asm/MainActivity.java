package com.example.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
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
	private Camera mCamera;
	private CascadeClassifier mCascade;
	
	private ImageView iv_canny;
	private ImageView iv_face_detect_img_view;
	private ImageView iv_image_view_asm;
	private TextView tv_info;
	private Button btn_do_asm;
	
	private Mat currentFrame = new Mat();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "on Create");
		
		// init cascade
		initCascade();
		copyDataFile2LocalDir();
		
		tv_info = (TextView) findViewById(R.id.text_view_info);
		iv_canny = (ImageView) findViewById(R.id.image_view_canny);
		iv_face_detect_img_view = (ImageView) findViewById(R.id.face_detect_img_view);
		iv_image_view_asm = (ImageView) findViewById(R.id.image_view_asm);
		btn_do_asm = (Button) findViewById(R.id.btn_do_asm);
		
		btn_do_asm.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// do asm
				if (currentFrame.empty()) {
					Toast.makeText(MainActivity.this, "no candidate image",
							Toast.LENGTH_SHORT).show();
					return;
				}
				findAsmLandmarks(currentFrame);
			}
		});
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
		Mat src = new Mat();
		Utils.bitmapToMat(bitmap, src);
		src.copyTo(currentFrame);
		
		// do canny
		Mat canny_mat = new Mat();
		Imgproc.Canny(src, canny_mat, 50, 150);
		Bitmap canny_bitmap = ImageUtils.mat2Bitmap(canny_mat);
		
		iv_canny.setImageBitmap(canny_bitmap);
		
		// do face detect
		Mat detected = new Mat();
		Mat face = new Mat();
		detected = ImageUtils.detectFacesAndExtractFace(mCascade, src, face);
		Bitmap face_detected_bitmap = ImageUtils.mat2Bitmap(detected);
		iv_face_detect_img_view.setImageBitmap(face_detected_bitmap);
	}
	
	private void findAsmLandmarks(Mat src) {
		Mat mat = new Mat();
		src.copyTo(mat);
		tv_info.setText("doing ASM....");
		new AsyncAsm(this).execute(mat);
	}
	
	private void drawAsmPoints(Mat src, List<Integer> list) {
		tv_info.setText("ASM Done.");
		Mat dst = new Mat();
		src.copyTo(dst);

		int[] points = new int[list.size()];
		for (int i = 0; i < list.size(); i++) {
			points[i] = list.get(i);
		}

		if ((points[0] == -1) && (points[1] == -1)) {
			Toast.makeText(MainActivity.this, "Cannot load image",
					Toast.LENGTH_SHORT).show();
		} else if ((points[0] == -2) && (points[1] == -2)) {
			Toast.makeText(MainActivity.this, "Error in stasm_search_single!",
					Toast.LENGTH_SHORT).show();
		} else if ((points[0] == -3) && (points[1] == -3)) {
			Toast.makeText(MainActivity.this, "No face found in input image",
					Toast.LENGTH_SHORT).show();
		} else {
			for (int i = 0; i < points.length / 2 - 1; i++) {
				Point p1 = new Point();
				p1.x = points[2 * i];
				p1.y = points[2 * i + 1];

				Point p2 = new Point();
				p2.x = points[2 * (i + 1)];
				p2.y = points[2 * (i + 1) + 1];
				Core.line(dst, p1, p2, new Scalar(255, 255, 255), 3);
			}
			Bitmap bmp = ImageUtils.mat2Bitmap(dst);
			iv_image_view_asm.setImageBitmap(bmp);
		}
	};
	
	private class AsyncAsm extends AsyncTask<Mat, Integer, List<Integer>> {
		private Context context;
		private Mat src;
		
		public AsyncAsm(Context context) {
			this.context = context;
		}

		@Override
		protected List<Integer> doInBackground(Mat... mat0) {
			List<Integer> list = new ArrayList<Integer>();
			Mat src = mat0[0];
			this.src = src;
			
			int[] points = NativeImageUtil.FindFaceLandmarks(src, 1, 1);
			for (int i = 0; i < points.length; i++) {
				list.add(points[i]);
			}
			
			return list;
		}
		
		// run on UI thread
		@Override
		protected void onPostExecute(List<Integer> list) {
			MainActivity.this.drawAsmPoints(this.src, list);
		}
	};
	
	/*
	 * init the cascade
	 */
	private boolean initCascade() {
		try {
			InputStream is = this.getResources().openRawResource(
					R.raw.haarcascade_frontalface_alt2);
			File cascadeDir = this.getDir("cascade", Context.MODE_PRIVATE);
			File cascadeFile = new File(cascadeDir,
					"haarcascade_frontalface_alt2.xml");
			FileOutputStream os = new FileOutputStream(cascadeFile);

			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();

			mCascade = new CascadeClassifier(cascadeFile.getAbsolutePath());
			if (mCascade.empty()) {
				Log.e(TAG, "Failed to load cascade classifier");
				mCascade = null;
			} else {
				Log.i(TAG, "Loaded cascade classifier from "
								+ cascadeFile.getAbsolutePath());
			}

			cascadeFile.delete();
			cascadeDir.delete();
			return true;

		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
		}
		return false;
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
