package baniakjr.lwp

import baniakjr.lwp.baniakjr.lwp.Direction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.nio.file.Path
import java.nio.file.Paths


class JsonFileReaderTest {

    private val jsonFileReader = JsonFileReader()

    @Test
    fun readFileOut() {
        val inputFilePath: Path = Paths.get("src", "test", "resources", "input.json")

        val result = jsonFileReader.readFile(inputFilePath, true, "00:00:00:00:00:01")

        assertThat(result).isNotNull().isEqualTo("OUT [0500010102]                                       HubProperty NAME ENABLE_UPDATES")
    }

    @Test
    fun readFileBoth() {
        val inputFilePath: Path = Paths.get("src", "test", "resources", "input.json")

        val result = jsonFileReader.readFile(inputFilePath, false, "00:00:00:00:00:01")

        assertThat(result).isNotNull().isEqualTo("OUT [0500010102]                                       HubProperty NAME ENABLE_UPDATES\n" +
                "IN  [0500050106]                                       Error from: HUB_PROPERTY code: INVALID_USE")
    }

    @Test
    fun readFileToCSV() {
        val inputFilePath: Path = Paths.get("src", "test", "resources", "input.json")

        val result = jsonFileReader.readFileToCSV(inputFilePath, false, "00:00:00:00:00:01")

        assertThat(result).isNotNull().isEqualTo("OUT;0500010102;HubProperty NAME ENABLE_UPDATES;HUB_PROPERTY\n" +
                "IN;0500050106;Error from: HUB_PROPERTY code: INVALID_USE;ERROR")
    }

    @Test
    fun processToParsedCommandOut() {
        val (address2, inputList) = prepareInput()
        val result = jsonFileReader.processToParsedCommand(inputList, address2, true)

        assertThat(result).hasSize(1).element(0).isNotNull().isEqualTo(ParsedCommand(Direction.OUT, byteArrayOf(0x05,0x00,0x01,0x01,0x02), "HubProperty NAME ENABLE_UPDATES"))
    }

    @Test
    fun processToParsedCommandBoth() {
        val (address2, inputList) = prepareInput()
        val result = jsonFileReader.processToParsedCommand(inputList, address2, false)

        assertThat(result).hasSize(2).containsExactly(ParsedCommand(Direction.OUT, byteArrayOf(0x05,0x00,0x01,0x01,0x02), "HubProperty NAME ENABLE_UPDATES"),
            ParsedCommand(Direction.IN, byteArrayOf(0x05,0x00,0x05,0x01,0x06), "Error from: HUB_PROPERTY code: INVALID_USE"))
    }

    @Test
    fun processToParsedCommandNone() {
        val (_, inputList) = prepareInput()
        val result = jsonFileReader.processToParsedCommand(inputList, "00:00:00:00:00:02", false)

        assertThat(result).isEmpty()
    }

    private fun prepareInput(): Pair<String, List<OuterElement>> {
        val address1 = "00:00:00:00:00:00"
        val address2 = "00:00:00:00:00:01"
        val inputList = listOf(
            prepareOuterElement(address1, address2, "05:00:01:01:02"),
            prepareOuterElement(address2, address1, "05:00:05:01:06")
        )
        return Pair(address2, inputList)
    }

    private fun prepareOuterElement(src: String, dst: String, value: String): OuterElement {
        return OuterElement(OuterElement.SourceBT(OuterElement.Layer(OuterElement.BluetoothElement(src, dst), OuterElement.BtAttElement(value))))
    }
}