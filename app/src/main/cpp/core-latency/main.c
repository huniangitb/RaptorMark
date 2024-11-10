#define _GNU_SOURCE

#include <pthread.h>
#include <unistd.h>
#include <stdio.h>
#include <stdlib.h>
#include <stdatomic.h>
#include <time.h>
#include <sched.h>
#include <string.h>

typedef enum {
    Ping,
    Pong,
} State;

typedef struct {
    atomic_int state;
    pthread_barrier_t barrier;
    long first_cpu;
    long second_cpu;
    int num_round_trips;
    long result;
} ThreadArgs;

static void set_affinity(unsigned int cpu_num, pid_t tid) {
    cpu_set_t cpu_set;
    CPU_ZERO(&cpu_set);
    CPU_SET(cpu_num, &cpu_set);
    sched_setaffinity(tid, sizeof(cpu_set_t), &cpu_set);
}

void* second_thread_func(void* arg) {
    ThreadArgs* args = (ThreadArgs*)arg;

    set_affinity(args->second_cpu, gettid());

    pthread_barrier_wait(&args->barrier);

    int num_round_trips = args->num_round_trips;
    int expected = Ping;
    atomic_int* state = &args->state;
    __builtin_prefetch(&num_round_trips, 0, 0);
    __builtin_prefetch(&expected, 1, 0);
    __builtin_prefetch(state, 1, 0);
    for (int i = 0; i < num_round_trips; ++i) {
        while (!atomic_compare_exchange_weak_explicit(state, &expected, Pong, memory_order_relaxed, memory_order_relaxed)) {
            expected = Ping;
        }
    }

    return NULL;
}

void* first_thread_func(void* arg) {
    ThreadArgs* args = (ThreadArgs*)arg;
    struct timespec start, end;

    set_affinity(args->first_cpu, gettid());

    pthread_barrier_wait(&args->barrier);

    clock_gettime(CLOCK_MONOTONIC_RAW, &start);

    int num_round_trips = args->num_round_trips;
    int expected = Pong;
    atomic_int* state = &args->state;
    __builtin_prefetch(&num_round_trips, 0, 0);
    __builtin_prefetch(&expected, 1, 0);
    __builtin_prefetch(state, 1, 0);
    for (int i = 0; i < num_round_trips; ++i) {
        while (!atomic_compare_exchange_weak_explicit(state, &expected, Ping, memory_order_relaxed, memory_order_relaxed)) {
            expected = Pong;
        }
    }

    clock_gettime(CLOCK_MONOTONIC_RAW, &end);

    double total_round_trips = num_round_trips * 2;
    double duration_ns = (double)(end.tv_sec - start.tv_sec) * 1e9 + (double)(end.tv_nsec - start.tv_nsec);
    args->result = (long)((double)duration_ns / total_round_trips + 0.5);

    return NULL;
}

long latency_bench_run(long first_cpu, long second_cpu, long num_cpus) {
    pthread_t first_thread, second_thread;
    int num_round_trips = 30000;

    while(true) {
        int i = rand() % num_cpus;
        if (i != first_cpu && i != second_cpu) {
            set_affinity(i, gettid());
            break;
        }
    }

    ThreadArgs args = {
        .state = Ping,
        .first_cpu = first_cpu,
        .second_cpu = second_cpu,
        .num_round_trips = num_round_trips,
    };

    pthread_barrier_init(&args.barrier, NULL, 2);

    pthread_create(&first_thread, NULL, first_thread_func, &args);
    pthread_create(&second_thread, NULL, second_thread_func, &args);

    pthread_join(first_thread, NULL);
    pthread_join(second_thread, NULL);

    pthread_barrier_destroy(&args.barrier);

    return args.result;
}

int core_latency(int argc, char *argv[], void *callback_ptr) {
    const long num_cpus = sysconf(_SC_NPROCESSORS_ONLN);
    size_t report_size = num_cpus * num_cpus * 32;
    char* result = malloc(report_size);

    for (int count = 0; count < 500; count++) {
        size_t offset = 0;
        for (long i = 0; i < num_cpus; ++i) {
            for (long j = 0; j < num_cpus; ++j) {
                if (i== j) {
                    continue;
                }
                long latency = latency_bench_run(i, j, num_cpus);
                offset += snprintf(result + offset, report_size - offset, "%ld, %ld, %ld\n", i, j, latency);
            }
        }
        ((int (*)(const char *))callback_ptr)(result);
        memset(result, 0, report_size);
    }

    return 0;
}
