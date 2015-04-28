# libra
This is a super project, contains some image process projects developed in Android platform
In this project, it contains project that show how to use NDK to develop native c/c++ code,
also it contains project that show how to combine native c/c++ code and java SDK of opencv.

When combined native c/c++ code and java SDK of opencv, NDK will delete the thrid party library automatically,
this problem seems does not happen in Windows version of opencv4android, but happens in linux/unix version of 
opencv4android, and this ugly bug confused me for nearly one day.

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
The default trainning data is Chinese OCR data, and this default trainning data is already contained in this repo, if you just want to do Chinese OCR, you don't need to go to tesseract-ocr project to download trainning data. But if you want to do other language OCR, such as English, you need to go to tesseract-ocr project and download corresponding trainning data. 

* Step 3: change the code in this OCRDemo project, as shown following:

  Change this line of code,

    private String lang = "chi_sim"; //in MainActivity.java 

  change "chi_sim" into the corresponding language.
  And don't forget to put the downloaded trainning data into asset fold of this project.

* Step 4: after imported the tess-two project, remember reference the tess-two project in this OCRDemo project, otherwise it could not found the referenced API. 
Update the property of this OCRDemo project, and set the library reference to tess-two project.

* Step 5: then you can build this OCRDemo project and run it in your Android phone or emulator.

# Say Goodbye to Image Process
Maybe this repo is my last image process related project, right now I have graduated for nearly one year and working on NLP, ML related work in Oracle. I really like this NLP related work, and maybe in the future I don't have time to do any image process work. Then, goodbye image process.
