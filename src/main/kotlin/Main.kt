package baniakjr.lwp

import java.nio.file.Paths
import java.util.*

val commandParser = LWPCommandParser()
val jsonFileReader = JsonFileReader()
val formatter = HexFormat.of()

fun main() {
    var exit = false
    printMainMenu()
    while(!exit) {
        val value = readln()
        when(value) {
            "f" -> processFile()
            "file" -> processFile()
            "c" -> processCommand()
            "cmd" -> processCommand()
            "command" -> processCommand()
            "help" -> printHelp()
            "exit" -> exit = true
            else -> println("Invalid command")
        }
    }
}

private fun processCommand() {
    println("Enter a command:")
    val command = readln()
    command.replace(" ", "")
    command.replace(":", "")
    try {
        val result = commandParser.parseCommand(formatter.parseHex(command))
        println(result)
    } catch (e: IllegalArgumentException) {
        println("Invalid command")
    }
}

private fun processFile() {
    println("Enter a file name:")
    val fileName = readln()
    val path = Paths.get(fileName)
    if (!path.toFile().exists()) {
        println("File does not exist")
        return
    } else {
        jsonFileReader.readFile(path, false, "00:00:00:00:00:00")
    }
}

fun printHelp() {
    TODO("Not yet implemented")
}

private fun printMainMenu() {
    println("LWP Command Parser")
    println("Enter a command:")
    println("file")
    println("command")
    println("help")
    println("exit")
}