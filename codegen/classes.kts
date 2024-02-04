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

declareInterface("InputFlowResponseHandler").apply {
    addMethod(
        "handleBlock",
        inputs = InputList().apply {
            argument("requestTag", DataType.int32)
            argument("size", DataType.int32)
            argument("array", DataType.array(DataType.uint8))
        }
    )
}

declareInterface("InputFlowBase").apply {
    addMethod(
        "registerAsyncHandler",
        inputs = InputList().apply {
            argument("handler", DataType.userClass("InputFlowResponseHandler", true))
        }
    )
    addMethod(
        "getSize",
        OutputList().apply {
            output("size", DataType.int64)
        }, null
    )
    addMethod(
        "getBlockSize",
        OutputList().apply {
            output("size", DataType.int32)
        }, null
    )
    addMethod(
        "getBlock",
        outputs = OutputList().apply {
            output("size", DataType.int32)
            outputReusable("array", DataType.array(DataType.uint8))
        },
        inputs = InputList().apply {
            argument("blockId", DataType.int32)
        }
    )
    addMethod(
        "getBlockAsync",
        inputs = InputList().apply {
            argument("offset", DataType.int64)
            argument("size", DataType.int32)
            argument("array", DataType.array(DataType.uint8))
            argument("requestTag", DataType.int32)
        }
    )
}