#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <string.h>
#include <ctype.h>
#include <errno.h>
#include <fcntl.h>
#include <sys/utsname.h>
#include <sys/syscall.h>
#include <sys/wait.h>
#include <dlfcn.h>
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

// Forward declarations
bool try_io_uring_setup(void);
bool checkRootAccess(void);

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
        case ENGINE_LIBAIO:
            available = true;
            break;
        case ENGINE_IO_URING:
            if ((kernelMajorVersion > IO_URING_KERNEL_VERSION_MAJOR) ||
                (kernelMajorVersion == IO_URING_KERNEL_VERSION_MAJOR &&
                 kernelMinorVersion >= IO_URING_KERNEL_VERSION_MINOR)) {
                if (try_io_uring_setup()) {
                    available = true;
                } else if (checkRootAccess()) {
                    // Root available - io_uring works via su
                    available = true;
                    LOGD("io_uring available via root");
                }
            }
            break;
        default:
            break;
    }

    return available;
}

bool checkRootAccess() {
    // Method 1: Check for Magisk specific indicators
    if (access("/sbin/.magisk", F_OK) == 0 ||
        access("/debug_ramdisk/.magisk", F_OK) == 0) {
        LOGD("Root detected: Magisk");
        return true;
    }

    // Method 2: Check for KernelSU specific indicators
    if (access("/dev/kernelsu", F_OK) == 0 ||
        access("/sys/kernel/security/kernelsu", F_OK) == 0) {
        LOGD("Root detected: KernelSU");
        return true;
    }

    // Method 3: Try executing su with popen to capture actual output
    // This works for Magisk, KernelSU, APatch, and traditional su
    const char *suPaths[] = {
        "/system/bin/su",
        "/system/xbin/su",
        "/sbin/su",
        "/su/bin/su",
        "/data/adb/magisk/su",
        "/data/adb/ksu/bin/su",
        NULL
    };

    for (int i = 0; suPaths[i] != NULL; i++) {
        if (access(suPaths[i], X_OK) == 0) {
            char cmd[256];
            snprintf(cmd, sizeof(cmd), "%s -c 'id -u' 2>/dev/null", suPaths[i]);
            FILE *fp = popen(cmd, "r");
            if (fp) {
                char buf[32] = {0};
                if (fgets(buf, sizeof(buf), fp) != NULL) {
                    // Root user ID is 0
                    if (atoi(buf) == 0) {
                        pclose(fp);
                        LOGD("Root detected via %s", suPaths[i]);
                        return true;
                    }
                }
                pclose(fp);
            }
        }
    }

    LOGD("Root not detected");
    return false;
}

