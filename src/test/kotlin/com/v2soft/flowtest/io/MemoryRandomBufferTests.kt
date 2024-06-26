package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.HashMethod
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MemoryRandomBufferTests {
    @Test
    fun testCRC32Hashes() {
        // allocate 1mb test data
        val memBuffer = MemoryRandomBuffer(
            blocksize = 1024,
            blocksCount = 1024,
            hashMethod = HashMethod.CRC32)
        runBlocking {
            memBuffer.fillRandomData()
            memBuffer.calculateBlockHashes()
        }
        val block0Hash = memBuffer.getBlockHash(0)
        assertNotNull(block0Hash)
        assertNotEquals(0, block0Hash.size)
        assertNotEquals(0, block0Hash[0])
        assertNotEquals(0, block0Hash[1])
        assertNotEquals(0, block0Hash[2])
        assertNotEquals(0, block0Hash[3])
    }

    @Test
    fun testSize() {
        // allocate 1024*128 test data
        val memBuffer = MemoryRandomBuffer(
            blocksize = 1024,
            blocksCount = 128,
            hashMethod = HashMethod.CRC32
        )
        assertEquals(128 * 1024, memBuffer.getSize())
        assertEquals(128, memBuffer.getBlocksCount())
    }

    @Test
    fun testGetBuffer() {
        // allocate 1024*128 test data
        val memBuffer = MemoryRandomBuffer(
            blocksize = 128,
            blocksCount = 1024,
            hashMethod = HashMethod.CRC32
        )
        val buffer = ByteArray(128)
        assertEquals(128, memBuffer.getBlock(0, buffer ,0))
        assertEquals(128, memBuffer.getBlock(1, buffer ,0))
        assertEquals(128, memBuffer.getBlock(2, buffer ,0))
    }
}