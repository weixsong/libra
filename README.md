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

