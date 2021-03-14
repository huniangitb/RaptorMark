#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <sys/utsname.h>
#include <cJSON.h>
#include "common.h"
#include "helper.h"

#ifdef __cplusplus
extern "C" {
#endif

static int kernelMajorVersion = DEFAULT_KERNEL_VERSION_MAJOR;
static int kernelMinorVersion = DEFAULT_KERNEL_VERSION_MINOR;

static void getKernelVersion(int *major, int *minor) {
    struct utsname buffer;
    char extra[32];

    if (uname(&buffer) != 0) return;

    sscanf(buffer.release, "%d.%d.%s", major, minor, extra);
}

static void native_init helperInit() {
    getKernelVersion(&kernelMajorVersion, &kernelMinorVersion);
}

static int getKeyFromStr(char *key, strPairStruct *lut, int keyNum) {
    for (int i = 0; i < keyNum; i++) {
        strPairStruct *pair = &lut[i];
        if (strcmp(pair->key, key) == 0)
            return pair->val;
    }

    return keyNum;
}

bool checkEngineAvailability(char *engine) {
    bool available = false;

    switch (getKeyFromStr(engine, engineLut, ENGINE_MAX)) {
        case ENGINE_MMAP:
        case ENGINE_PSYNC:
            available = true;
            break;
        case ENGINE_IO_URING:
            if ((kernelMajorVersion > IO_URING_KERNEL_VERSION_MAJOR) ||
                (kernelMajorVersion == IO_URING_KERNEL_VERSION_MAJOR &&
                 kernelMinorVersion >= IO_URING_KERNEL_VERSION_MINOR)) {
                available = true;
            }
            break;
        default:
            break;
    }

    return available;
}

void json2Options(const char *jsonStr, int *argc, char ***argv) {
    cJSON *root, *options, *option, *shortOpts;
    root = cJSON_Parse(jsonStr);

    shortOpts = cJSON_GetObjectItem(root, "shortopts");
    bool isShortOpts = cJSON_IsBool(shortOpts) && (shortOpts->type & cJSON_True);

    options = cJSON_GetObjectItem(root, "options");
    *argc = cJSON_GetArraySize(options);
    *argv = (char**)calloc(*argc, sizeof(char*));

    int i = 0;
    cJSON_ArrayForEach(option, options)
    {
        cJSON *name = cJSON_GetObjectItem(option, "name");
        cJSON *value = cJSON_GetObjectItem(option, "value");

        (*argv)[i] = (char*) calloc(ARGV_OPTION_MAX_LENGTH, sizeof(char));
        sprintf((*argv)[i], isShortOpts ? "-%s" :"--%s", name->valuestring);
        if (cJSON_IsString(value))
            sprintf((*argv)[i] + strlen((*argv)[i]), "=%s", value->valuestring);

        i++;
    }
}

void freeOptions(int *argc, char ***argv) {
    for (int i = 0; i < *argc; i++)
        free((*argv)[i]);

    free(*argv);
}

#ifdef __cplusplus
}
#endif
