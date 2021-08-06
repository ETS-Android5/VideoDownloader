#include <jni.h>
#include "https.h"
#include <android/log.h>
#include <string.h>

#define LOG_TAG "TAG/Native"
#define LOGI(...)  __android_log_print(ANDROID_LOG_INFO,LOG_TAG,__VA_ARGS__)
#define LOGE(...)  __android_log_print(ANDROID_LOG_ERROR,LOG_TAG,__VA_ARGS__)

JNIEXPORT jint JNICALL
Java_euphoria_psycho_explorer_NativeShare_get91Porn(JNIEnv *env, jobject thisObj,
                                                    jbyteArray data, jint length,
                                                    jbyteArray buffer) {
    // int len = (*env)->GetArrayLength(env, data);
    jbyte *dataToWrite = (*env)->GetByteArrayElements(env, data, NULL);
    HTTP_INFO hi1;
    // 32768 = 32 KB
    unsigned char arr[32768];
    http_get(&hi1, (char *) dataToWrite, (char *) arr, 32768);


    char *result = strstr((const char *) arr, "document.write(strencode2(\"");
    int patternSize = strlen("document.write(strencode2(\"");

    if (!result) {
        (*env)->ReleaseByteArrayElements(env, data, dataToWrite, 0);
        return 0;
    } else {
        char *index = memchr(result + patternSize, '"', strlen(result));
        length = index - result - patternSize;
    }

    (*env)->SetByteArrayRegion(env, buffer, 0, (jsize) length, (jbyte *) (result+patternSize));
    (*env)->ReleaseByteArrayElements(env, data, dataToWrite, 0);
    return length;
}

JNIEXPORT jboolean JNICALL
Java_euphoria_psycho_explorer_NativeShare_getString(JNIEnv *env, jobject thisObj,
                                                    jbyteArray data, jint length,
                                                    jbyteArray buffer) {
    // int len = (*env)->GetArrayLength(env, data);
    jbyte *dataToWrite = (*env)->GetByteArrayElements(env, data, NULL);
    HTTP_INFO hi1;
    unsigned char arr[length];
    int result = http_get(&hi1, (char *) dataToWrite, (char *) arr, length);
    (*env)->SetByteArrayRegion(env, buffer, 0, (jsize) length, (jbyte *) arr);
    return JNI_TRUE;
}