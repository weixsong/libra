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

import android.graphics.Bitmap;
import android.hardware.Camera;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.TextView;

public class MainActivity extends Activity implements PreviewCallback {
	private final String TAG = "com.example.asm.MainActivity";
	private final int BUFFER_SIZE = 4096;
	private final long DOUBLE_PRESS_INTERVAL = 500;

	static {
		System.loadLibrary("opencv_java");
	}

	private CameraPreview mPreview;
	private Camera mCamera;

	private ImageView iv_canny;
	private ImageView iv_face_detect_img_view;
	private ImageView iv_image_view_asm;
	private TextView tv_info;
	private Button btn_do_asm;

	private Mat currentFrame = new Mat();
	private Bitmap currentAsmBitmap = null;

	public Handler mHandler;
	private FaceDetectThread faceDetectThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG, "on Create");

		copyDataFile2LocalDir();

		tv_info = (TextView) findViewById(R.id.text_view_info);
		iv_canny = (ImageView) findViewById(R.id.image_view_canny);
		iv_face_detect_img_view = (ImageView) findViewById(R.id.face_detect_img_view);
		iv_image_view_asm = (ImageView) findViewById(R.id.image_view_asm);
		btn_do_asm = (Button) findViewById(R.id.btn_do_asm);

		mHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				if (msg.what == Params.FACE_DETECT_DONE) {
					Mat detected = (Mat) msg.obj;
					Bitmap face_detected_bitmap = ImageUtils
							.mat2Bitmap(detected);
					if (currentAsmBitmap != null) {
						currentAsmBitmap.recycle();
					}
					currentAsmBitmap = Bitmap.createBitmap(face_detected_bitmap);
					iv_face_detect_img_view
							.setImageBitmap(face_detected_bitmap);
				}
			}
		};

		initUI();
	}

	private void initUI() {
		btn_do_asm.setOnClickListener(new ClickEvent());
		iv_canny.setOnClickListener(new ClickEvent());
		iv_image_view_asm.setOnClickListener(new ClickEvent());
	}

	private class ClickEvent implements View.OnClickListener {

		long last = 0;
		long current = 0;

		@Override
		public void onClick(View v) {
			last = current;
			current = System.currentTimeMillis();
			boolean doubleClick = false;

			if (current - last < DOUBLE_PRESS_INTERVAL) {
				// legal double click
				Log.i(TAG, "double click");
				doubleClick = true;
			}

			if (v == iv_canny && doubleClick) {
				// start CannyActivity
				Intent intent = new Intent(MainActivity.this, CannyViewActivity.class);
				startActivity(intent);
			}
			
			if (v == iv_face_detect_img_view && doubleClick) {
				// start FaceDetectActivity
			}
			
			if (v == iv_image_view_asm && doubleClick) {
				// start AsmViewActivity
				if (currentAsmBitmap == null) {
					Toast.makeText(MainActivity.this, "no available ASM image",
							Toast.LENGTH_SHORT).show();
					return;
				}
				
				// open 
			}

			if (v == btn_do_asm) {
				// do asm
				if (currentFrame.empty()) {
					Toast.makeText(MainActivity.this, "no candidate image",
							Toast.LENGTH_SHORT).show();
					return;
				}
				findAsmLandmarks(currentFrame);
			}
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG, "on resume");
		super.onResume();

		faceDetectThread = new FaceDetectThread(this);
		faceDetectThread.start();

		// Create an instance of Camera
		mCamera = CameraUtils.getCameraInstance(this,
				Camera.CameraInfo.CAMERA_FACING_BACK);

		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(mPreview);
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG, "on pause");
		
		try {
			faceDetectThread.interrupt();
		} catch (Exception e) {
			Log.i(TAG, e.toString());
		}

		if (mPreview != null) {
			FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
			preview.removeView(mPreview);
			mPreview = null;
		}
	}

	@Override
	protected void onStop() {
		super.onPause();

		Log.d(TAG, "on stop");
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

		// do face detect in Thread
		faceDetectThread.assignTask(Params.DO_FACE_DETECT, src);
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

	private boolean isDataFileInLocalDir(File f_frontalface, File f_lefteye,
			File f_righteye) {
		boolean ret = false;
		try {
			ret = f_frontalface.exists() && f_lefteye.exists()
					&& f_righteye.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * put raw data into local DIR /data/data/com.example.asm/app_data/
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
