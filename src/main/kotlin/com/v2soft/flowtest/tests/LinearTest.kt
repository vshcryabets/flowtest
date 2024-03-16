package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.InputBufferBase
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.yield
import java.time.Duration
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

class LinearTest(
    private val inputBuffer: InputBufferBase
) : Test {
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
    }

    override fun startTest() {
//        workFlag.set(true)
//        val bufferDescription = bufferDescription ?: throw IllegalStateException("bufferDescription is null")
//        val inputFlowBase = inputBuffer ?: throw IllegalStateException("inputFlowBase is null")
//        readThread = Thread {
//            var pos = 0
//            startTime = System.currentTimeMillis()
//            while (workFlag.get()) {
//                requestStartArray[pos] = (System.currentTimeMillis() - startTime).toInt()
////                inputFlowBase.getBlockAsync(
////                    pos,
////                    bufferDescription.blockSize,
////                    getFreeBuffer(), pos
////                )
//                pos++
//                if (pos >= bufferDescription.blockCount) pos = 0
//            }
//        }
//        readThread?.start()
//
//        validationThread = Thread {
//
//        }
//        validationThread?.start()
    }

    private fun releaseBuffer(buffer: ByteArray) {
        try {
            buffersListMutex.acquire()
            buffersList.push(buffer)
        } finally {
            buffersListMutex.release()
        }
    }

    private fun getFreeBuffer(): ByteArray {
//        val bufferDescription = bufferDescription ?: throw IllegalStateException("bufferDescription is null")
//        try {
//            buffersListMutex.acquire()
//            if (buffersList.isEmpty()) {
//                return ByteArray(bufferDescription.blockSize)
//            } else {
//                return buffersList.pop()
//            }
//        } finally {
//            buffersListMutex.release()
//        }
        TODO("Not implemented")
    }

    override fun stopTest() {
        workFlag.set(false)
        readThread?.join()
        validationThread?.join()
    }
}