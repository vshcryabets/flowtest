package com.v2soft.flowtest.base

interface Node {
    fun prepare(blockSize: Int) : BufferDescription
}