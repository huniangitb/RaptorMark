/*
 * fio_runner - Standalone fio test runner for Android
 *
 * Used when elevated (root) privileges are needed, e.g. for io_uring engine.
 * Loads libfio.so via dlopen and executes the test, outputting JSON result.
 * Usage: fio_runner --json-config=<path>
 */

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <dlfcn.h>
#include <unistd.h>
#include <errno.h>
#include <stdbool.h>
#include <cJSON.h>

#define MAX_ARGV 128
#define ARGV_OPTION_MAX_LENGTH 256

typedef int (*fio_func_t)(int argc, char *argv[], char *envp[], void *callback_ptr);

static int json2Options(const char *jsonStr, int *argc, char ***argv) {
    cJSON *root, *options, *option, *shortOpts;

    root = cJSON_Parse(jsonStr);
    if (!root) {
        fprintf(stderr, "fio_runner: Failed to parse JSON config\n");
        return -1;
    }

    shortOpts = cJSON_GetObjectItem(root, "shortopts");
    bool isShortOpts = cJSON_IsBool(shortOpts) && (shortOpts->type & cJSON_True);

    options = cJSON_GetObjectItem(root, "options");
    if (!options || !cJSON_IsArray(options)) {
        fprintf(stderr, "fio_runner: Invalid or missing 'options' in config\n");
        cJSON_Delete(root);
        return -1;
    }

    *argc = cJSON_GetArraySize(options) + 1;
    *argv = (char **) calloc(*argc, sizeof(char *));
    if (!*argv) {
        cJSON_Delete(root);
        return -1;
    }

    (*argv)[0] = (char *) calloc(ARGV_OPTION_MAX_LENGTH, sizeof(char));
    snprintf((*argv)[0], ARGV_OPTION_MAX_LENGTH, "fio_runner");

    int i = 1;
    cJSON_ArrayForEach(option, options) {
        cJSON *name = cJSON_GetObjectItem(option, "name");
        cJSON *value = cJSON_GetObjectItem(option, "value");

        if (!name || !cJSON_IsString(name)) {
            continue;
        }

        (*argv)[i] = (char *) calloc(ARGV_OPTION_MAX_LENGTH, sizeof(char));
        snprintf((*argv)[i], ARGV_OPTION_MAX_LENGTH,
                 isShortOpts ? "-%s" : "--%s", name->valuestring);
        if (value && cJSON_IsString(value)) {
            size_t curLen = strlen((*argv)[i]);
            snprintf((*argv)[i] + curLen, ARGV_OPTION_MAX_LENGTH - curLen,
                     isShortOpts ? "%s" : "=%s", value->valuestring);
        }
        i++;
    }

    cJSON_Delete(root);
    return 0;
}

static void freeOptions(int argc, char **argv) {
    for (int i = 0; i < argc; i++) {
        free(argv[i]);
    }
    free(argv);
}

int main(int argc, char *argv[]) {
    const char *libPath = NULL;
    const char *configPath = NULL;
    char *jsonConfig = NULL;
    void *libHandle = NULL;
    fio_func_t fio_func = NULL;
    int ret = 1;
    int fio_argc = 0;
    char **fio_argv = NULL;

    // Parse command line arguments
    for (int i = 1; i < argc; i++) {
        if (strncmp(argv[i], "--lib-path=", 11) == 0) {
            libPath = argv[i] + 11;
        } else if (strncmp(argv[i], "--json-config=", 14) == 0) {
            configPath = argv[i] + 14;
        }
    }

    if (!configPath) {
        fprintf(stderr, "fio_runner: --json-config=<path> is required\n");
        return 1;
    }

    // Read JSON config file
    FILE *configFile = fopen(configPath, "r");
    if (!configFile) {
        fprintf(stderr, "fio_runner: Cannot open config file '%s': %s\n",
                configPath, strerror(errno));
        return 1;
    }

    fseek(configFile, 0, SEEK_END);
    long configSize = ftell(configFile);
    fseek(configFile, 0, SEEK_SET);

    jsonConfig = (char *) malloc(configSize + 1);
    if (jsonConfig) {
        size_t readSize = fread(jsonConfig, 1, configSize, configFile);
        jsonConfig[readSize] = '\0';
    }
    fclose(configFile);

    if (!jsonConfig) {
        fprintf(stderr, "fio_runner: Failed to read config file\n");
        return 1;
    }

    // Try default library path if not specified
    if (!libPath) {
        libPath = "libfio.so";
    }

    // Load libfio.so
    libHandle = dlopen(libPath, RTLD_NOW | RTLD_GLOBAL);
    if (!libHandle) {
        // Try just "libfio.so" without path
        libHandle = dlopen("libfio.so", RTLD_NOW | RTLD_GLOBAL);
    }
    if (!libHandle) {
        fprintf(stderr, "fio_runner: Failed to load libfio.so: %s\n", dlerror());
        goto cleanup;
    }

    // Get fio function
    fio_func = (fio_func_t) dlsym(libHandle, "fio");
    if (!fio_func) {
        fprintf(stderr, "fio_runner: Failed to find 'fio' symbol: %s\n", dlerror());
        goto cleanup;
    }

    // Convert JSON config to fio args
    if (json2Options(jsonConfig, &fio_argc, &fio_argv) != 0) {
        fprintf(stderr, "fio_runner: Failed to parse JSON config\n");
        goto cleanup;
    }

    // Redirect stderr to stdout for unified output
    // (fio outputs progress info to stderr and JSON to stdout)

    // Run fio test
    ret = fio_func(fio_argc, fio_argv, NULL, NULL);

    // Make sure output is flushed
    fflush(stdout);
    fflush(stderr);

cleanup:
    if (fio_argv) {
        freeOptions(fio_argc, fio_argv);
    }
    if (libHandle) {
        dlclose(libHandle);
    }
    free(jsonConfig);

    return ret;
}
