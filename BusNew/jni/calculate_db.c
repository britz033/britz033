#include <jni.h>

const jdouble value = 0.000384;
const jdouble value2 = 0.000172;

jdouble Java_com_zoeas_util_CalculateC_getData1(JNIEnv *env, jobject obj,
		jdouble latitude, jint id) {
	jdouble result = 0;
	if (id % 2 == 0) {
		result = (jdouble)id * value;
	} else {
		result = (jdouble)id* value;
	}
	return latitude-result;
}

jdouble Java_com_zoeas_util_CalculateC_getData2(JNIEnv *env, jobject obj,
		jdouble longitude, jint id) {
	jdouble result = 0;
	if (id % 2 == 0) {
		result = (jdouble)id* value2;
	} else {
		result = (jdouble)id* value2;
	}
	return longitude-result;
}
