package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.BufferDescription
import com.v2soft.flowtest.base.InputFlowBase
import com.v2soft.flowtest.base.InputFlowResponseHandler
import java.time.Duration
import java.util.*
import java.util.concurrent.Semaphore
import java.util.concurrent.atomic.AtomicBoolean

class LinearTest : Test {
    private var readThread: Thread? = null
    private var validationThread: Thread? = null
    private var inputFlowBase: InputFlowBase? = null
    private var bufferDescription: BufferDescription? = null
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

    override fun prepare(
        inputFlowBase: InputFlowBase,
        bufferDescription: BufferDescription,
        notifyPeriod: Duration
    ) {
        this.inputFlowBase = inputFlowBase
        this.bufferDescription = bufferDescription
        requestStartArray = IntArray(bufferDescription.blockCount)
        responseArray = IntArray(bufferDescription.blockCount)
        countArray = ByteArray(bufferDescription.blockCount)
//        inputFlowBase.registerAsyncHandler(object : InputFlowResponseHandler {
//            override fun handleBlock(requestTag: Int, size: Int, array: ByteArray) {
//                val current = (System.currentTimeMillis() - startTime).toInt()
//                responseArray[requestTag] += requestStartArray[requestTag] - current
//                try {
//                    validationListMutex.acquire()
//                    validationList.push(Pair(requestTag, array))
//                } finally {
//                    validationListMutex.release()
//                }
//                // notify about changes
//                // TODO validate result
//                releaseBuffer(array)
//            }
//        })
    }

    override fun startTest() {
        workFlag.set(true)
        val bufferDescription = bufferDescription ?: throw IllegalStateException("bufferDescription is null")
        val inputFlowBase = inputFlowBase ?: throw IllegalStateException("inputFlowBase is null")
        readThread = Thread {
            var pos = 0
            startTime = System.currentTimeMillis()
            while (workFlag.get()) {
                requestStartArray[pos] = (System.currentTimeMillis() - startTime).toInt()
//                inputFlowBase.getBlockAsync(
//                    pos,
//                    bufferDescription.blockSize,
//                    getFreeBuffer(), pos
//                )
                pos++
                if (pos >= bufferDescription.blockCount) pos = 0
            }
        }
        readThread?.start()

        validationThread = Thread {

        }
        validationThread?.start()
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
        val bufferDescription = bufferDescription ?: throw IllegalStateException("bufferDescription is null")
        try {
            buffersListMutex.acquire()
            if (buffersList.isEmpty()) {
                return ByteArray(bufferDescription.blockSize)
            } else {
                return buffersList.pop()
            }
        } finally {
            buffersListMutex.release()
        }
    }

    override fun stopTest() {
        workFlag.set(false)
        readThread?.join()
        validationThread?.join()
    }
}