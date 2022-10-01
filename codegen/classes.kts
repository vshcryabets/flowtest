import ce.defs.*
import generators.obj.input.*

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

declareInterface("InputFlowBase2").apply {
    addMethod(
        "getSize",
        OutputList().apply {
            output("size", DataType.int64)
        }, null
    )
    addMethod(
        "getSize",
        outputs = OutputList().apply {
            output("size", DataType.int64)
            outputReusable("array", DataType.array(DataType.uint8))
        },
        inputs = InputList().apply {
            argument("offset", DataType.int64)
            argument("size", DataType.int32)
        }
    )
}