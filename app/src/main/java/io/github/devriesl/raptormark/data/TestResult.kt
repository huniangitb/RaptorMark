package io.github.devriesl.raptormark.data

sealed class TestResult {
    class SEQUENCE(
        val bandwidth: Int,
    ) : TestResult()
    class RANDOM(
        val ioPerSec: Int,
        val latency: Int,
    ) : TestResult()
    class MIX(
        val rdIoPerSec: Int,
        val wrIoPerSec: Int,
        val latency: Int,
    ) : TestResult()
}
