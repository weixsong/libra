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
const String APP_DIR = "/data/data/com.example.asm/app_data/";

extern "C" {
/*
 * do Canny edge detect
 */
JNIEXPORT void JNICALL Java_com_example_asm_NativeCode_DoCanny(JNIEnv* env,
		jobject obj, jlong matSrc, jlong matDst, jdouble threshold1 = 50,
		jdouble threshold2 = 150, jint aperatureSize = 3) {

	Mat * img = (Mat *) matSrc;
	Mat * dst = (Mat *) matDst;
	cvtColor(*img, *dst, COLOR_BGR2GRAY);
	Canny(*img, *dst, threshold1, threshold2, aperatureSize);
}

/*
 * face detection
 * matDst: face region
 * scaleFactor = 1.1
 * minNeighbors = 2
 * minSize = 30 * 30
 */
JNIEXPORT void JNICALL Java_com_example_asm_NativeCode_FaceDetect(JNIEnv* env,
		jobject obj, jlong matSrc, jlong matDst, jdouble scaleFactor, jint minNeighbors, jint minSize) {

	Mat * src = (Mat *) matSrc;
	Mat * dst = (Mat *) matDst;

	float factor = 0.3;
	Mat img;
	resize(*src, img, Size((*src).cols * factor, (*src).rows * factor));

	String cascadeFile = APP_DIR + "haarcascade_frontalface_alt2.xml";

	if (!init) {
		cascade.load(cascadeFile);
		init = true;
	}

	if (cascade.empty() != true) {
		vector<Rect> faces;
		cascade.detectMultiScale(img, faces, scaleFactor, minNeighbors, 0
				| CV_HAAR_FIND_BIGGEST_OBJECT
				| CV_HAAR_DO_ROUGH_SEARCH
				| CV_HAAR_SCALE_IMAGE, Size(minSize, minSize));

		for (int i = 0; i < faces.size(); i++) {
			Rect rect = faces[i];
			rect.x /= factor;
			rect.y /= factor;
			rect.width /= factor;
			rect.height /= factor;

			if (i == 0) {
				(*src)(rect).copyTo(*dst);
			}

			rectangle(*src, rect.tl(), rect.br(), Scalar(0, 255, 0, 255), 3);
		}
	}
}

/*
 *  do ASM
 *  error code:
 *  -1: illegal input Mat
 *  -2: ASM initialize error
 *  -3: no face detected
 */
JNIEXPORT jintArray JNICALL Java_com_example_asm_NativeCode_FindFaceLandmarks(
		JNIEnv* env, jobject, jlong matAddr, jfloat ratioW, jfloat ratioH) {
	const char * PATH = APP_DIR.c_str();

	clock_t StartTime = clock();
	jintArray arr = env->NewIntArray(2 * stasm_NLANDMARKS);
	jint *out = env->GetIntArrayElements(arr, 0);

	Mat img = *(Mat *) matAddr;
	cvtColor(img, img, COLOR_BGR2GRAY);

	if (!img.data) {
		out[0] = -1; // error code: -1(illegal input Mat)
		out[1] = -1;
		img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	}

	int foundface;
	float landmarks[2 * stasm_NLANDMARKS]; // x,y coords

	if (!stasm_search_single(&foundface, landmarks, (const char*) img.data,
			img.cols, img.rows, " ", PATH)) {
		out[0] = -2; // error code: -2(ASM initialize failed)
		out[1] = -2;
		img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	}

	if (!foundface) {
		out[0] = -3; // error code: -3(no face found)
		out[1] = -3;
		img.release();
		env->ReleaseIntArrayElements(arr, out, 0);
		return arr;
	} else {
		for (int i = 0; i < stasm_NLANDMARKS; i++) {
			out[2 * i] = cvRound(landmarks[2 * i] * ratioW);
			out[2 * i + 1] = cvRound(landmarks[2 * i + 1] * ratioH);
		}
	}
	double TotalAsmTime = double(clock() - StartTime) / CLOCKS_PER_SEC;
	__android_log_print(ANDROID_LOG_INFO, "com.example.asm.native",
			"running in native code, \nStasm Ver:%s Img:%dx%d ---> Time:%.3f secs.", stasm_VERSION,
			img.cols, img.rows, TotalAsmTime);

	img.release();
	env->ReleaseIntArrayElements(arr, out, 0);
	return arr;
}
}
