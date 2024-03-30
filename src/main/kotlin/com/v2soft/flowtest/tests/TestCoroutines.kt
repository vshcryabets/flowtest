package com.v2soft.flowtest.tests

import kotlinx.coroutines.flow.Flow

interface TestCoroutines {
    fun prepare(): Flow<Pair<Int, Int>>
    fun startTest(): Flow<Pair<Int, Int>>
    fun cancelTest()
}