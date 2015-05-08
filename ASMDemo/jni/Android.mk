LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
OPENCV_LIB_TYPE:=STATIC
ifeq ("$(wildcard $(OPENCV_MK_PATH))","")
#try to load OpenCV.mk from default install location
include /home/wesong/software/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk
else
include $(OPENCV_MK_PATH)
endif
LOCAL_MODULE    := Native

FILE_LIST := $(wildcard $(LOCAL_PATH)/stasm/*.cpp) \
 $(wildcard $(LOCAL_PATH)/stasm/MOD_1/*.cpp)

LOCAL_SRC_FILES := Native.cpp \
$(FILE_LIST:$(LOCAL_PATH)/%=%)

LOCAL_LDLIBS +=  -llog -ldl
include $(BUILD_SHARED_LIBRARY)

# other library
include $(CLEAR_VARS)
LOCAL_MODULE := opencv_java-prebuild
LOCAL_SRC_FILES := libopencv_java.so
include $(PREBUILT_SHARED_LIBRARY)
