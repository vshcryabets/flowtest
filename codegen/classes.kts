import ce.defs.*

when (target()) {
    ce.defs.Target.Kotlin, ce.defs.Target.Java -> namespace("com.v2soft.flowtest.base")
    else -> namespace("flowtest")
}

setOutputBasePath("../src/main/kotlin/")
dataClass("BufferDescription").apply {
    field("blockSize", DataType.int32)
    field("blockCount", DataType.int32)
    field("lastBlockSize", DataType.int32)
    field("blocks", DataType.array(DataType.int64))
}