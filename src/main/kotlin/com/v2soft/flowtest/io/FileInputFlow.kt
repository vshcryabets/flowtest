package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.base.InputBufferBase
import com.v2soft.flowtest.base.InputFlowResponseHandler
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile
import java.util.concurrent.Semaphore

class FileInputFlow(
    private val file: File,
    private val blockSize: Int,
    hashMethod: HashMethod
) : InputBufferBase, Closeable {

    val fileInput = RandomAccessFile(file, "r")
    var requesthandler: InputFlowResponseHandler? = null
    val mutex = Semaphore(1)

    override fun getSize(): Long = file.length()
    override fun getBlocksCount(): Int {
        TODO("Not yet implemented")
    }

    override fun getBlock(blockId: Int, array: ByteArray, offset: Int): Int =
        try {
            mutex.acquire()
            fileInput.seek(blockId * (blockSize.toLong()));
            fileInput.read(array, offset, blockSize)
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