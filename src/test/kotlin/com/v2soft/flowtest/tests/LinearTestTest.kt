package com.v2soft.flowtest.tests

import com.v2soft.flowtest.core.CalculateByFlow
import com.v2soft.flowtest.io.MemoryRandomFlow
import kotlinx.coroutines.*
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.Duration
import java.util.concurrent.atomic.AtomicBoolean

internal class LinearTestTest {

    val invalidated = AtomicBoolean(false)

    @Test
    fun test1() {
        val memBuffer = MemoryRandomFlow(1024, 1024)
        val bufferDescription = CalculateByFlow().calculateBufferDescriptionByFlow(memBuffer, 2048)
        val test = LinearTest()
        test.prepare(memBuffer, bufferDescription, Duration.ofSeconds(1))
        test.startTest()
        Thread.sleep(1000)
        test.stopTest()
    }

    suspend fun sender() {
        println("Start sender")
        while (!invalidated.get()) {
            // send request
            println("Send request ${Thread.currentThread().name}")
            delay(1000)
        }
        println("End sender")
     }

    suspend fun receiver() {
        println("Start receiver")
        while (!invalidated.get()) {
            // send request
            println("Get result ${Thread.currentThread().name}")
            delay(1000)
        }
        println("End receiver")
    }

    @Test
    fun test2() {
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        println("A1 ${Thread.currentThread().name}")
        scope.launch {
            println("A2 ${Thread.currentThread().name}")
            launch { sender() }
            delay(100)
            launch { receiver() }
        }
        Thread.sleep(5000)
        invalidated.set(true)


//        println("A2 ${Thread.currentThread().name}")
//        launch { // context of the parent, main runBlocking coroutine
//            println("A3 ${Thread.currentThread().name}")
//        }
//        launch(Dispatchers.Unconfined) { // not confined -- will work with main thread
//            println("A4 ${Thread.currentThread().name}")
//        }
//        launch(Dispatchers.Default) { // will get dispatched to DefaultDispatcher
//            println("A5 ${Thread.currentThread().name}")
//        }
//        launch(newSingleThreadContext("MyOwnThread")) { // will get its own new thread
//            println("A6 ${Thread.currentThread().name}")
//        }

        //scope.cancel()
//        runBlocking { // this: CoroutineScope
//            val job = launch { // launch a new coroutine and continue
//                delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
//                println("World!") // print after delay
//            }
//            job.join()
//            println("Hello") // main coroutine continues while a previous one is delayed
//        }
    }
}