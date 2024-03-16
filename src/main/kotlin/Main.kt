fun main(args: Array<String>) {
    if (args.size < 1) {
        println("Flow test")
        println("\t--listen - listen mode")
        println("\t--connect - connect mode")
        return
    }
    if (args[0].equals("--listen")) {

    }
    println(args[0])
//    val calculcator = CalculateByFlow()
//    val flow = FileInputFlow(File("./LICENSE"))
//    var bufferDescription = calculcator.calculateBufferDescriptionByFlow(flow, 1024 * 1)
//    println(bufferDescription)
//    val memoryRandomFlow = MemoryRandomFlow(1024, 1024)
//    bufferDescription = calculcator.calculateBufferDescriptionByFlow(memoryRandomFlow, 1024)
//    println(bufferDescription)
}
