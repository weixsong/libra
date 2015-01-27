package com.example.asm;

import java.io.ByteArrayOutputStream;

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
 * @author wei song
 * 
 *
 */
public class ImageUtils {
	private static String TAG = "com.example.asm.ImageUtils";
	
	public static Bitmap yuv2bitmap(byte[] data, int width, int height) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		YuvImage yuvImage = new YuvImage(data, ImageFormat.NV21, width, height, null);
		yuvImage.compressToJpeg(new android.graphics.Rect(0, 0, width, height), 100, out);
		byte[] imageBytes = out.toByteArray();
		Bitmap bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);

		// rotate
		Matrix matrix = new Matrix();
		matrix.postRotate(90);
		Bitmap dst = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);
		return dst;
	}
	
	public static Bitmap mat2Bitmap(Mat data){
		if(data.empty() == true){
			return null;
		}
		
		Bitmap bmp = Bitmap.createBitmap(data.width(), data.height(), Bitmap.Config.ARGB_8888);
        try {
            Utils.matToBitmap(data, bmp);
        } catch(Exception e) {
            Log.e(TAG, "Utils.matToBitmap() throws an exception: " + e.getMessage());
            bmp.recycle();
            bmp = null;
        }
        return bmp;
	}
	
	public static Mat detectFacesAndExtractFace(CascadeClassifier cascade, Mat src, Mat face){
		Mat ret = new Mat();
		src.copyTo(ret);

		float factor = (float)0.3;
		Mat dst = new Mat();
		Imgproc.resize(src, dst, new Size(src.width() * factor, src.height() * factor));
		
		if (cascade != null) {
            MatOfRect faces = new MatOfRect();
            cascade.detectMultiScale(dst, faces, 1.2, 3, 0
                    |Objdetect.CASCADE_FIND_BIGGEST_OBJECT
                    |Objdetect.CASCADE_DO_ROUGH_SEARCH
                    //|Objdetect.CASCADE_DO_CANNY_PRUNING
                    |Objdetect.CASCADE_SCALE_IMAGE
                    , new Size(30, 30), new Size());
            
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
                    	src.submat(rects[i]).copyTo(face);
                	}
                	
                	// draw rectangle on original image
                	Core.rectangle(ret, rects[i].tl(), rects[i].br(), new Scalar(0, 255, 0, 255), 3);
                }  	
            }
		}
        return ret;
	}
}
