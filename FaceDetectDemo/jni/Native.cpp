#include <jni.h>
#include <stdio.h>
#include <stdlib.h>
#include <iostream>
#include <android/log.h>

#include <opencv2/opencv.hpp>
#include "./stasm/stasm_lib.h"

using namespace cv;
using namespace std;

CascadeClassifier cascade;
bool init = false;

extern "C" {
	/*
	 * do Canny edge detect
	 */
	JNIEXPORT void JNICALL Java_com_example_facedetectdemo_NativeCode_DoCanny(
			JNIEnv* env, jobject obj, jlong matSrc, jlong matDst) {

		Mat * img = (Mat *)matSrc;
		Mat * dst = (Mat *)matDst;

		cvtColor(*img, *dst, COLOR_BGR2GRAY);
		Canny(*img, *dst, 50, 150, 3);
	}

	/*
	 * face detection
	 * matDst: face region
	 */
	JNIEXPORT void JNICALL Java_com_example_facedetectdemo_NativeCode_FaceDetect(
			JNIEnv* env, jobject obj, jlong matSrc, jlong matDst) {

		Mat * src = (Mat *)matSrc;
		Mat * dst = (Mat *)matDst;

		float factor = 0.3;
		Mat img;
		resize(*src, img, Size((*src).cols * factor, (*src).rows * factor));

		String cascadeName = "/data/data/com.example.facedetectdemo/app_data/haarcascade_frontalface_alt2.xml";

		if(!init) {
			cascade.load(cascadeName);
			init = true;
		}

		if(cascade.empty() != true) {
			vector<Rect> faces;
			cascade.detectMultiScale( img, faces,
				      1.1, 2, 0
				      |CV_HAAR_FIND_BIGGEST_OBJECT
				      |CV_HAAR_DO_ROUGH_SEARCH
				      |CV_HAAR_SCALE_IMAGE
				      ,
				      Size(30, 30) );

			for(int i = 0; i < faces.size(); i++) {
				Rect rect = faces[i];
				rect.x /= factor;
				rect.y /= factor;
				rect.width /= factor;
				rect.height /= factor;

				if(i == 0) {
					(*src)(rect).copyTo(*dst);
				}

				rectangle(*src, rect.tl(), rect.br(), Scalar(0, 255, 0, 255), 3);
			}
		}
	}

	/*
	 *  do ASM
	 */
	JNIEXPORT jintArray JNICALL Java_com_example_facedetectdemo_NativeCode_FindFaceLandmarks(
			JNIEnv* env, jobject, jlong matAddr, jfloat ratioW, jfloat ratioH) {
		const char * PATH = "/data/data/com.example.facedetectdemo/app_data/";

		clock_t StartTime = clock();
		jintArray arr = env->NewIntArray(2*stasm_NLANDMARKS);
		jint *out = env->GetIntArrayElements(arr, 0);

		Mat  img = *(Mat *)matAddr;
		cvtColor(img, img, COLOR_BGR2GRAY);

    	if (!img.data) {
	    	out[0] = -1;
	    	out[1] = -1;
	    	img.release();
			env->ReleaseIntArrayElements(arr, out, 0);
			return arr;
	    }

	    int foundface;
	    float landmarks[2 * stasm_NLANDMARKS]; // x,y coords

    	if (!stasm_search_single(&foundface, landmarks, (const char*)img.data, img.cols, img.rows, " ", PATH)) {
    		out[0] = -2;
    		out[1] = -2;
    		img.release();
			env->ReleaseIntArrayElements(arr, out, 0);
			return arr;
    	}

   		if (!foundface){
	   		out[0] = -3;
	    	out[1] = -3;
	    	img.release();
			env->ReleaseIntArrayElements(arr, out, 0);
			return arr;
	   	} else {
			for (int i = 0; i < stasm_NLANDMARKS; i++) {
				out[2*i]   = cvRound(landmarks[2*i]*ratioW);
				out[2*i+1] = cvRound(landmarks[2*i+1]*ratioH);
			}
		}
		double TotalAsmTime = double(clock() - StartTime) / CLOCKS_PER_SEC;
	    __android_log_print(ANDROID_LOG_ERROR, "StasmAndroidDemo",
	    	"Stasm Ver:%s Img:%dx%d ---> Time:%.3f secs.", stasm_VERSION, img.cols, img.rows, TotalAsmTime);

	    img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	}
}
