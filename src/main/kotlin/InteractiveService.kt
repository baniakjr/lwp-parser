package baniakjr.lwp

import java.nio.file.Paths

class InteractiveService {

    fun run() {
        var exit = false
        printHelp()
        while(!exit) {
            printInputPrefix()
            val value = readln()
            when(value) {
                "f" -> processFile()
                "file" -> processFile()
                "c" -> processCommand()
                "cmd" -> processCommand()
                "command" -> processCommand()
                "h" -> printHelp()
                "help" -> printHelp()
                "exit" -> exit = true
                else -> println("Invalid command")
            }
        }
    }

    private fun printInputPrefix() {
        print(">")
    }

    private fun processCommand() {
        println("Enter command bytes:")
        printInputPrefix()
        val command = readln()
        parseCommand(command)
    }

    private fun processFile() {
        println("Enter file name:")
        printInputPrefix()
        val fileName = readln()
        val path = Paths.get(fileName)
        if (!path.toFile().exists()) {
            println("File does not exist")
            return
        } else {
            val address = readHubAddress()
            if(address.isEmpty()) {
                println("Hub address is required")
                return
            }
            val outOnly = readDirection()
            println(jsonFileReader.readFile(path, outOnly, address))
        }
    }

    private fun readHubAddress(): String {
        println("Enter hub address:")
        printInputPrefix()
        return readln()
    }

    private fun readDirection(): Boolean {
        println("Should incoming (Commands send by the hub to device) commands be processed (Y/N):")
        printInputPrefix()
        while (true) {
            val value = readln()
            when(value) {
                "y" -> return false
                "Y" -> return false
                "n" -> return true
                "N" -> return true
                else -> {
                    println("Invalid option please enter Y or N")
                    printInputPrefix()
                }
            }
        }
    }

    private fun printHelp() {
        println("LWP Command Parser")
        println("Available a commands:")
        println("* file or f - process file. Android btsnoop_hci.log file converted to json by wireshark")
        println("* command or cmd or c - process single command. Bytes separated by space or colon or without delimiter")
        println("* help or h - show this help")
        println("* exit - exit program")
    }

}