# libra
This is a super project, contains some image process projects developed in Android platform
In this project, it contains project that show how to use NDK to develop native c/c++ code,
also it contains project that show how to combine native c/c++ code and java SDK of opencv.

When combined native c/c++ code and java SDK of opencv, NDK will delete the thrid party library automatically in the libs folder, this problem seems does not happen in Windows version of opencv4android, but happens in linux/unix version of opencv4android, and this ugly bug confused me for nearly one day. Finally solved by prebuild library strategy in NDK. See details in [ASMDemo](https://github.com/weixsong/libra#asmandroiddemo).

Projects in this repo:
 * [NDKDemo](https://github.com/weixsong/libra#ndkdemo)
 * [ASMDemo](https://github.com/weixsong/libra#asmandroiddemo)
 * [OCRDemo](https://github.com/weixsong/libra#ocrdemo)
 * [DailyselfieDemo](https://github.com/weixsong/libra#dailyselfiedemo)

# IDE Setup
Currently Android Studio is famous for Android App development, But I'm not familiar with Android Stduio, so I'm still using Eclipse + ADT(Android Development Tools) to develop Android app. All the following setup instructions are based on Eclipse + ADT. If you're using Android Studio, please check out how to setup the IDE and help to updae this instructions.

This IDE Setup is on the assumption that your want to do NDK development by using Opencv.
If your just want to take a look project OCRDemo, you don't need to do the following setup. In other words, the following non-trivial setup steps are only useful for project NDKDemo and ASMAndroidDemo.

To setup your development environment, please follow the instructions: http://blog.csdn.net/watkinsong/article/details/9849973
Currently, this instruction is in Chinese, your can try to translate it with Google translator or something. Later I will provide detailed English version. And the following also contains some simple instructions.

 * 1. Download Eclipse.
 * 2. Install java on your OS(I'm using Ubuntu14.04LTS), and configure the System variables.
 * 3. Install ADT which is used to develop Android project in Eclipse. Go to this link: http://developer.android.com/sdk/eclipse-adt.html  to download ADT tools. After download the ADT tools, open your Eclipse, and click Help->Install New Software..., then by archive install, please select all the components provided by ADT.
 * 4. Configure Android SDK and create Android Virtual Machine, if your want to run your app on a real device, then you don't need to create Android Virtual Machine. This step is easy, just use the Android SDK and AVD manager, and also please download the corresponding Anroid SDK.
 * 5. OPTIONAL, install CDT, if you select all the components in step 3, you don't need to do this step, but if you don't have CDT installed, please install CDT.
 * 6. Download NDK (Native Development Kit):http://developer.android.com/sdk/ndk/index.html , extract the package and put it somewhere on your disk.
 * 7. Download OpenCV4Android: http://sourceforge.net/projects/opencvlibrary/files/opencv-android/, extract the package and put it somewhere on your disk.

Upto now, the IDE setup is basically done, but if you want to run the following projects, you still need to do someother change of the project property. What you need to change is included in each project's part. Just move on.


# NDKDemo
This project shows how to use NDK to develop native c/c++ code that invoked by jni in Android.
Developed by opencv4android, but only use opencv c/c++ head file.

Setup this project:
 * 1. Goto https://github.com/weixsong/libra/blob/master/README.md#ide-setup, and following the instructions to setup IDE.
 * 2. Import this NDKDemo project by Eclipse.
 * 3. After imported this project, your may see some errors with this project, don't be afriad, just go to step 4, :).
 * 4. Open the property of this NDKDemo project, click C/C++ Build -> Envrionment, change the variable NDKROOT, change the NDKROOT value to the folder of your unzipped NDK folder.
 * 5. If you're using Windows, open the property of NDKDemo project, click C/C++ Build, in Builder setting, change ${NDKROOT}/ndk-build to ${NDKROOT}/ndk-build.cmd
 * 6. Still open the property, click C/C++ General -> Path and Symbols, in the include tab, select GNU C++, and find the variable such as "home/wesong/software/OpenCV-2.4.10-android-sdk/sdk/native/jni/include", change this include directories to the OpenCV-2.4.10-android-sdk/sdk/native/jni/include of your opencv4android folder, this will make Eclipse be able to find the head files of opencv.
 * 7. Other include directories you need to pay attentation are:
    + ${NDKROOT}/platforms/android-21/arch-arm/usr/include
    + ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.9/include
    + ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi-v7a/include/bits
 * 8. Change Android.mk file, line 8, change the line "include /home/wesong/software/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk" to include your OpenCV.mk, find OpenCV.mk on your OpenCV4Android, and put the path here.

Then, you could build the NDKDemo project and run it on your mobile phone or AVD.
If you encounter some problem, please goto http://blog.csdn.net/watkinsong/article/details/9849973 for more information.

# ASMAndroidDemo
This project combined native c/c++ code and OpenCV java SDK (OpenCV4Android) to do ASM (Active Shape Model) points location and face detection.
ASMAndroidDemo defaultly use back camera, just because I don't have enough time to add function to call 
from camera.

ASMAndroidDemo is doing camera preview in customized SurfaceView, which run in isolated thread, and doing 
canny edge detection in UI thread, and doing face detection in a new thread, then, currently most of Android 
phone is 4 cores cpu, then compute burden could be distributed into each cpu core.

In this project, ASM points location is done by native c/c++ code, and because ASM really cost a longer time to 
compute the points location, so ASM computation will be done in AsyncTask, this method will not block the UI
thread and will not cause an ANR error of Android.

For Canny Edge detect, face detection, here default parameters are used, actually these parameters should be configurated by user, but no time for me.

Actually the orginal project of this demo that I developed one year ago support both front camera and back camera,
and user could config to choose camera and choose use native c/c++ code or opencv4android java SDK, but now I don't have enough time, so here just developed a working demo, someone interested about this project could fork it and improve it.

**IMPORTANT**: My test devices are RedMi2 and XiaoMi3, the target ABI of these two phones are **armeabi** and **armeabi-v7a**, in order to make this project runable, you need to select correct libopencv_java.so according to your hardware platform, default **target ABI** is **amerabi**, if you are using other hardware platform, checkout what platform you are using and select **libopencv_java.so** from corresponding platform library that Opencv4Android has already built for you.
Actually I encounted this problem when I install this project on RedMi2 but the **libopencv_java.so** is from other platform **amerabi-v7a**, and I have the runtime error such as java.lang.noclassdeffounderror.

Setup this project:
 * 1. Goto https://github.com/weixsong/libra/blob/master/README.md#ide-setup, and following the instructions to setup IDE.
 * 2. Import this project by Eclipse.
 * 3. Open the property of this project, click C/C++ Build -> Envrionment, change variable NDKROOT to your NDK root path. Such as : /home/wesong/software/android-ndk-r10d. 
 * 3(2). If you're using Windows, open the property of NDKDemo project, click C/C++ Build, in Builder setting, change ${NDKROOT}/ndk-build to ${NDKROOT}/ndk-build.cmd
 * 4. Import Opencv4Android into your Eclipse.
 * 5. Open the property of this project, click Android, and delete the default library reference, add your Opencv4Android library reference. (Pointing to the OpenCV4Android project)
 * 6. Still open the property, click C/C++ General -> Path and Symbols, in the include tab, select GNU C++, and find the variable such as "home/wesong/software/OpenCV-2.4.10-android-sdk/sdk/native/jni/include", change this include directories to the OpenCV-2.4.10-android-sdk/sdk/native/jni/include of your opencv4android folder, this will make Eclipse be able to find the head files of opencv.
 * 7. Other include directories you need to pay attentation are:
    + ${NDKROOT}/platforms/android-21/arch-arm/usr/include
    + ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.9/include
    + ${NDKROOT}/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi-v7a/include/bits
 * 8. Change Android.mk file, line 8, change the line "include /home/wesong/software/OpenCV-2.4.10-android-sdk/sdk/native/jni/OpenCV.mk" to include your OpenCV.mk, find OpenCV.mk on your OpenCV4Android, and put the path here.

Then, you could build the ASMDemo project and run it on your mobile phone or AVD.
If you encounter some problem, please goto http://blog.csdn.net/watkinsong/article/details/9849973 for more information.

# OCRDemo
OCRDemo is an application that shows how to use OCR (Optical Character Recognition) on Android platform.
This OCRDemo project is based on tess-two (an Android library that could use OCR) and tesseract-orc project(using this project's training data).

How to setup this project:

* Step 1: goto https://github.com/weixsong/tess-two, download the latest tess-two project, import the project by Eclipse.

* Step 2: goto https://code.google.com/p/tesseract-ocr/, download your interested language trainning data.
The default trainning data is english OCR data, and this default trainning data is already contained in this repo, if you just want to do English OCR, you don't need to go to tesseract-ocr project to download trainning data. But if you want to do other language OCR, such as Chinese(this also include in the project), you need to go to tesseract-ocr project and download corresponding trainning data. 

* Step 3: change the code in this OCRDemo project, as shown following:

  Change this line of code,

    private String lang = "eng"; //in MainActivity.java 

  change "eng" into the corresponding language, such as "chi_sim".
  And don't forget to put the downloaded trainning data into asset fold of this project.

  Currently this project contains two languages(simplied Chinese, English), you can change the language by set setting, and if you just want to contain only on language, you could download the corresponding trained data and hard code the lang variable: private String lang = "you lang". If you want to support mulit language, please add the lang in res->values->string.xml by referencing what I've done, and please don't forget update TesstwoOCR.java file to contain other language OCR detection code.

* Step 4: after imported the tess-two project, remember reference the tess-two project in this OCRDemo project, otherwise it could not found the referenced API. 
Update the property of this OCRDemo project, and set the library reference to tess-two project.

* Step 5: then you can build this OCRDemo project and run it in your Android phone or emulator.

This OCRDemo project is just a very simple project that show you how to do OCR on Android platform. You can take a look of this project and know how to do OCR on Android platform quickly. And you can play whatever you want by OCR on Android, and maybe you could come up with some wonderful projects.

## OCRDemo effectiveness
Currently for English OCR the effectiveness is pretty good, go to the screenshot folder to see the screenshot. 

English results are better than Chinese results. And it will take a long time to run OCR if there are too many words in the image.

This project is tested on my Xiaomi phone (RedMi2)

# Say Goodbye to Image Process
Maybe this repo is my last image process related project, right now I have graduated for nearly one year and working on NLP, ML related work in Oracle. I really like this NLP related work, and maybe in the future I don't have time to do any image process work. Then, goodbye image process.

# DailySelfieDemo
DailySelfieDemo is a complex Android app demo, please notice that this project has no relation with image process on Android platform, I put this project here just to give an example of how to develop a relatively complex Android app.

By take a look of this DailySelfDemo project, you could understand the mechanism of:

* 1. Android Intent and how to start another activity.
* 2. How to use AsyncTask to do async work.
* 3. How to load image async.
* 4. How to store image on phone stroage.
* 5. How to take image by camera.
* 6. How to use NotificationManager in Android.
* 7. How to use BroadcastReceiver.
* 8. How to create Service.
* 9. How to create customized Adapter.
* 10. How to start service on Android bootup by BroadcastReceiver.

What is bad is that I'm using MIROM, this ROM blocks system android.intent.action.BOOT_COMPLETED signal, so I could not start service after the system boot.
