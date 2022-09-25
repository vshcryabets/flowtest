package com.v2soft.flowtest.io

import com.v2soft.flowtest.base.InputFlowBase
import java.io.Closeable
import java.io.File
import java.io.RandomAccessFile

class FileInputFlow(val file: File) : InputFlowBase, Closeable {
    val fileInput = RandomAccessFile(file, "r")

    override fun getSize(): Long = file.length()

    override fun getBlock(offset: Long, size: Int, array : ByteArray): Int {
        fileInput.seek(offset);
        return fileInput.read(array, 0, size)
    }

    override fun close() {
        fileInput.close()
    }
}