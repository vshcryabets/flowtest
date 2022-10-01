package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.InputFlowBase
import java.nio.ByteBuffer
import kotlin.random.Random

class MemoryRandomFlow(val blocksize: Int, val blockcount: Int) : InputFlowBase {

    val size = blockcount * blocksize
    val buffer : ByteBuffer = ByteBuffer.allocate(size)

    init {
        val random = Random(System.currentTimeMillis())
        val byteArray = ByteArray(blocksize)
        for (i in 0..blockcount - 1) {
            random.nextBytes(byteArray)
            buffer.put(byteArray)
        }
    }

    override fun getSize(): Long = size.toLong()

    override fun getBlock(offset: Long, size: Int, array: ByteArray): Int {
        buffer.get(offset.toInt(), array, 0, size)
        return size
    }
}