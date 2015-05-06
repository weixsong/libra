LOCAL_PATH := $(call my-dir) 
include $(CLEAR_VARS)
OPENCV_LIB_TYPE:=STATIC
OPENCV_INSTALL_MODULES:=on
OPENCV_CAMERA_MODULES:=off
ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
#try to load OpenCV.mk from default install location
include /home/wesong/software/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk
else
include $(OPENCV_MK_PATH)
endif
LOCAL_MODULE    := native
LOCAL_SRC_FILES := native.cpp

LOCAL_LDLIBS +=  -llog -ldl
include $(BUILD_SHARED_LIBRARY)