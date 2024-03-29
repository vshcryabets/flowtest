package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.InputFlowBase
import com.v2soft.flowtest.base.InputFlowResponseHandler
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Semaphore

class FileInputFlow(val file: File) : InputFlowBase, Closeable {
    val fileInput = RandomAccessFile(file, "r")
    var requesthandler: InputFlowResponseHandler? = null
    val mutex = Semaphore(1)
    override fun registerAsyncHandler(handler: InputFlowResponseHandler?) {
        requesthandler = handler
    }

    override fun getSize(): Long = file.length()

    override fun getBlock(offset: Long, size: Int, array: ByteArray): Int =
        try {
            mutex.acquire()
            fileInput.seek(offset);
            fileInput.read(array, 0, size)
        } finally {
            mutex.release()
        }

    override fun getBlockAsync(offset: Long, size: Int, array: ByteArray, requestTag: Int) {
        val result = getBlock(offset, size, array)
        requesthandler?.handleBlock(requestTag, result, array)
    }

    override fun close() {
        fileInput.close()
    }
}