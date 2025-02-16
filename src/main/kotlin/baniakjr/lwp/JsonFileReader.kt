package baniakjr.lwp

import baniakjr.lwp.baniakjr.lwp.Direction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import java.io.FileNotFoundException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors
import java.util.stream.Stream

class JsonFileReader {
    private val gson = Gson()
    private val commandParser = LWPCommandParser()
    private val formatterJson: HexFormat = HexFormat.of().withDelimiter(":")

    @Throws(FileNotFoundException::class)
    fun readFile(path: Path, outOnly: Boolean, hubAddress: String): String {
        val result = readFile(path)
        return processToParsedCommand(result, hubAddress, outOnly)
            .map { cmd: ParsedCommand -> cmd.toString(true) }
            .collect(Collectors.joining("\n"))
    }

    @Throws(FileNotFoundException::class)
    fun readFileToCSV(path: Path, outOnly: Boolean, hubAddress: String): String {
        val result = readFile(path)
        return processToParsedCommand(result, hubAddress, outOnly)
            .map { cmd: ParsedCommand -> cmd.toStringCSV() }
            .collect(Collectors.joining("\n"))
    }

    fun processToParsedCommand(
        result: List<OuterElement>,
        hubAddress: String,
        outOnly: Boolean
    ): Stream<ParsedCommand> =
        result.stream().map { element: OuterElement -> convertToBTTraffic(hubAddress, element) }
            .filter { it.isPresent }
            .map { it.get() }
            .filter { !outOnly || it.direction == Direction.OUT }
            .map { this.parseCommand(it) }

    private fun readFile(path: Path): List<OuterElement> {
        val reader = JsonReader(Files.newBufferedReader(path))

        val listType = object : TypeToken<ArrayList<OuterElement?>?>() {}.type
        val result = gson.fromJson<List<OuterElement>>(reader, listType)
        return result
    }

    private fun convertToBTTraffic(hubAddress: String, element: OuterElement): Optional<BTTraffic> {
        val blData = element.source.layers.bluetooth
        val batt = element.source.layers.btatt
        if (blData.destination != hubAddress && blData.source != hubAddress) {
            return Optional.empty()
        }
        if (batt.value == null) {
            return Optional.empty()
        }
        return Optional.of(BTTraffic(if (blData.destination == hubAddress) Direction.OUT else Direction.IN, formatterJson.parseHex(batt.value)))
    }

    private fun parseCommand(btTraffic: BTTraffic): ParsedCommand {
        val parsedCommand = commandParser.parseCommand(btTraffic.message)
        return ParsedCommand(btTraffic.direction, btTraffic.message, parsedCommand)
    }

    @JvmRecord
    data class BTTraffic(val direction: Direction, val message: ByteArray)

}
