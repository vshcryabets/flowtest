package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.io.MemoryRandomBuffer
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

internal class LinearThreadsTest {

    val invalidated = AtomicBoolean(false)

    @Test
    fun testPrepare() {
        val memBuffer = object : MemoryRandomBuffer(
            blocksize = 1024,
            blocksCount = 1024,
            hashMethod = HashMethod.CRC32
        ) {
            override fun getBlockHash(blockId: Int): ByteArray {
                Thread.sleep(5)
                return super.getBlockHash(blockId)
            }
        }

        val test = LinearTestThreads(memBuffer)
        var notifyCount = 0
        test.prepare(50.milliseconds) { current, max ->
            notifyCount++
            //println("$current from $max")
        }
        assertEquals(1024, test.hashes.size)
        assertTrue(notifyCount < 120)
    }

    @Test
    fun testValidation() {
        val memBuffer = object : MemoryRandomBuffer(
            blocksize = 1024,
            blocksCount = 16,
            hashMethod = HashMethod.CRC32
        ) {
            override fun getBlockHash(blockId: Int): ByteArray {
//                Thread.sleep(5)
                return super.getBlockHash(blockId)
            }
        }

        val test = LinearTestThreads(memBuffer)
        test.prepare(50.milliseconds) { current, max -> }
        test.startTest(
            50.milliseconds,
            {
                println(it)
            },
            {
                test.cancelTest()
            }
        )
    }
}