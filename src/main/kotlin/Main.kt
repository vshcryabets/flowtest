import com.v2soft.flowtest.core.CalculateByFlow
import com.v2soft.flowtest.io.FileInputFlow
import com.v2soft.flowtest.io.MemoryRandomFlow
import java.io.File

fun main(args: Array<String>) {
    val calculcator = CalculateByFlow()
    val flow = FileInputFlow(File("./LICENSE"))
    var bufferDescription = calculcator.calculateBufferDescriptionByFlow(flow, 1024 * 1)
    println(bufferDescription)
    val memoryRandomFlow = MemoryRandomFlow(1024, 1024)
    bufferDescription = calculcator.calculateBufferDescriptionByFlow(memoryRandomFlow, 1024)
    println(bufferDescription)
}
