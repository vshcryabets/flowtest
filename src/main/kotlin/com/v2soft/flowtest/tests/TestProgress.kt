package com.v2soft.flowtest.tests

data class TestProgress(
    val maxRounds: Int = 0,
    val currentRound: Int = 0,
    val maxBlocks: Int = 0,
    val requestedBlocks: Int = 0, //reads count
    val requestErrors: Int = 0, // read errors
    val correctBlocks: Int = 0, // hash correct blocks
    val incorrectBlocks: Int = 0 //hash errors
)
