package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.io.MemoryRandomBuffer
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import org.junit.jupiter.api.Test
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

internal class LinearThreadsTest {
    fun prepareMemBuffer() = object : MemoryRandomBuffer(
        blocksize = 1024,
        blocksCount = 1024,
        hashMethod = HashMethod.CRC32
    ) {
        override fun getBlockHash(blockId: Int): ByteArray {
            Thread.sleep(5)
            return super.getBlockHash(blockId)
        }
    }.apply {
        runBlocking {
            fillRandomData()
            calculateBlockHashes()
        }
    }

    @Test
    fun testPrepare() {
        val memBuffer = prepareMemBuffer()
        val test = LinearTestThreads(memBuffer)
        var notifyCount = 0
        test.prepare(50.milliseconds) { current, max ->
            notifyCount++
        }
        assertEquals(1024, test.hashes.size)
        assertTrue(notifyCount < 120)
    }

    @Test
    fun testValidation() {
        val memBuffer = prepareMemBuffer()
        val test = LinearTestThreads(memBuffer, repeatCount = 1024)
        test.prepare(50.milliseconds) { current, max -> }

        test.startTest(
            50.milliseconds,
            {
                if (it.maxBlocks + 5 < it.requestedBlocks)
                    test.cancelTest()
                assertTrue(it.maxBlocks + 10 > it.requestedBlocks, "Requested too much blocks ")
            },
            {
                test.cancelTest()
            }
        )
        test.join()
        val progress = test.progress.value
        println(progress)
        assertEquals(progress.maxBlocks, progress.requestedBlocks)
        assertEquals(progress.maxRounds, progress.currentRound)
        assertEquals(progress.maxBlocks, progress.correctBlocks, "Wrong number of correct blocks")
        assertEquals(0, progress.incorrectBlocks)
    }

    @Test
    fun testSendReadRequestAfterEnd() {
        val memBuffer = prepareMemBuffer()
        val test = LinearTestThreads(memBuffer)
        test.prepare(50.milliseconds) { current, max -> }
        assertFalse(test.sendReadRequest(0, {}), "sendReadRequest() should return false when test not strated")
    }

    @Test
    fun testStatistics() {
        val repeatCount = 1024 * 16
        val memBuffer = prepareMemBuffer()
        val test = LinearTestThreads(memBuffer, repeatCount = repeatCount)
        test.prepare(50.milliseconds) { current, max -> }

        test.startTest(
            50.milliseconds,
            {
            },
            {
                test.cancelTest()
            }
        )
        test.join()
        val stat = test.getStatistics()
        assertTrue(stat.testTimeMs > 0)
        assertEquals(0, stat.errorBlocks)
        assertEquals(memBuffer.getBlocksCount() * repeatCount, stat.blocksReceived)
        assertEquals(memBuffer.getBlocksCount() * repeatCount, stat.blocksChecked)
        // check mean receive speed
        val meanSpeed = stat.blocksReceived / (stat.testTimeMs / 1000.0)
        println("meanSpeed=$meanSpeed KB/s time=${stat.testTimeMs}")
        // check mean CRC process speed
    }
    // TEST cancel
}