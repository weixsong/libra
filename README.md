# libra
This is a super project, contains some image process projects developed in Android platform
In this project, it contains project that show how to use NDK to develop native c/c++ code,
also it contains project that show how to combine native c/c++ code and java SDK of opencv.

When combined native c/c++ code and java SDK of opencv, NDK will delete the thrid party library automatically,
this problem seems does not happen in Windows version of opencv4android, but happens in linux/unix version of 
opencv4android, and this ugly bug confused me for nearly one day.

# IDE Setup
To setup your development environment, please follow the instructions: http://blog.csdn.net/watkinsong/article/details/9849973
Currently, this instruction is in Chinese, your can try to translate it with Google translator or something. Later I will provide detailed English version. And the following also contains some simple instructions.

 * 1. Download Eclipse.
 * 2. Install java on your platform. (I'm using Ubuntu14.04LTS)
 * 3. Install ADT which is used to develop c/c++ project in Eclipse. Go to this link: http://developer.android.com/sdk/eclipse-adt.html  to download ADT tools. After download the ADT tools, open your Eclipse, and click Help->Install New Software..., then by archive install, please select all the components provided by ADT.
 * 4. 

# NDKDemo
This project shows how to use NDK to develop native c/c++ code that invoked by jni in Android.
Developed by opencv4android, but only use opencv c/c++ head file.

# ASMAndroidDemo
This project combined native c/cpp code and opencv java SDK to do ASM points location and face detection.
ASMAndroidDemo defaultly use back camera, just because I don't have enough time to add function to call 
from camera.

ASMAndroidDemo is doning camera preview in customized SurfaceView, which run in isolated thread, and doing 
canny edge detection in UI thread, and doing face detection in a new thread, then, right now most of Android 
phone is 4 cores cpu, then compute burden could be distributed into each cpu core.

In this project, ASM points location is done by native c/c++ code, and because ASM really cost a longer time to 
compute the points location, so ASM computation will be done in AsyncTask, this method will not block the UI
thread and will not cause an ANR error of Android.

For Canny Edge detect, face detection, here default parameters are used, actually these parameters should be configurate
by user, but no time for me.

Actually the orginal project of this demo that I developed one year ago support both front camera and back camera,
and user could config to choose camera and choose use native c/c++ code or opencv4android java SDK, but now I don't
have enough time, so here just developed a working demo, someone interested about this project could fork it and 
improve it.


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

# OCRDemo effectiveness
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
