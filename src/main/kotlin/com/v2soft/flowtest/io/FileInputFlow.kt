package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.base.InputFlowBase
import com.v2soft.flowtest.base.InputFlowResponseHandler
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Semaphore

class FileInputFlow(
    private val file: File,
    private val blockSize: Int,
    hashMethod: HashMethod
) : InputFlowBase, Closeable {

    val fileInput = RandomAccessFile(file, "r")
    var requesthandler: InputFlowResponseHandler? = null
    val mutex = Semaphore(1)

    override fun getSize(): Long = file.length()

    override fun getBlock(blockId: Int, array: ByteArray): Int =
        try {
            mutex.acquire()
            fileInput.seek(blockId * (blockSize.toLong()));
            fileInput.read(array, 0, blockSize)
        } finally {
            mutex.release()
        }

    override fun getBlockSize(): Int = blockSize

    override fun getBlockHash(blockId: Int): ByteArray {
        TODO("Not yet implemented")
    }

//    override fun getBlockAsync(offset: Long, size: Int, array: ByteArray, requestTag: Int) {
//        val result = getBlock(offset, size, array)
//        requesthandler?.handleBlock(requestTag, result, array)
//    }

    override fun close() {
        fileInput.close()
    }
}