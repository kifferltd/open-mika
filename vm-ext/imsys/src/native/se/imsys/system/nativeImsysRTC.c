/* 
 * nativeImsysMDIO.c
 *
 * Copyright (C) 2002 Imsys AB. All rights reserved
 *
 *
 * native implementation of methods in class se.imsys.RTC
 * 
 */

#include <global.h>
#include <time.h>

extern void java_lang_System_currentTimeMillis(void);

/******************************************************************************************
 * Java_se_imsys_system_RTC_getTime
 *
 * native implementation of method:	 public static native long getTime()
 *
 */
void Java_se_imsys_system_RTC_getTime() 
{
	java_lang_System_currentTimeMillis();	// Will leave the current time, 64 bits, on the eval stack.
}

void Java_se_imsys_RTC_getTime() 
{
	java_lang_System_currentTimeMillis();	// Will leave the current time, 64 bits, on the eval stack.
}

/******************************************************************************************
 * Java_se_imsys_system_RTC_setTime
 *
 * native implementation of method:	 public static native void setTime(long time)
 *
 */
#pragma warn variables off
void Java_se_imsys_system_RTC_setTime(int timeLo, int timeHi) 
{
	time_t t;

//	t = timeLo / 1000;
	t = (time_t)asm("div.l", 1000, 0, timeLo, timeHi);

	settime(&t);
}

void Java_se_imsys_RTC_setTime(int timeLo, int timeHi) 
{
	time_t t;

	t = (time_t)asm("", timeLo, timeHi);
	settime(&t);
}
#pragma warn variables on


const NativeImplementationType Java_se_imsys_system_RTC_natives[] = 
{
    {"getTime",       		(NativeFunctionPtr)Java_se_imsys_system_RTC_getTime},
    {"setTime",       		(NativeFunctionPtr)Java_se_imsys_system_RTC_setTime},
    NATIVE_END_OF_LIST	
};

// For backward compability
const NativeImplementationType se_imsys_RTC_natives[] = 
{
    {"getTime",       		(NativeFunctionPtr)Java_se_imsys_RTC_getTime},
    {"setTime",       		(NativeFunctionPtr)Java_se_imsys_RTC_setTime},
    NATIVE_END_OF_LIST	
};


