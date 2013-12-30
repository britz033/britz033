#include <jni.h>

const jdouble value = 1.13;

jdouble Java_com_zoeas_util_CalculateC_getData(JNIEnv *env, jobject obj,
		jdouble latitude) {
	return latitude/value;
}

jdouble Java_com_zoeas_util_CalculateC_setData(JNIEnv *env, jobject obj,
		jdouble longitude) {
	return longitude*value;
}
