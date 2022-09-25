import com.v2soft.flowtest.core.CalculateByFlow
import com.v2soft.flowtest.io.FileInputFlow
import java.io.File

fun main(args: Array<String>) {
    val calculcator = CalculateByFlow()
    val flow = FileInputFlow(File("./LICENSE"))
    val bufferDescription = calculcator.calculateBufferDescriptionByFlow(flow, 1024 * 1)
    println(bufferDescription)
}
