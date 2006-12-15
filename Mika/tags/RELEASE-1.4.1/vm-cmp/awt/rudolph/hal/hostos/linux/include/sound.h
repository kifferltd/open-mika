#include "wonka.h"
#include "jni.h"

typedef int CallBack(JNIEnv *env, jobject thisObj);


w_void play(w_ubyte *raw_data, w_int length, CallBack* callback, JNIEnv *env, jobject thisObj);

w_int decode_au_mulau(w_ubyte *file_data, w_int length);
