package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.base.InputFlowBase
import com.v2soft.flowtest.base.InputFlowResponseHandler
import kotlinx.coroutines.yield
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore
import java.util.zip.CRC32
import kotlin.random.Random

class MemoryRandomFlow(
    private val blocksize: Int,
    private val blockcount: Int,
    private val hashMethod: HashMethod) : InputFlowBase {
    private val mutex = Semaphore(1)
    private val size = blockcount * blocksize
    private val buffer: ByteBuffer = ByteBuffer.allocate(size)
    private var requesthandler: InputFlowResponseHandler? = null
    private val hashes: Array<ByteArray>
    private val hashSize: Int

    init {
        hashSize = when (hashMethod) {
            HashMethod.CRC32 -> 4
            HashMethod.MD5 -> 16
        }
        hashes = Array(blockcount) { ByteArray(hashSize) }
    }

    suspend fun fillRandomData() {
        val random = Random(System.currentTimeMillis())
        val byteArray = ByteArray(blocksize)
        for (i in 0..blockcount - 1) {
            yield()
            random.nextBytes(byteArray)
            buffer.put(byteArray)
        }
    }

    suspend fun calculateBlockHashes() {
        val crc32 = CRC32()
        val buffer = ByteArray(blocksize)

        for (i in 0..blockcount - 1) {
            yield()
            getBlock(i, buffer)
            crc32.reset()
            crc32.update(buffer)
            val result = ByteArray(4)
            result[0] = (crc32.value shr 24) as Byte
            result[1] = (crc32.value shr 16) as Byte
            result[2] = (crc32.value shr 8) as Byte
            result[3] = (crc32.value shr 0) as Byte
            hashes[i] = result
        }
    }

//    override fun registerAsyncHandler(handler: InputFlowResponseHandler?) {
//        requesthandler = handler
//    }

    override fun getSize(): Long = size.toLong()

    override fun getBlock(blockId: Int, array: ByteArray): Int =
        try {
            mutex.acquire()
            val offset = blockId * getBlockSize()
            buffer.get(offset, array, 0, getBlockSize())
            getBlockSize()
        } finally {
            mutex.release()
        }

    override fun getBlockSize(): Int = blocksize

    override fun getBlockHash(blockId: Int): ByteArray = hashes[blockId]

//    override fun getBlockAsync(offset: Long, size: Int, array: ByteArray, requestTag: Int) {
//        val result = getBlock(offset, size, array)
//        requesthandler?.handleBlock(requestTag, result, array)
//    }
}