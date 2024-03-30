package com.v2soft.flowtest.tests

import com.v2soft.flowtest.base.HashMethod
import com.v2soft.flowtest.io.MemoryRandomBuffer
import kotlinx.coroutines.*
import org.junit.jupiter.api.Test
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.assertEquals

internal class LinearCoroutinesTest {

    val invalidated = AtomicBoolean(false)

    @Test
    fun testPrepare() {
        val memBuffer = MemoryRandomBuffer(
            blocksize = 1024,
            blocksCount = 1024,
            hashMethod = HashMethod.CRC32)

        val test = LinearTestCoroutines(memBuffer)
        runBlocking {
            test.prepare().collect {
                println(it)
            }
        }
        assertEquals(1024, test.hashes.size)
    }

//    suspend fun sender() {
//        println("Start sender")
//        while (!invalidated.get()) {
//            // send request
//            println("Send request ${Thread.currentThread().name}")
//            delay(1000)
//        }
//        println("End sender")
//     }
//
//    suspend fun receiver() {
//        println("Start receiver")
//        while (!invalidated.get()) {
//            // send request
//            println("Get result ${Thread.currentThread().name}")
//            delay(1000)
//        }
//        println("End receiver")
//    }

    @Test
    fun test2() {
//        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
//        println("A1 ${Thread.currentThread().name}")
//        scope.launch {
//            println("A2 ${Thread.currentThread().name}")
//            launch { sender() }
//            delay(100)
//            launch { receiver() }
//        }
//        Thread.sleep(5000)
//        invalidated.set(true)


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