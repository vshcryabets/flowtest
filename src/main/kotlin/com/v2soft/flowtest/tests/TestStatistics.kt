package com.v2soft.flowtest.tests

data class TestStatistics(
    val testTimeMs: Long,
    val blocksReceived: Long,
    val errorBlocks: Long,
)
