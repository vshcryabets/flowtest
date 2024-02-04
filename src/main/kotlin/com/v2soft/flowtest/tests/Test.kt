package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.BufferDescription
import com.v2soft.flowtest.base.InputFlowBase
import java.time.Duration

interface Test {
    fun prepare(inputFlowBase: InputFlowBase, bufferDescription: BufferDescription, notifyPeriod : Duration)
    fun startTest()
    fun stopTest()
}