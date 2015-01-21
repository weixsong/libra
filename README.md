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
