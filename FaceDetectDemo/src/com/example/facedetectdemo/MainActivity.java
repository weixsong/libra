package com.example.facedetectdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;

import android.graphics.Bitmap;
import android.graphics.PointF;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;
import android.widget.RadioButton;
import android.widget.TextView;

public class MainActivity extends Activity implements PreviewCallback,
		SurfaceHolder.Callback {
	private String TAG = "sw";
	private String TAG_EVENT = "event";
	private String TAG_SURFACE = "surface";

	static {
		System.loadLibrary("opencv_java");
	}

	private ImageView mPreview;
	private ImageView mFaceRegion;
	private ImageView mFaceASM;
	private ImageView mCanny;

	private SurfaceView mSurfaceView = null;
	private SurfaceHolder mHolder = null;

	private RadioButton radioButton_java;
	private RadioButton radioButton_cpp;
	private RadioButton raidoButton_Canny;

	private RadioButton radioButton_backcamera;
	private RadioButton radioButton_frontcamera;

	private Button button_ASM;

	private View radiogroup_language_previous;
	private View radiogroup_camera_previous;

	private TextView textView_info;

	private Camera mCamera;

	private int mPreviewFrameWidth;

	private float mPreviewWeight = 5;
	private float mLinearLayoutBesidePreviewWeight = 2;

	private ImageUtils imageUtil;
	private SystemParameters mParameters;

	// cascade file
	private File dataDir = null;
	private File f_frontalface = null;
	private File f_lefteye = null;
	private File f_righteye = null;

	Mat img = new Mat();
	Mat mbmp = new Mat();
	Mat mface = new Mat();

	// used to justify if the app go to foreground by onCreate() or onResume()
	private boolean isInit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		Log.d(TAG_EVENT, "on Create");

		mParameters = new SystemParameters();

		mSurfaceView = (SurfaceView) this.findViewById(R.id.surfaceView);
		textView_info = (TextView) this.findViewById(R.id.textView_info);
		mHolder = mSurfaceView.getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

		// copy file
		if (!isDataFileInLocalDir(MainActivity.this)) {
			boolean f1, f2, f3;
			f1 = putDataFileInLocalDir(MainActivity.this,
					R.raw.haarcascade_frontalface_alt2, f_frontalface);
			f2 = putDataFileInLocalDir(MainActivity.this,
					R.raw.haarcascade_mcs_lefteye, f_lefteye);
			f3 = putDataFileInLocalDir(MainActivity.this,
					R.raw.haarcascade_mcs_righteye, f_righteye);

			if (f1 && f2 && f2) {
				textView_info.setText("load cascade file successed");
			} else {
				textView_info.setText("load cascade file failed");
			}
		}
		// init UI
		InitUI();

		// init Functions
		InitFunctions();
		isInit = true;
	}

	@Override
	public void onPreviewFrame(byte[] data, Camera camera) {
		// TODO Auto-generated method stub
		Log.d(TAG, "onPreviewFrame");

		img = imageUtil.convertBytes2Mat(data);
		mbmp = new Mat();
		Mat mface = new Mat();
		Mat mcanny = new Mat();
		Mat masm = new Mat();

		// need to be reconstructed
		if (mParameters.CodeType == CodeTypeEnum.JAVA) {
			// use java classifier
			Log.d(TAG_EVENT, "process java code");
			textView_info.setText("using JAVA code");

			mbmp = imageUtil.detectFacesAndExtractFace(img);
			mface = imageUtil.GetFaceRegion();

		} else if (mParameters.CodeType == CodeTypeEnum.CPP) {
			// use c++ classifier
			// use native c++ code to process image
			Log.d(TAG_EVENT, "process native c++ code: Face Detect");
			textView_info.setText("using native c++ code: Face Detect");

			NativeCode.FaceDetect(img.getNativeObjAddr(),
					mbmp.getNativeObjAddr());
			mbmp.copyTo(mface);
			img.copyTo(mbmp);
		} else if (mParameters.CodeType == CodeTypeEnum.CANNY) {
			// use c++ canny
			// use native c++ code to canny
			Log.d(TAG_EVENT, "process native c++ code: Canny");
			textView_info.setText("using native c++ code: Canny");

			NativeCode.DoCanny(img.getNativeObjAddr(), mbmp.getNativeObjAddr());
			mbmp.copyTo(mcanny);
		}

		Bitmap bmp = imageUtil.convertMat2Bitmap(mbmp);
		Bitmap face = imageUtil.convertMat2Bitmap(mface);
		Bitmap canny = imageUtil.convertMat2Bitmap(mcanny);
		Bitmap asm = imageUtil.convertMat2Bitmap(masm);

		if (bmp != null) {
			mPreview.setImageBitmap(bmp);
		}

		if (face != null) {
			mFaceRegion.setImageBitmap(face);
		}

		if (canny != null) {
			mCanny.setImageBitmap(canny);
		}

		if (asm != null) {
			mFaceASM.setImageBitmap(asm);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.d(TAG_EVENT, "on pause");
		isInit = false;
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onDestroy() {
		Log.d(TAG_EVENT, "on Destroy");
		super.onDestroy();
		isInit = false;
		if (mCamera != null) {
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}

	@Override
	protected void onResume() {
		Log.d(TAG_EVENT, "on resume");
		super.onResume();

		if (!isInit) {
			InitFunctions();
		}
	}

	/*
	 * inner class for click event
	 */
	class ClickEvent implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (v == radioButton_java) {
				if (v != radiogroup_language_previous) {
					Log.d(TAG_EVENT, "java selected");
					radiogroup_language_previous = v;

					mParameters.CodeType = CodeTypeEnum.JAVA;
				}
			} else if (v == radioButton_cpp) {
				if (v != radiogroup_language_previous) {
					Log.d(TAG_EVENT, "cpp selected");
					radiogroup_language_previous = v;

					mParameters.CodeType = CodeTypeEnum.CPP;
				}
			} else if (v == raidoButton_Canny) {
				if (v != radiogroup_language_previous) {
					Log.d(TAG_EVENT, "canny selected");
					radiogroup_language_previous = v;

					mParameters.CodeType = CodeTypeEnum.CANNY;
				}
			} else if (v == radioButton_backcamera) {
				if (v != radiogroup_camera_previous) {
					Log.d(TAG_EVENT, "back camera selected");
					radiogroup_camera_previous = v;

					mParameters.CameraID = Camera.CameraInfo.CAMERA_FACING_BACK;
					InitFunctions();
				}
			} else if (v == radioButton_frontcamera) {
				if (v != radiogroup_camera_previous) {
					Log.d(TAG_EVENT, "front camera selected");
					radiogroup_camera_previous = v;

					mParameters.CameraID = Camera.CameraInfo.CAMERA_FACING_FRONT;
					InitFunctions();
				}
			}
		}
	}

	private void InitUI() {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mPreview = (ImageView) findViewById(R.id.imageView_cameraPreview);
		mFaceRegion = (ImageView) findViewById(R.id.imageView_faceregion);
		mFaceASM = (ImageView) findViewById(R.id.imageView_ASM);
		mCanny = (ImageView) findViewById(R.id.imageView_canny);

		radioButton_java = (RadioButton) findViewById(R.id.radioButton_java);
		radioButton_cpp = (RadioButton) findViewById(R.id.radioButton_cpp);
		raidoButton_Canny = (RadioButton) findViewById(R.id.radioButton_canny);
		radioButton_backcamera = (RadioButton) findViewById(R.id.radioButton_backcamera);
		radioButton_frontcamera = (RadioButton) findViewById(R.id.radioButton_frontCamera);
		button_ASM = (Button) findViewById(R.id.button_ASM);

		radioButton_java.setOnClickListener(new ClickEvent());
		radioButton_cpp.setOnClickListener(new ClickEvent());
		raidoButton_Canny.setOnClickListener(new ClickEvent());
		radioButton_backcamera.setOnClickListener(new ClickEvent());
		radioButton_frontcamera.setOnClickListener(new ClickEvent());

		textView_info = (TextView) findViewById(R.id.textView_info);

		radiogroup_language_previous = radioButton_java;
		radiogroup_camera_previous = radioButton_backcamera;

		mPreview.setScaleType(ScaleType.CENTER_INSIDE);
		mFaceRegion.setScaleType(ScaleType.FIT_CENTER);
		mFaceASM.setScaleType(ScaleType.FIT_CENTER);
		mCanny.setScaleType(ScaleType.FIT_CENTER);

		button_ASM.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				textView_info.setText("Doing ASM...");

				if (img.empty() != true) {
					Mat dst = new Mat();
					Mat mat = new Mat();
					img.copyTo(mat);
					img.copyTo(dst);
					textView_info.setText("begin...");
					int[] points = NativeCode.FindFaceLandmarks(
							mat.getNativeObjAddr(), 1, 1);
					textView_info.setText("ASM DONE!");

					// handle possible error
					if ((points[0] == -1) && (points[1] == -1)) {
						Toast.makeText(MainActivity.this, "Cannot load image",
								Toast.LENGTH_LONG).show();
					} else if ((points[0] == -2) && (points[1] == -2)) {
						Toast.makeText(MainActivity.this,
								"Error in stasm_search_single!",
								Toast.LENGTH_LONG).show();
					} else if ((points[0] == -3) && (points[1] == -3)) {
						Toast.makeText(MainActivity.this,
								"No face found in input image",
								Toast.LENGTH_LONG).show();
					} else {
						Toast.makeText(MainActivity.this, "ASM DONE",
								Toast.LENGTH_LONG).show();
						double[] data = { 250, 250, 250 };
						for (int i = 0; i < points.length / 2 - 1; i++) {
							// dst.put(points[2*i+1], points[2*i], data);
							Point p1 = new Point();
							p1.x = points[2 * i];
							p1.y = points[2 * i + 1];

							Point p2 = new Point();
							p2.x = points[2 * (i + 1)];
							p2.y = points[2 * (i + 1) + 1];
							Core.line(dst, p1, p2, new Scalar(255, 255, 255), 3);
						}
						Bitmap bmp = imageUtil.convertMat2Bitmap(dst);
						mFaceASM.setImageBitmap(bmp);
					}
				}
			}
		});

		int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
		int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
		mPreviewFrameWidth = (int) (screenWidth * (mPreviewWeight / (mPreviewWeight + mLinearLayoutBesidePreviewWeight)));
	}

	private void InitFunctions() {
		SystemReset();

		mCamera = CameraUtils.getCameraInstance(this, mParameters.CameraID);
		if (mCamera != null) {
			Log.d(TAG_EVENT, "open camera successed");

			// set preview parameters
			CameraUtils.setCameraPreviewParameters(mCamera, mPreviewFrameWidth);
			Size size = mCamera.getParameters().getPreviewSize();
			int w = size.width;
			int h = size.height;

			imageUtil = new ImageUtils(w, h, mParameters, this);

			// set callback
			mCamera.stopPreview();
			mCamera.setPreviewCallback(this);

			// start preview
			mCamera.startPreview();
			textView_info.setText("Open Camera Successed");
		} else {
			if (mParameters.CameraID == Camera.CameraInfo.CAMERA_FACING_BACK) {
				Log.d(TAG_EVENT, "open back camera failed");
				textView_info.setText("Open Back Camera Failed");
			}
			if (mParameters.CameraID == Camera.CameraInfo.CAMERA_FACING_FRONT) {
				Log.d(TAG_EVENT, "open front camera failed");
				textView_info.setText("Open Front Camera Failed");
			}
		}
	}

	// reset Camera to null
	private void SystemReset() {
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(null);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
		mPreview.setImageBitmap(null);
		mFaceRegion.setImageBitmap(null);
		mFaceASM.setImageBitmap(null);
		mCanny.setImageBitmap(null);
		textView_info.setText("Resources reset successed!");
	}

	private boolean isDataFileInLocalDir(Context context) {
		boolean ret = false;
		try {
			dataDir = context.getDir("data", Context.MODE_PRIVATE);
			f_frontalface = new File(dataDir,
					"haarcascade_frontalface_alt2.xml");
			f_lefteye = new File(dataDir, "haarcascade_mcs_lefteye.xml");
			f_righteye = new File(dataDir, "haarcascade_mcs_righteye.xml");

			ret = f_frontalface.exists() && f_lefteye.exists()
					&& f_righteye.exists();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ret;
	}

	/*
	 * put raw data into local DIR
	 * /data/data/org.androidhat.stasmandroiddemo/app_data/
	 */
	private boolean putDataFileInLocalDir(Context context, int id, File f) {
		Log.d(TAG, "putDataFileInLocalDir: " + f.toString());
		try {
			InputStream is = context.getResources().openRawResource(id);
			FileOutputStream os = new FileOutputStream(f);
			byte[] buffer = new byte[4096];
			int bytesRead;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}
			is.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
			textView_info.setText("load cascade file failed");
			return false;
		}
		Log.d(TAG, "putDataFileInLocalDir: done!");
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.i(TAG_SURFACE, "SurfaceView Changed!");
		if (mHolder.getSurface() == null) {
			// preview surface does not exist
			return;
		}

		// start preview with new settings
		try {
			mCamera.setPreviewDisplay(mHolder);

		} catch (Exception e) {
			Log.d(TAG_SURFACE,
					"Error starting camera preview: " + e.getMessage());

			if (mCamera != null) {
				if (mCamera != null) {
					try {
						mCamera.setPreviewDisplay(null);
					} catch (IOException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
					mCamera.setPreviewCallback(null);
					mCamera.stopPreview();
					mCamera.release();
					mCamera = null;
				}
			}
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		Log.i(TAG_SURFACE, "SurfaceView Created!");
		if (mCamera == null) {
			InitFunctions();
		}

		try {
			mCamera.setPreviewDisplay(holder);

		} catch (IOException e) {
			Log.d(TAG_SURFACE,
					"Error setting camera preview: " + e.getMessage());
			if (mCamera != null) {
				try {
					mCamera.setPreviewDisplay(null);
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				mCamera.setPreviewCallback(null);
				mCamera.stopPreview();
				mCamera.release();
				mCamera = null;
			}
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG_SURFACE, "SurfaceView Destroy!");
		if (mCamera != null) {
			try {
				mCamera.setPreviewDisplay(null);
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}
			mCamera.setPreviewCallback(null);
			mCamera.stopPreview();
			mCamera.release();
			mCamera = null;
		}
	}
}
