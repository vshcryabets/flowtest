package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.HashMethod
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MemoryRandomFlowTest {
    @Test
    fun testCRC32Hashes() {
        // allocate 1mb test data
        val memBuffer = MemoryRandomFlow(
            blocksize = 1024,
            blockcount = 1024,
            hashMethod = HashMethod.CRC32)
        val block0Hash = memBuffer.getBlockHash(0)
        assertNotNull(block0Hash)
        assertNotEquals(0, block0Hash.size)
        assertNotEquals(0, block0Hash[0])
        assertNotEquals(0, block0Hash[1])
        assertNotEquals(0, block0Hash[2])
        assertNotEquals(0, block0Hash[3])
    }
}