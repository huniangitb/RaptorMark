package io.github.devriesl.raptormark.data

import io.github.devriesl.raptormark.Constants.IO_TYPE_MIX_RW_VALUE
import io.github.devriesl.raptormark.Constants.IO_TYPE_RAND_RD_VALUE
import io.github.devriesl.raptormark.Constants.IO_TYPE_RAND_WR_VALUE
import io.github.devriesl.raptormark.Constants.IO_TYPE_SEQ_RD_VALUE
import io.github.devriesl.raptormark.Constants.IO_TYPE_SEQ_WR_VALUE
import kotlinx.serialization.Serializable

@Serializable
enum class TestCase(
    val type: String,
    val weight: Double,
    val ioDepth: Int,
    val numJobs: Int,
    val rwMixWrite: Int? = null,
) {
    Q8T8_SEQ_RD(IO_TYPE_SEQ_RD_VALUE, 0.0, 8, 8),
    Q8T8_SEQ_WR(IO_TYPE_SEQ_WR_VALUE, 0.0, 8 ,8),
    Q1T1_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.5, 1, 1),
    Q2T1_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.25, 2, 1),
    Q4T1_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.0, 4, 1),
    Q4T4_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.25, 4, 4),
    Q8T1_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.0, 8, 1),
    Q8T4_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.0, 8, 4),
    Q8T8_RAND_RD(IO_TYPE_RAND_RD_VALUE, 0.0, 8, 8),
    Q1T1_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.5, 1, 1),
    Q2T1_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.25, 2, 1),
    Q4T1_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.0, 4, 1),
    Q4T4_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.25, 4, 4),
    Q8T1_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.0, 8, 1),
    Q8T4_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.0, 8, 4),
    Q8T8_RAND_WR(IO_TYPE_RAND_WR_VALUE, 0.0, 8, 8),
    Q1T1_MIX_9R1W(IO_TYPE_MIX_RW_VALUE, 0.3/3, 1, 1, 10),
    Q1T1_MIX_7R3W(IO_TYPE_MIX_RW_VALUE, 0.3/3, 1, 1, 30),
    Q1T1_MIX_5R5W(IO_TYPE_MIX_RW_VALUE, 0.3/3, 1, 1, 50),
    Q2T1_MIX_9R1W(IO_TYPE_MIX_RW_VALUE, 0.3/3,  2, 1, 10),
    Q2T1_MIX_7R3W(IO_TYPE_MIX_RW_VALUE, 0.3/3,  2, 1, 30),
    Q2T1_MIX_5R5W(IO_TYPE_MIX_RW_VALUE, 0.3/3,  2, 1, 50),
    Q4T1_MIX_9R1W(IO_TYPE_MIX_RW_VALUE, 0.2/3, 4, 1, 10),
    Q4T1_MIX_7R3W(IO_TYPE_MIX_RW_VALUE, 0.2/3, 4, 1, 30),
    Q4T1_MIX_5R5W(IO_TYPE_MIX_RW_VALUE, 0.2/3, 4, 1, 50),
    Q4T4_MIX_9R1W(IO_TYPE_MIX_RW_VALUE, 0.2/3, 4, 4, 10),
    Q4T4_MIX_7R3W(IO_TYPE_MIX_RW_VALUE, 0.2/3, 4, 4, 30),
    Q4T4_MIX_5R5W(IO_TYPE_MIX_RW_VALUE, 0.2/3, 4, 4, 50),
}

fun TestCase.isSeqRw(): Boolean = this in listOf(
    TestCase.Q8T8_SEQ_RD,
    TestCase.Q8T8_SEQ_WR
)

fun TestCase.isRandRd(): Boolean = this in listOf(
    TestCase.Q1T1_RAND_RD,
    TestCase.Q2T1_RAND_RD,
    TestCase.Q4T1_RAND_RD,
    TestCase.Q4T4_RAND_RD,
    TestCase.Q8T1_RAND_RD,
    TestCase.Q8T4_RAND_RD,
    TestCase.Q8T8_RAND_RD
)

fun TestCase.isRandWr(): Boolean = this in listOf(
    TestCase.Q1T1_RAND_WR,
    TestCase.Q2T1_RAND_WR,
    TestCase.Q4T1_RAND_WR,
    TestCase.Q4T4_RAND_WR,
    TestCase.Q8T1_RAND_WR,
    TestCase.Q8T4_RAND_WR,
    TestCase.Q8T8_RAND_WR
)

fun TestCase.isRandRw(): Boolean = this in listOf(
    TestCase.Q1T1_RAND_RD,
    TestCase.Q2T1_RAND_RD,
    TestCase.Q4T1_RAND_RD,
    TestCase.Q4T4_RAND_RD,
    TestCase.Q8T1_RAND_RD,
    TestCase.Q8T4_RAND_RD,
    TestCase.Q8T8_RAND_RD,
    TestCase.Q1T1_RAND_WR,
    TestCase.Q2T1_RAND_WR,
    TestCase.Q4T1_RAND_WR,
    TestCase.Q4T4_RAND_WR,
    TestCase.Q8T1_RAND_WR,
    TestCase.Q8T4_RAND_WR,
    TestCase.Q8T8_RAND_WR
)

fun TestCase.isQ1T1MixRw(): Boolean = this in listOf(
    TestCase.Q1T1_MIX_9R1W,
    TestCase.Q1T1_MIX_7R3W,
    TestCase.Q1T1_MIX_5R5W
)

fun TestCase.isQ2T1MixRw(): Boolean = this in listOf(
    TestCase.Q2T1_MIX_9R1W,
    TestCase.Q2T1_MIX_7R3W,
    TestCase.Q2T1_MIX_5R5W
)

fun TestCase.isQ4T1MixRw(): Boolean = this in listOf(
    TestCase.Q4T1_MIX_9R1W,
    TestCase.Q4T1_MIX_7R3W,
    TestCase.Q4T1_MIX_5R5W
)

fun TestCase.isQ4T4MixRw(): Boolean = this in listOf(
    TestCase.Q4T4_MIX_9R1W,
    TestCase.Q4T4_MIX_7R3W,
    TestCase.Q4T4_MIX_5R5W
)

fun TestCase.isMixRw(): Boolean = this in listOf(
    TestCase.Q1T1_MIX_9R1W,
    TestCase.Q1T1_MIX_7R3W,
    TestCase.Q1T1_MIX_5R5W,
    TestCase.Q2T1_MIX_9R1W,
    TestCase.Q2T1_MIX_7R3W,
    TestCase.Q2T1_MIX_5R5W,
    TestCase.Q4T1_MIX_9R1W,
    TestCase.Q4T1_MIX_7R3W,
    TestCase.Q4T1_MIX_5R5W,
    TestCase.Q4T4_MIX_9R1W,
    TestCase.Q4T4_MIX_7R3W,
    TestCase.Q4T4_MIX_5R5W,
)
