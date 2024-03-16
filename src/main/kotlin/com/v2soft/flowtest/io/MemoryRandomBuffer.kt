package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.base.InputBufferBase
import kotlinx.coroutines.yield
import java.nio.ByteBuffer
import java.util.concurrent.Semaphore
import java.util.zip.CRC32
import kotlin.random.Random

class MemoryRandomBuffer(
    private val blocksize: Int,
    private val blocksCount: Int,
    hashMethod: HashMethod
) : InputBufferBase {
    private val mutex = Semaphore(1)
    private val size = blocksCount * blocksize
    private val buffer: ByteBuffer = ByteBuffer.allocate(size)
    private val hashes: Array<ByteArray>
    private val hashSize: Int

    init {
        hashSize = when (hashMethod) {
            HashMethod.CRC32 -> 4
            HashMethod.MD5 -> 16
        }
        hashes = Array(blocksCount) { ByteArray(hashSize) }
    }

    suspend fun fillRandomData() {
        val random = Random(System.currentTimeMillis())
        val byteArray = ByteArray(blocksize)
        for (i in 0..blocksCount - 1) {
            yield()
            random.nextBytes(byteArray)
            buffer.put(byteArray)
        }
    }

    suspend fun calculateBlockHashes() {
        val crc32 = CRC32()
        val buffer = ByteArray(blocksize)
        for (i in 0..blocksCount - 1) {
            yield()
            getBlock(i, buffer)
            crc32.reset()
            crc32.update(buffer)
            val convertBuffer = ByteBuffer.allocate(Int.SIZE_BYTES)
            convertBuffer.putInt(crc32.value.toInt())
            hashes[i] = convertBuffer.array()
        }
    }

    override fun getSize(): Long = size.toLong()
    override fun getBlocksCount(): Int = blocksCount

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
}