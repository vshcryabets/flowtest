package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.InputBufferBase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.nio.ByteBuffer
import java.util.concurrent.LinkedBlockingDeque
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
    private var allBlocksRequested = AtomicBoolean(false)
    private var _hashes = mutableListOf<ByteArray>()
    val hashes: List<ByteArray> = _hashes
    private val freeBlocks = LinkedBlockingDeque<ByteArray>()
    private val receivedBlocks = LinkedBlockingDeque<Pair<Int, ByteArray>>()
    private val _progress = MutableStateFlow(TestProgress(
        maxRounds = repeatCount,
        maxBlocks = inputBuffer.getBlocksCount() * repeatCount
    ))
    val progress: StateFlow<TestProgress> = _progress
    private var _statistics: TestStatistics = TestStatistics(
        testTimeMs = 0,
        blocksReceived = 0,
        errorBlocks = 0,
        blocksChecked = 0
    )

    override fun prepare(notifyPeriod: kotlin.time.Duration,
                         callback: (Int, Int) -> Unit) {
        val blocksCount = inputBuffer.getBlocksCount()
        var notifyTime = 0L
        // load hashes from buffer
        repeat(blocksCount) {
            Thread.yield()
            _hashes.add(inputBuffer.getBlockHash(it))
            if (System.currentTimeMillis() - notifyTime > notifyPeriod.inWholeMilliseconds) {
                callback(it, blocksCount)
                notifyTime = System.currentTimeMillis()
            }
        }
        callback(blocksCount, blocksCount)
        // prepare blocks set
    }

    override fun startTest(notifyPeriod: kotlin.time.Duration,
                           progressCallback: (TestProgress) -> Unit,
                           readErrorCallback: (Exception) -> Unit) {
        startTime = System.currentTimeMillis()
        workFlag.set(true)
        allBlocksRequested.set(false)
        _statistics = _statistics.copy(
            testTimeMs = 0,
            blocksReceived = 0,
            errorBlocks = 0,
            blocksChecked = 0
        )

        for (tid in 0..maxHashingThreads - 1) {
            validationThreads[tid] = Thread {
                val crc32 = CRC32()
                val convertBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
                while (workFlag.get()) {
                    if (allBlocksRequested.get() && receivedBlocks.isEmpty()) {
                        println("allBlocksRequested + receivedBlocks.isEmpty")
                        workFlag.set(false)
                    }
                    val receivedBlock = receivedBlocks.poll(50, TimeUnit.MILLISECONDS) ?: continue
                    val expectedHash = hashes[receivedBlock.first]
                    convertBuffer.position(0)
                    convertBuffer.put(expectedHash)
                    convertBuffer.position(0)
                    val expectedHashInt = convertBuffer.getInt()
                    crc32.reset()
                    crc32.update(receivedBlock.second)
                    if (expectedHashInt != crc32.value.toInt()) {
                        _progress.update { it.copy(incorrectBlocks = it.incorrectBlocks + 1) }
                        println("Wrong hash for block ${receivedBlock.first} " +
                                "$expectedHashInt != ${crc32.value.toInt()}")
                    } else {
                        _progress.update { it.copy(correctBlocks = it.correctBlocks + 1) }
                    }
                    freeBlocks.push(receivedBlock.second)
                }
                workFlag.set(false)
            }
            validationThreads[tid]?.start()
        }

        readThread = Thread {
            var readBlockCount = 0
            freeBlocks.clear()
            repeat(maxParallelRequests) {
                freeBlocks.add(ByteArray(inputBuffer.getBlockSize()))
            }
            for (round in 0..repeatCount - 1) {
                _progress.update {
                    it.copy(currentRound = round + 1)
                }
                if (!workFlag.get())
                    break
                for (blockNo in 0..inputBuffer.getBlocksCount() - 1) {
                    readBlockCount++
                    _progress.update { it.copy(requestedBlocks = readBlockCount) }
                    progressCallback(_progress.value)
                    if (!sendReadRequest(blockNo, readErrorCallback))
                        break
                }
            }
            allBlocksRequested.set(true)
            _progress.update {
                it.copy(
                    currentRound = it.maxRounds,
                    requestedBlocks = it.maxBlocks
                )
            }
        }
        readThread?.start()
    }

    fun sendReadRequest(blockNo: Int, readErrorCallback: (Exception) -> Unit): Boolean {
        var freeBuffer: ByteArray? = null
        while (freeBuffer == null) {
            if (!workFlag.get())
                return false
            freeBuffer = freeBlocks.poll(50, TimeUnit.MILLISECONDS)
        }

//        println("Request $blockNo")
        _progress.update { it.copy(requestedBlocks = it.requestedBlocks + 1) }
        val res = inputBuffer.getBlock(blockNo, freeBuffer, 0)
        if (res <= 0) {
            _progress.update { it.copy(requestErrors = it.requestErrors+1) }
            readErrorCallback(Exception("Getbuffer returns $res"))
            return false
        }
//        println("Got $blockNo = $res")
        receivedBlocks.push(blockNo to freeBuffer)
        return true
    }

    override fun cancelTest() {
        workFlag.set(false)
    }

    override fun getStatistics(): TestStatistics = _statistics

    fun join() {
        readThread?.join()
        validationThreads.forEach {
            it?.join()
        }
        val progress = _progress.value
        _statistics = _statistics.copy(
            testTimeMs = System.currentTimeMillis() - startTime,
            errorBlocks = progress.incorrectBlocks,
            blocksReceived = progress.requestedBlocks,
            blocksChecked = progress.correctBlocks
        )
    }
}