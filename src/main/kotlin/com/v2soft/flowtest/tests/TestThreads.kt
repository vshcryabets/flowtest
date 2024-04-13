package com.v2soft.flowtest.tests

import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

interface TestThreads {
    fun prepare(notifyPeriod: Duration,
                callback: (Int, Int) -> Unit)
    fun startTest(notifyPeriod: Duration,
                  callback: (TestProgress) -> Unit,
                  readErrorCallback: (Exception) -> Unit)
    fun cancelTest()
    fun getStatistics(): TestStatistics
}