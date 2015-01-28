package com.example.asm;

import org.opencv.core.Mat;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

public class FaceDetectThread extends Thread {
	private final String TAG = "com.example.asm.FaceDetectThread";

	private Context mContext;
	private Handler mHandler;

	private ImageUtils imageUtils;

	public FaceDetectThread(Context context) {
		mContext = context;
		imageUtils = new ImageUtils(context);
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
					detected = imageUtils.detectFacesAndExtractFace(src, face);

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
}
