################################################################################
# Automatically-generated file. Do not edit!
################################################################################

# Add inputs and outputs from these tool invocations to the build variables 
CPP_SRCS += \
../jni/stasm/asm.cpp \
../jni/stasm/classicdesc.cpp \
../jni/stasm/convshape.cpp \
../jni/stasm/err.cpp \
../jni/stasm/eyedet.cpp \
../jni/stasm/eyedist.cpp \
../jni/stasm/faceroi.cpp \
../jni/stasm/hat.cpp \
../jni/stasm/hatdesc.cpp \
../jni/stasm/landmarks.cpp \
../jni/stasm/misc.cpp \
../jni/stasm/pinstart.cpp \
../jni/stasm/print.cpp \
../jni/stasm/shape17.cpp \
../jni/stasm/shapehacks.cpp \
../jni/stasm/shapemod.cpp \
../jni/stasm/startshape.cpp \
../jni/stasm/stasm.cpp \
../jni/stasm/stasm_lib.cpp 

OBJS += \
./jni/stasm/asm.o \
./jni/stasm/classicdesc.o \
./jni/stasm/convshape.o \
./jni/stasm/err.o \
./jni/stasm/eyedet.o \
./jni/stasm/eyedist.o \
./jni/stasm/faceroi.o \
./jni/stasm/hat.o \
./jni/stasm/hatdesc.o \
./jni/stasm/landmarks.o \
./jni/stasm/misc.o \
./jni/stasm/pinstart.o \
./jni/stasm/print.o \
./jni/stasm/shape17.o \
./jni/stasm/shapehacks.o \
./jni/stasm/shapemod.o \
./jni/stasm/startshape.o \
./jni/stasm/stasm.o \
./jni/stasm/stasm_lib.o 

CPP_DEPS += \
./jni/stasm/asm.d \
./jni/stasm/classicdesc.d \
./jni/stasm/convshape.d \
./jni/stasm/err.d \
./jni/stasm/eyedet.d \
./jni/stasm/eyedist.d \
./jni/stasm/faceroi.d \
./jni/stasm/hat.d \
./jni/stasm/hatdesc.d \
./jni/stasm/landmarks.d \
./jni/stasm/misc.d \
./jni/stasm/pinstart.d \
./jni/stasm/print.d \
./jni/stasm/shape17.d \
./jni/stasm/shapehacks.d \
./jni/stasm/shapemod.d \
./jni/stasm/startshape.d \
./jni/stasm/stasm.d \
./jni/stasm/stasm_lib.d 


# Each subdirectory must supply rules for building sources it contributes
jni/stasm/%.o: ../jni/stasm/%.cpp
	@echo 'Building file: $<'
	@echo 'Invoking: GCC C++ Compiler'
	g++ -I"/home/songwei/Softwares/android-ndk-r10d/platforms/android-21/arch-arm/usr/include" -I"/home/songwei/Softwares/android-ndk-r10d/sources/cxx-stl/gnu-libstdc++/4.9/include" -I"/home/songwei/Softwares/android-ndk-r10d/sources/cxx-stl/gnu-libstdc++/4.9/libs/armeabi-v7a/include" -I/home/songwei/Softwares/OpenCV-2.4.10-android-sdk/sdk/native/jni/include -O0 -g3 -Wall -c -fmessage-length=0 -MMD -MP -MF"$(@:%.o=%.d)" -MT"$(@:%.o=%.d)" -o "$@" "$<"
	@echo 'Finished building: $<'
	@echo ' '


