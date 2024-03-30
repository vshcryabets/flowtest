package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.InputBufferBase
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import java.time.Duration
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

class LinearTestCoroutines(
    private val inputBuffer: InputBufferBase,
    private val maxParallelRequests: Int = 10,
    private val repeatCount: Int = 1,
) : TestCoroutines {
    private var readThread: Thread? = null
    private var validationThread: Thread? = null
    private var startTime: Long = 0
    private var workFlag = AtomicBoolean(false)
    private lateinit var requestStartArray: IntArray
    private lateinit var countArray: ByteArray
    private lateinit var responseArray: IntArray
    private val buffersListMutex = Semaphore(1)
    private val buffersList = LinkedList<ByteArray>()
    private val validationListMutex = Semaphore(1)
    private val validationList = LinkedList<Pair<Int, ByteArray>>()
    private var notifyPeriod = Duration.ofSeconds(1L)
    var hashes: List<ByteArray> = emptyList()
    var requestsSet: Set<Int> = emptySet()

    override fun prepare() = flow {
        val blocksCount = inputBuffer.getBlocksCount()
        requestStartArray = IntArray(blocksCount)
        responseArray = IntArray(blocksCount)
        countArray = ByteArray(blocksCount)

        // load hashes from buffer
        hashes = mutableListOf<ByteArray>().apply {
            repeat(blocksCount) {
                yield()
                add(inputBuffer.getBlockHash(it))
                emit(Pair(it, blocksCount))
            }
        }
        emit(Pair(blocksCount, blocksCount))
        // prepare blocks set

    }

    override fun startTest() = flow<Pair<Int,Int>> {
        startTime= System.currentTimeMillis()
        workFlag.set(true)
        val maxBlocks = repeatCount * inputBuffer.getBlocksCount()
        val buffers = ConcurrentLinkedDeque<ByteArray>()
        repeat(maxParallelRequests) {
            buffers.add(ByteArray(inputBuffer.getBlockSize()))
        }
        repeat(repeatCount) { fullRepeat ->
            yield()
            repeat(inputBuffer.getBlocksCount()) { blockNo ->
                yield()
                val freeBuffer = buffers.poll()
                //var res = inputBuffer.getBlock(blockNo, buffers[freeBuffer], 0)

            }
        }
    }

    override fun cancelTest() {
        workFlag.set(false)
        readThread?.join()
        validationThread?.join()
    }
}