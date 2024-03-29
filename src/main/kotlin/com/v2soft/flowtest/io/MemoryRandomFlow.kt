package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.InputFlowBase
import com.v2soft.flowtest.base.InputFlowResponseHandler
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore
import kotlin.random.Random

class MemoryRandomFlow(val blocksize: Int, val blockcount: Int) : InputFlowBase {
    val mutex = Semaphore(1)
    val size = blockcount * blocksize
    val buffer: ByteBuffer = ByteBuffer.allocate(size)
    var requesthandler: InputFlowResponseHandler? = null

    init {
        val random = Random(System.currentTimeMillis())
        val byteArray = ByteArray(blocksize)
        for (i in 0..blockcount - 1) {
            random.nextBytes(byteArray)
            buffer.put(byteArray)
        }
    }

    override fun registerAsyncHandler(handler: InputFlowResponseHandler?) {
        requesthandler = handler
    }

    override fun getSize(): Long = size.toLong()

    override fun getBlock(offset: Long, size: Int, array: ByteArray): Int =
        try {
            mutex.acquire()
            buffer.get(offset.toInt(), array, 0, size)
            size
        } finally {
            mutex.release()
        }


    override fun getBlockAsync(offset: Long, size: Int, array: ByteArray, requestTag: Int) {
        val result = getBlock(offset, size, array)
        requesthandler?.handleBlock(requestTag, result, array)
    }
}