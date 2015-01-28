package com.example.asm;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.YuvImage;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 * ImageUtils used to handle Image process
 * 
 * @author wei song
 * 
 * 
 */
public class ImageUtils {
	private static String TAG = "com.example.asm.ImageUtils";

	private Context mContext;
	private CascadeClassifier mCascade;

	public ImageUtils(Context context) {
		mContext = context;
		initCascade();
	}

	public static Bitmap yuv2bitmap(byte[] data, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height,
				null);
		yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, width, height),
				100, out);
		byte[] imageBytes = out.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0,
				imageBytes.length);

		// rotate
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap dst = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return dst;
	}

	public static Bitmap mat2Bitmap(Mat data) {
		if (data.empty() == true) {
			return null;
		}

		Bitmap bmp = Bitmap.createBitmap(data.width(), data.height(),
				Bitmap.Config.ARGB_8888);
		try {
			Utils.matToBitmap(data, bmp);
		} catch (Exception e) {
			Log.e(TAG,
					"Utils.matToBitmap() throws an exception: "
							+ e.getMessage());
			bmp.recycle();
			bmp = null;
		}
		return bmp;
	}

	public Mat detectFacesAndExtractFace(Mat src, Mat face) {
		Mat ret = new Mat();
		src.copyTo(ret);

		float factor = (float) 0.3;
		Mat dst = new Mat();
		Imgproc.resize(src, dst, new Size(src.width() * factor, src.height()
				* factor));

		if (mCascade != null) {
			MatOfRect faces = new MatOfRect();
			mCascade.detectMultiScale(dst, faces,
					Params.FaceDetectParams.SCALE_FACTOR,
					Params.FaceDetectParams.MIN_NEIGHBORS, 0
							| Objdetect.CASCADE_FIND_BIGGEST_OBJECT
							| Objdetect.CASCADE_DO_ROUGH_SEARCH
							// |Objdetect.CASCADE_DO_CANNY_PRUNING
							| Objdetect.CASCADE_SCALE_IMAGE, new Size(
							Params.FaceDetectParams.MIN_SIZE,
							Params.FaceDetectParams.MIN_SIZE), new Size());

			if (faces.toArray().length > 0) {
				Log.d(TAG, "face detected");
				Rect[] rects = faces.toArray();
				for (int i = 0; i < rects.length; i++) {
					rects[i].x /= factor;
					rects[i].y /= factor;
					rects[i].width /= factor;
					rects[i].height /= factor;

					if (i == 0) {
						// copy face region
						src.submat(rects[i]).copyTo(face);
					}

					// draw rectangle on original image
					Core.rectangle(ret, rects[i].tl(), rects[i].br(),
							new Scalar(0, 255, 0, 255), 3);
				}
			}
		}
		return ret;
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
