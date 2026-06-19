#include <jni.h>
#include <string>
#include <unistd.h>
#include "cJSON.h"
#include "common.h"
#include "helper.h"
#include "raptormark-jni.h"

#ifdef __cplusplus
extern "C" {
#endif

typedef struct fio_context {
    JavaVM  *javaVM;
    jclass   nativeDataSrcClz;
} FIOContext;

FIOContext globalCtx;

int updateStatusCallback(const char *msg) {
    JNIEnv *env;

    if (globalCtx.javaVM->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jstring javaMsg = env->NewStringUTF(msg);
    jmethodID updateStatusMethod = env->GetStaticMethodID(globalCtx.nativeDataSrcClz,
                                        METHOD_NAME_UPDATE_STATUS, "(Ljava/lang/String;)I");

    int ret = env->CallStaticIntMethod(globalCtx.nativeDataSrcClz, updateStatusMethod, javaMsg);

    env->DeleteLocalRef(javaMsg);

    return ret;
}

JNIEXPORT jint JNICALL native_FIOTest(JNIEnv *env, jobject instance, jstring jsonCommand) {
    const char *jsonStr = env->GetStringUTFChars(jsonCommand, NULL);
    int ret, argc;
    char **argv;
    LibFIO libFio("fio", (void *)updateStatusCallback);

    json2Options(jsonStr, &argc, &argv);
    ret = libFio.fio(argc, argv);
    freeOptions(&argc, &argv);

    return ret;
}

JNIEXPORT jstring JNICALL native_ListEngines(JNIEnv *env, jobject instance) {
    char *engineList[MAX_ENGINE_NUM];
    int index, engineNum;
    LibFIO libFio("fio_list_ioengines", (void *)updateStatusCallback);

    engineNum = libFio.fio_list_ioengines(engineList);

    cJSON *root = cJSON_CreateObject();
    cJSON *engines = cJSON_AddArrayToObject(root, "engines");

    if (engineNum > 0) {
        for (index = 0; index < engineNum; index++) {
            cJSON *engine = cJSON_CreateObject();
            char *engineName = engineList[index];
            bool engineAvailable = checkEngineAvailability(engineName);
            cJSON_AddStringToObject(engine, "name", engineName);
            cJSON_AddBoolToObject(engine, "available", engineAvailable);
            cJSON_AddItemToArray(engines, engine);

            free(engineList[index]);
        }
    } else {
        LOGE("Could not find IO engine.");
    }

    cJSON *meta = cJSON_AddObjectToObject(root, "meta");
    bool rootAccess = checkRootAccess();
    bool ioUringDirect = try_io_uring_setup();
    cJSON_AddBoolToObject(meta, "rootAvailable", rootAccess);
    cJSON_AddBoolToObject(meta, "ioUringDirectAccess", ioUringDirect);

    return env->NewStringUTF(cJSON_PrintUnformatted(root));
}

JNIEXPORT jboolean JNICALL native_CheckRootAccess(JNIEnv *env, jobject instance) {
    return checkRootAccess() ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jstring JNICALL native_RunFIOTestWithRoot(JNIEnv *env, jobject instance,
                                                      jstring jsonCommand,
                                                      jstring runnerPath) {
    const char *jsonStr = env->GetStringUTFChars(jsonCommand, NULL);
    const char *runnerPathStr = env->GetStringUTFChars(runnerPath, NULL);

    char *result = runFioWithRoot(jsonStr, runnerPathStr);

    env->ReleaseStringUTFChars(jsonCommand, jsonStr);
    env->ReleaseStringUTFChars(runnerPath, runnerPathStr);

    if (result == NULL) {
        return NULL;
    }

    jstring jResult = env->NewStringUTF(result);
    free(result);
    return jResult;
}

#ifdef __cplusplus
}
#endif

static const JNINativeMethod FIOMethods[] = {
        {"native_FIOTest",           "(Ljava/lang/String;)I",          (void *) native_FIOTest},
        {"native_ListEngines",       "()Ljava/lang/String;",           (void *) native_ListEngines},
        {"native_CheckRootAccess",   "()Z",                            (void *) native_CheckRootAccess},
        {"native_RunFIOTestWithRoot","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;",
                                                                       (void *) native_RunFIOTestWithRoot}
};

JNIEXPORT jint JNI_OnLoad(JavaVM *vm, void *reserved) {
    JNIEnv *env;

    memset(&globalCtx, 0, sizeof(globalCtx));

    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return JNI_ERR;
    }

    jclass c = env->FindClass(CLASS_NAME_NATIVE_DATA_SRC);
    if (c == nullptr) return JNI_ERR;

    globalCtx.javaVM = vm;
    globalCtx.nativeDataSrcClz = (jclass)env->NewGlobalRef(c);

    jint rc = env->RegisterNatives(c, FIOMethods, sizeof(FIOMethods) / sizeof(JNINativeMethod));
    if (rc != JNI_OK) return rc;

    return JNI_VERSION_1_6;
}

JNIEXPORT void JNI_OnUnload(JavaVM *vm, void *reserved) {
    JNIEnv *env;
    if (vm->GetEnv(reinterpret_cast<void **>(&env), JNI_VERSION_1_6) != JNI_OK) {
        return;
    }

    jclass c = env->FindClass(CLASS_NAME_NATIVE_DATA_SRC);
    if (c == nullptr) return;

    env->DeleteGlobalRef(globalCtx.nativeDataSrcClz);

    env->UnregisterNatives(c);
}
