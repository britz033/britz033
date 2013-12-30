LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE    := calculate_db
LOCAL_SRC_FILES := calculate_db.c

include $(BUILD_SHARED_LIBRARY)