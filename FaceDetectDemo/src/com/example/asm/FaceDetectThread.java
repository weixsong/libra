package com.example.asm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.core.Mat;
import org.opencv.objdetect.CascadeClassifier;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

public class FaceDetectThread extends Thread {
	private final String TAG = "com.example.asm.FaceDetectThread";

	private Context mContext;
	private Handler mHandler;

	private CascadeClassifier mCascade;

	public FaceDetectThread(Context context) {
		mContext = context;

		// init cascade
		initCascade();
	}

	public void assignTask(int id, Mat src) {

		// do face detect
		if (id == Params.DO_FACE_DETECT) {
			Message msg = new Message();
			msg.what = Params.DO_FACE_DETECT;
			msg.obj = src;
			this.mHandler.sendMessage(msg);
		}
	}

	@Override
	public void run() {
		Looper.prepare();
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == Params.DO_FACE_DETECT) {
					Mat detected = new Mat();
					Mat face = new Mat();
					Mat src = (Mat) msg.obj;
					detected = ImageUtils.detectFacesAndExtractFace(mCascade,
							src, face);

					Message uiMsg = new Message();
					uiMsg.what = Params.FACE_DETECT_DONE;
					uiMsg.obj = detected;
					// send Message to UI
					((MainActivity) mContext).mHandler.sendMessage(uiMsg);
				}
			}
		};
		Looper.loop();
	}

	/*
	 * init the cascade
	 */
	private boolean initCascade() {
		try {
			InputStream is = mContext.getResources().openRawResource(
					R.raw.haarcascade_frontalface_alt2);
			File cascadeDir = mContext.getDir("cascade", Context.MODE_PRIVATE);
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
				Log.i(TAG,
						"Loaded cascade classifier from "
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
}
