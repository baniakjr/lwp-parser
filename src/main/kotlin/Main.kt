package baniakjr.lwp

import java.util.*

val commandParser = LWPCommandParser()
val jsonFileReader = JsonFileReader()
val formatter: HexFormat = HexFormat.of()

fun main(args: Array<String>) {
    if(args.isNotEmpty()) {
        CommandLineService().run(args)
    } else {
        InteractiveService().run()
    }

}

internal fun parseCommand(command: String) {
    command.replace(" ", "")
    command.replace(":", "")
    try {
        val result = commandParser.parseCommand(formatter.parseHex(command))
        println(result)
    } catch (e: IllegalArgumentException) {
        println("Invalid command")
    }
}
