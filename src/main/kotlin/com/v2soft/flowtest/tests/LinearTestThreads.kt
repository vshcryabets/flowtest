package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.InputBufferBase
import java.nio.ByteBuffer
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.zip.CRC32

class LinearTestThreads(
    private val inputBuffer: InputBufferBase,
    private val maxParallelRequests: Int = 10,
    private val repeatCount: Int = 1,
    private val maxHashingThreads: Int = 4,
) : TestThreads {
    private var readThread: Thread? = null
    private var validationThreads: Array<Thread?> = arrayOfNulls(maxHashingThreads)
    private var startTime: Long = 0
    private var workFlag = AtomicBoolean(false)
    var hashes: List<ByteArray> = emptyList()
    private val freeBlocks = LinkedBlockingDeque<ByteArray>()
    private val receivedBlocks = LinkedBlockingDeque<Pair<Int, ByteArray>>()

    override fun prepare(notifyPeriod: kotlin.time.Duration,
                         callback: (Int, Int) -> Unit) {
        val blocksCount = inputBuffer.getBlocksCount()
        var notifyTime = 0L
        // load hashes from buffer
        hashes = mutableListOf<ByteArray>().apply {
            repeat(blocksCount) {
                Thread.yield()
                add(inputBuffer.getBlockHash(it))
                if (System.currentTimeMillis() - notifyTime > notifyPeriod.inWholeMilliseconds) {
                    callback(it, blocksCount)
                    notifyTime = System.currentTimeMillis()
                }
            }
        }
        callback(blocksCount, blocksCount)
        // prepare blocks set

    }

    override fun startTest(notifyPeriod: kotlin.time.Duration,
                           progressCallback: (TestProgress) -> Unit,
                           readErrorCallback: (Exception) -> Unit) {
        startTime= System.currentTimeMillis()
        workFlag.set(true)

        for (tid in 0..maxHashingThreads - 1) {
            validationThreads[tid] = Thread {
                val crc32 = CRC32()
                val convertBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
                while (workFlag.get()) {
                    val receivedBlock = receivedBlocks.poll(50, TimeUnit.MILLISECONDS) ?: continue
                    val expectedHash = hashes[receivedBlock.first]
                    convertBuffer.position(0)
                    convertBuffer.put(expectedHash)
                    convertBuffer.position(0)
                    val expectedHashInt = convertBuffer.getInt()
                    crc32.reset()
                    crc32.update(receivedBlock.second)
                    if (expectedHashInt != crc32.value.toInt()) {
                        println("Wrong hash for block ${receivedBlock.first}")
                    }
                    freeBlocks.push(receivedBlock.second)
                }
            }
            validationThreads[tid]?.start()
        }

        readThread = Thread {
            freeBlocks.clear()
            repeat(maxParallelRequests) {
                freeBlocks.add(ByteArray(inputBuffer.getBlockSize()))
            }
            while (workFlag.get()) {
                for (round in 0..repeatCount-1) {
                    if (!workFlag.get())
                        break
                    for (blockNo in 0..inputBuffer.getBlocksCount()-1) {
                        if (!workFlag.get())
                            break
                        val freeBuffer = freeBlocks.poll()
                        println("Request $blockNo")
                        var res = inputBuffer.getBlock(blockNo, freeBuffer, 0)
                        if (res <= 0) {
                            readErrorCallback(Exception("Getbuffer returns $res"))
                            workFlag.set(false)
                            break
                        }
                        println("Got $blockNo = $res")
                        receivedBlocks.push(blockNo to freeBuffer)
                    }
                }
            }
        }
        readThread?.start()

    }

    override fun cancelTest() {
        workFlag.set(false)
        readThread?.join()
        validationThreads.forEach {
            it?.join()
        }
    }

    override fun getStatistics(): TestStatistics {
        TODO("Not yet implemented")
    }
}