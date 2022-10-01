package com.v2soft.flowtest.core

import com.v2soft.flowtest.base.BufferDescription
import com.v2soft.flowtest.base.InputFlowBase
import java.util.zip.CRC32

class CalculateByFlow {
    fun calculateBufferDescriptionByFlow(flow: InputFlowBase, blockSize: Int): BufferDescription {
        val flowSize = flow.getSize()
        val lastBlockSize = (flowSize % blockSize).toInt()
        val blockCount = (flowSize / blockSize).toInt() + (if (lastBlockSize > 0) 1 else 0)
        val crc32array = LongArray(blockCount)
        val buffer = ByteArray(blockSize)
        val crc32 = CRC32()

        for (i in 0..blockCount - 2) {
            flow.getBlock(i.toLong() * blockSize.toLong(), blockSize, buffer)
            crc32.reset()
            crc32.update(buffer)
            crc32array[i] = crc32.value
        }
        flow.getBlock((blockCount - 1).toLong() * blockSize.toLong(), lastBlockSize, buffer)
        crc32.reset()
        crc32.update(buffer)
        crc32array[blockCount - 1] = crc32.value

        return BufferDescription(blockSize, blockCount, lastBlockSize, crc32array)
    }
}