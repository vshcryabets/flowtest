package com.v2soft.flowtest.tests

data class TestStatistics(
    val testTimeMs: Long,
    val blocksReceived: Int,
    val errorBlocks: Int,
    val blocksChecked: Int,
)
