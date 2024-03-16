package com.v2soft.flowtest.tests

import kotlinx.coroutines.flow.Flow

interface Test {
    fun prepare(): Flow<Pair<Int, Int>>
    fun startTest()
    fun stopTest()
}