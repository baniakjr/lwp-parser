package baniakjr.lwp

import baniakjr.lwp.baniakjr.lwp.Direction
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import java.util.stream.Stream

class ParsedCommandTest {

    @ParameterizedTest
    @MethodSource("provideToStringData")
    fun testToString(direction: Direction, payload: ByteArray, parsed: String, expectedResult: String) {
        val parseCommand = ParsedCommand(direction, payload, parsed)

        val result = parseCommand.toString(true)

        assertThat(result).isEqualTo(expectedResult)
    }

    @ParameterizedTest
    @MethodSource("provideToStringCSVData")
    fun toStringCSV(direction: Direction, payload: ByteArray, parsed: String, expectedResult: String) {
        val parseCommand = ParsedCommand(direction, payload, parsed)

        val result = parseCommand.toStringCSV()

        assertThat(result).isEqualTo(expectedResult)
    }

    companion object {
        @JvmStatic
        fun provideToStringData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Direction.OUT, byteArrayOf(0x01, 0x02, 0x03), "parsed", "OUT [010203]                                           parsed"),
                Arguments.of(Direction.IN, byteArrayOf(0x01, 0x02, 0x03, 0x04), "parsed2", "IN  [01020304]                                         parsed2")
            )
        }

        @JvmStatic
        fun provideToStringCSVData(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(Direction.OUT, byteArrayOf(0x01, 0x02, 0x03), "parsed", "OUT;010203;parsed;" + Command.ALERT),
                Arguments.of(Direction.IN, byteArrayOf(0x01, 0x02, 0x05, 0x04), "parsed2", "IN;01020504;parsed2;" + Command.ERROR),
                Arguments.of(Direction.IN, byteArrayOf(0x01, 0x02), "parsed3", "IN;0102;parsed3;N/A")
            )
        }
    }
}