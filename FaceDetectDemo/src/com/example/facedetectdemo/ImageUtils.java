package com.example.facedetectdemo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.CvType;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

/**
 * ImageUtils used to handle Image process
 * @author wei song
 * 
 *
 */
public class ImageUtils {
	private String TAG = "sw";
	
	private int previewWidth;
	private int previewHeight;
	
	private Mat mYuv;
	private Mat mRgba;
	private Mat mFaceRegion;
	
	private SystemParameters mParameters;
	
	private CascadeClassifier   mCascade;
	private boolean cascadeInit = false;
	
	private Context mContext;
	
	public ImageUtils(int imageWidth, int imageHeight, SystemParameters parameters,Context context){
		previewWidth = imageWidth;
		previewHeight = imageHeight;
		
		mContext = context;
		mParameters = parameters;
		
		InitCascade(mContext);
		
		mYuv = new Mat(previewHeight + previewHeight / 2, previewWidth, CvType.CV_8UC1);
		mRgba = new Mat();
	}

	public Bitmap convertBytes2Bitmap(byte[] data){

		Mat dst = convertBytes2Mat(data);
        Bitmap bmp = Bitmap.createBitmap(dst.width(), dst.height(), Bitmap.Config.ARGB_8888);

        try {
            Utils.matToBitmap(dst, bmp);
            Log.d(TAG, "Mat transpose OK");
        } catch(Exception e) {
            Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
	}
	
	/*
	 * return 3 channel Mat
	 */
	public Mat convertBytes2Mat(byte[] data){
		mYuv.put(0, 0, data);
        Imgproc.cvtColor(mYuv, mRgba, Imgproc.COLOR_YUV420sp2RGB, 4);
        
        Mat dst = new Mat();
        
        if(mParameters.CameraID == Camera.CameraInfo.CAMERA_FACING_BACK){
        	Core.transpose(mRgba, dst);
            Core.flip(dst, dst, 1);
        }
        if(mParameters.CameraID == Camera.CameraInfo.CAMERA_FACING_FRONT){
      	  	Core.transpose(mRgba, dst);
      	  	Core.flip(dst, dst, 0);
        }
        Log.d(TAG, "Mat transpose");
        
        // convert 4 channel Mat to 3 channel Mat
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGRA2BGR);
        return dst;
	}
	
	public Bitmap convertMat2Bitmap(Mat data){
		if(data.empty() == true){
			return null;
		}
		
		Bitmap bmp = Bitmap.createBitmap(data.width(), data.height(), Bitmap.Config.ARGB_8888);
        try {
            Utils.matToBitmap(data, bmp);
            Log.d(TAG, "Mat transpose to Bitmap OK");
        } catch(Exception e) {
            Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
	}
	
	public Mat rotatedMat(Mat src){
		// TODO
		return null;
	}
	
	public Mat detectFacesAndExtractFace(Mat img){		
		float factor = (float)0.3;
		Mat dst = new Mat();
		Imgproc.resize(img, dst, new Size(img.width() * factor, img.height() * factor));
		
		mFaceRegion = new Mat();
		if (mCascade != null) {
            MatOfRect faces = new MatOfRect();
            mCascade.detectMultiScale(dst, faces, 1.1, 3, 0
                    |Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                    |Objdetect.CASCADE_DO_ROUGH_SEARCH
                    //|Objdetect.CASCADE_DO_CANNY_PRUNING
                    |Objdetect.CASCADE_SCALE_IMAGE
                    , new Size(20, 20), new Size());
            
            if(faces.toArray().length > 0){
            	Log.d(TAG, "face detected");
            	
            	Rect[] rects = faces.toArray();
            	
                for(int i = 0; i < rects.length; i++)
                {
                	rects[i].x /= factor;
                	rects[i].y /= factor;
                	rects[i].width /= factor;
                	rects[i].height /= factor;
                	
                	if(i == 0){
                		// copy face region
                    	img.submat(rects[i]).copyTo(mFaceRegion);
                	}
                	
                	// draw rectangle on original image
                	Core.rectangle(img, rects[i].tl(), rects[i].br(), new Scalar(0, 255, 0, 255), 3);
                }  	
            }
		}
        return img;
	}
	
	// detect face by haar cascade
	public Mat detectFaces(Mat img){
		
		float factor = (float)0.3;
		Mat dst = new Mat();
		Imgproc.resize(img, dst, new Size(img.width() * factor, img.height() * factor));
		
		if (mCascade != null) {
            MatOfRect faces = new MatOfRect();
            mCascade.detectMultiScale(dst, faces, 1.1, 3, 0
                    |Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                    |Objdetect.CASCADE_DO_ROUGH_SEARCH
                    //|Objdetect.CASCADE_DO_CANNY_PRUNING
                    |Objdetect.CASCADE_SCALE_IMAGE
                    , new Size(40, 40), new Size());
             
            Rect[] rects = faces.toArray();
            
            for(int i = 0; i < rects.length; i++)
            {
            	rects[i].x /= factor;
            	rects[i].y /= factor;
            	rects[i].width /= factor;
            	rects[i].height /= factor;
            	
            	Core.rectangle(img,rects[i].tl(), rects[i].br(), new Scalar(0, 255, 0, 255), 3);
            }
            
            
//            if(faces.toArray().length > 0){
//            	Log.d(TAG, "face detected");
//            	for (Rect r : faces.toArray()){
//                    Core.rectangle(img, r.tl(), r.br(), new Scalar(0, 255, 0, 255), 3);
//            	}
//            }
		}
        return img;
	}
	
	private ImageUtils(){
		// Hide the null parameter constructor
	}
	
	/*
	 * init the cascade
	 */
	public boolean InitCascade(Context context){
		cascadeInit = false;
    	try {
            InputStream is = context.getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File cascadeFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");
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
            } else{
                Log.i(TAG, "Loaded cascade classifier from " + cascadeFile.getAbsolutePath());
                cascadeInit = true;
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
	
	public Mat GetFaceRegion(){
		if(mFaceRegion != null){
			return mFaceRegion;
		}
		return null;
	}
}
