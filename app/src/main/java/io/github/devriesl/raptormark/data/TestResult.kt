package io.github.devriesl.raptormark.data

sealed class TestResult {
    class SEQUENCE(
        val bandwidth: Int,
    ) : TestResult() {
        override fun calculateScore(): Double {
            return 0.0
        }
    }

    class RANDOM(
        val ioPerSec: Int,
        val latency: Int,
    ) : TestResult() {
        override fun calculateScore(): Double {
            return ioPerSec * 0.25
        }
    }

    class MIX(
        val rdIoPerSec: Int,
        val wrIoPerSec: Int,
        val latency: Int,
    ) : TestResult() {
        override fun calculateScore(): Double {
            return (rdIoPerSec + wrIoPerSec) * 0.5
        }
    }

    abstract fun calculateScore(): Double
}