bool try_io_uring_setup(void) {
    pid_t pid;
    int status;
    int pipefd[2];
    char result = 0;

    // Use fork to safely test syscall - avoids seccomp SIGSYS killing the process
    if (pipe(pipefd) < 0) {
        LOGD("try_io_uring: pipe failed");
        return false;
    }

    pid = fork();
    if (pid < 0) {
        LOGD("try_io_uring: fork failed");
        close(pipefd[0]);
        close(pipefd[1]);
        return false;
    }

    if (pid == 0) {
        // Child process - test io_uring_setup syscall
        // Use standard types to avoid kernel header dependency
        struct {
            unsigned char sq_entries;
            unsigned char cq_entries;
            unsigned int flags;
            unsigned int sq_thread_cpu;
            unsigned int sq_thread_idle;
            unsigned int features;
            unsigned int wq_fd;
            unsigned int resv[3];
            struct {
                unsigned int head;
                unsigned int tail;
                unsigned int ring_mask;
                unsigned int ring_entries;
                unsigned int flags;
                unsigned int array;
                unsigned int resv[3];
            } sq_off;
            struct {
                unsigned int head;
                unsigned int tail;
                unsigned int ring_mask;
                unsigned int ring_entries;
                unsigned int flags;
                unsigned int cqes;
                unsigned int resv[3];
            } cq_off;
        } params;

        // Close read end, keep write end
        close(pipefd[0]);

        memset(&params, 0, sizeof(params));

        int ret = syscall(IO_URING_SETUP_SYS_NUM, 1, &params);
        if (ret >= 0) {
            close(ret);  // close the ring fd
            result = 1;  // success
        } else {
            LOGD("try_io_uring: syscall failed, errno=%d", errno);
            // errno contains the reason (EPERM, ENOSYS, etc.)
            result = (errno == EPERM || errno == EACCES) ? 2 : 0;
        }

        // Write result to pipe
        write(pipefd[1], &result, 1);
        close(pipefd[1]);
        _exit(0);
    }

    // Parent process - wait for child
    close(pipefd[1]);  // close write end

    // Read result from child before waiting (avoids SIGCHLD races)
    ssize_t n = read(pipefd[0], &result, 1);
    close(pipefd[0]);

    // Wait for child and check if it was killed by seccomp
    waitpid(pid, &status, 0);

    if (WIFSIGNALED(status)) {
        int sig = WTERMSIG(status);
        if (sig == SIGSYS) {
            // seccomp blocked the syscall - it's definitely not available
            LOGD("try_io_uring: seccomp blocked syscall 425 (SIGSYS)");
            return false;
        }
        LOGD("try_io_uring: child killed by signal %d", sig);
        return false;
    }

    if (WIFEXITED(status) && WEXITSTATUS(status) != 0) {
        LOGD("try_io_uring: child exited with %d", WEXITSTATUS(status));
        return false;
    }

    // Read successful result from pipe
    if (n == 1 && result == 1) {
        LOGD("try_io_uring: available (syscall succeeded)");
        return true;
    } else if (n == 1 && result == 2) {
        LOGD("try_io_uring: blocked (permission denied)");
    } else {
        LOGD("try_io_uring: not available (result=%d)", result);
    }

    return false;
}

