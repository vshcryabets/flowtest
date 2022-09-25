package com.v2soft.flowtest.base

interface InputFlowBase {
    fun getSize() : Long
    fun getBlock(offset: Long, size: Int, array : ByteArray): Int
}