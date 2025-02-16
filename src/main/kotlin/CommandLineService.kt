package baniakjr.lwp

import java.nio.file.Paths

class CommandLineService {

    fun run(args: Array<String>) {
        val argList = args.toMutableList()
        when(argList.removeFirst()) {
            "file" -> processFile(argList)
            "cmd" -> processCommand(argList)
            "help" -> processHelp(argList.removeFirst())
            else -> println("Invalid command")
        }
    }

    private fun processHelp(arg: String) {
        when(arg) {
            "file" -> {
                println("Usage: file <path> <hubAddress> [outOnly]")
                println()
                println("Process file")
                println("<path> - Path to file. File must be in JSON format. Android btsnoop_hci.log file converted to json by wireshark")
                println("<hubAddress> - Address of the hub. MAC address in format 00:00:00:00:00:00")
                println("outOnly - Optional. If present, only OUT commands will be processed")
                println()
                println("Examples: ")
                println("file /path/to/file.json 00:00:00:00:00:00")
                println("file /path/to/file.json 00:00:00:00:00:00 outOnly")
            }
            "cmd" -> {
                println("Usage: cmd <command>")
                println()
                println("Process single command")
                println("<command> - Bytes separated by space or colon or without delimiter")
                println()
                println("Examples: ")
                println("cmd 05:00:05:01:06")
                println("cmd 05 00 05 01 06")
                println("cmd 0500050106")
            }
            "help" -> {
                println("Usage: help [<command>]")
                println()
                println("Show help")
                println("<command> - Optional command to show help for")
                println()
                println("Examples: ")
                println("help file")
                println("help cmd")
            }
            else -> {
                println("file <path> <hubAddress> [outOnly] - process file")
                println("cmd <command> - process single command. Bytes separated by space or colon or without delimiter")
                println("help [<command>] - show help")
            }
        }
    }

    private fun processFile(argsList: MutableList<String>) {
        if(argsList.size < 2) {
            println("Usage: file <path> <hubAddress> [outOnly]")
            return
        }
        val path = Paths.get(argsList.removeFirst())
        if (!path.toFile().exists()) {
            println("File does not exist")
            return
        }
        val address = argsList.removeFirst()
        if(address.isEmpty()) {
            println("Hub address is required")
            return
        }
        var outOnly = false
        if(argsList.isNotEmpty()) {
            outOnly = argsList.removeFirst().equals("outOnly", true)
        }
        println(jsonFileReader.readFile(path, outOnly, address))
    }

    private fun processCommand(argsList: MutableList<String>) {
        val command = argsList.removeFirst()
        parseCommand(command)
    }

}