void json2Options(const char *jsonStr, int *argc, char ***argv) {
    cJSON *root, *options, *option, *shortOpts;
    root = cJSON_Parse(jsonStr);

    shortOpts = cJSON_GetObjectItem(root, "shortopts");
    bool isShortOpts = cJSON_IsBool(shortOpts) && (shortOpts->type & cJSON_True);

    options = cJSON_GetObjectItem(root, "options");
    *argc = (cJSON_GetArraySize(options) + 1);
    *argv = (char **) calloc(*argc, sizeof(char *));

    (*argv)[0] = (char *) calloc(ARGV_OPTION_MAX_LENGTH, sizeof(char));
    sprintf((*argv)[0], "%s", getprogname());

    int i = 1;
    cJSON_ArrayForEach(option, options) {
        cJSON *name = cJSON_GetObjectItem(option, "name");
        cJSON *value = cJSON_GetObjectItem(option, "value");

        (*argv)[i] = (char *) calloc(ARGV_OPTION_MAX_LENGTH, sizeof(char));
        sprintf((*argv)[i], isShortOpts ? "-%s" : "--%s", name->valuestring);
        if (cJSON_IsString(value))
            sprintf((*argv)[i] + strlen((*argv)[i]), isShortOpts ? "%s" : "=%s", value->valuestring);

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

int LibMBW::mbw(int argc, char *argv[]) {
    return libFunc.mbw(argc, argv, callbackPtr);
}

LibMBW::LibMBW(const char *func, void *callback) {
    callbackPtr = callback;
    libHandler = dlopen("libmbw.so", RTLD_NOW);
    libFunc.funcPtr = dlsym(libHandler, func);
}

LibMBW::~LibMBW() {
    dlclose(libHandler);
}

int LibCoreLatency::core_latency(int argc, char *argv[]) {
    return libFunc.core_latency(argc, argv, callbackPtr);
}

LibCoreLatency::LibCoreLatency(const char *func, void *callback) {
    callbackPtr = callback;
    libHandler = dlopen("libcore_latency.so", RTLD_NOW);
    libFunc.funcPtr = dlsym(libHandler, func);
}

LibCoreLatency::~LibCoreLatency() {
    dlclose(libHandler);
}

int LibFIO::fio(int argc, char *argv[]) {
    return libFunc.fio(argc, argv, nullptr, callbackPtr);
}

int LibFIO::fio_list_ioengines(char **list_buf) {
    return libFunc.fio_list_ioengines(list_buf);
}

LibFIO::LibFIO(const char *func, void *callback) {
    callbackPtr = callback;
    libHandler = dlopen("libfio.so", RTLD_NOW);
    libFunc.funcPtr = dlsym(libHandler, func);
}

LibFIO::~LibFIO() {
    dlclose(libHandler);
}

char *runFioWithRoot(const char *jsonConfig, const char *fioRunnerPath) {
    char tmpFile[512];
    char resultBuffer[65536];
    pid_t pid;
    int pipefd[2];
    int status;
    FILE *configFile = NULL;

    // Determine temp directory: use HOME (app's data dir) or /data/local/tmp
    const char *homeDir = getenv("HOME");
    if (!homeDir || access(homeDir, W_OK) != 0) {
        homeDir = "/data/local/tmp";
    }

    // Create unique temp file path
    snprintf(tmpFile, sizeof(tmpFile), "%s/fio_config_%d_%d.json",
             homeDir, getpid(), rand() % 10000);

    // Write JSON config to temp file
    configFile = fopen(tmpFile, "w");
    if (!configFile) {
        LOGE("runFioWithRoot: failed to open config file, errno=%d", errno);
        unlink(tmpFile);
        return NULL;
    }
    fprintf(configFile, "%s", jsonConfig);
    fclose(configFile);

    // Create pipe for reading output
    if (pipe(pipefd) < 0) {
        LOGE("runFioWithRoot: pipe failed, errno=%d", errno);
        unlink(tmpFile);
        return NULL;
    }

    pid = fork();
    if (pid < 0) {
        LOGE("runFioWithRoot: fork failed, errno=%d", errno);
        close(pipefd[0]);
        close(pipefd[1]);
        unlink(tmpFile);
        return NULL;
    }

    if (pid == 0) {
        // Child process - run via su
        close(pipefd[0]);  // close read end
        dup2(pipefd[1], STDOUT_FILENO);  // redirect stdout to pipe
        dup2(pipefd[1], STDERR_FILENO);  // redirect stderr to pipe
        close(pipefd[1]);

        // Build the command for su: set LD_LIBRARY_PATH and run fio_runner
        char cmd[2048];
        // LD_LIBRARY_PATH must include the app's native lib dir for dlopen(libfio.so)
        const char *nativeLibDir = getenv("LD_LIBRARY_PATH");
        if (nativeLibDir) {
            snprintf(cmd, sizeof(cmd),
                     "LD_LIBRARY_PATH=%s %s --json-config=%s",
                     nativeLibDir, fioRunnerPath, tmpFile);
        } else {
            snprintf(cmd, sizeof(cmd),
                     "%s --json-config=%s",
                     fioRunnerPath, tmpFile);
        }

        // Exec su -c to run fio_runner as root
        execlp("su", "su", "-c", cmd, NULL);

        // Also try with specific paths for Magisk/KernelSU
        execl("/system/bin/su", "su", "-c", cmd, NULL);
        execl("/system/xbin/su", "su", "-c", cmd, NULL);
        execl("/sbin/su", "su", "-c", cmd, NULL);

        // If exec fails
        _exit(127);
    }

    // Parent process - read output
    close(pipefd[1]);  // close write end

    memset(resultBuffer, 0, sizeof(resultBuffer));
    ssize_t totalRead = 0;
    ssize_t n;

    while ((n = read(pipefd[0], resultBuffer + totalRead,
                     sizeof(resultBuffer) - totalRead - 1)) > 0) {
        totalRead += n;
    }

    close(pipefd[0]);

    // Wait for child to complete
    waitpid(pid, &status, 0);

    // Clean up temp file
    unlink(tmpFile);

    if (totalRead == 0) {
        LOGE("runFioWithRoot: no output from fio_runner (status=%d)", status);
        return NULL;
    }

    // Allocate and return result
    char *result = (char *) calloc(totalRead + 1, sizeof(char));
    if (result) {
        memcpy(result, resultBuffer, totalRead);
        result[totalRead] = '\0';
    }

    return result;
}

#endif
