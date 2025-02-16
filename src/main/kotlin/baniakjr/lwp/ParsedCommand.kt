package baniakjr.lwp

import baniakjr.lwp.baniakjr.lwp.Direction
import java.util.HexFormat

@JvmRecord
data class ParsedCommand(val direction: Direction, val message: ByteArray, val parsedString: String) {
    fun toString(addOriginal: Boolean): String {
        val direction = directionWithPadding()
        val original = if (addOriginal) messageWithPadding() else ""
        return "$direction$original $parsedString"
    }

    fun toStringCSV(): String {
        val cmd = formatter.formatHex(message)
        return "$direction;$cmd;$parsedString;$commandName"
    }

    private fun messageWithPadding(): String {
        val cmd = formatter.formatHex(message)
        return String.format("%-50s", "[$cmd]")
    }

    private fun directionWithPadding(): String {
        return String.format("%-4s", direction)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ParsedCommand

        if (direction != other.direction) return false
        if (!message.contentEquals(other.message)) return false
        if (parsedString != other.parsedString) return false

        return true
    }

    override fun hashCode(): Int {
        var result = direction.hashCode()
        result = 31 * result + message.contentHashCode()
        result = 31 * result + parsedString.hashCode()
        return result
    }

    private val commandName: String
        get() {
            if (message.size < 3) {
                return "N/A"
            }
            val command = LWPByteValue.fromByte(
                Command::class.java, message[Command.IN_MESSAGE_INDEX]
            )
            return command?.name ?: formatter.toHexDigits(message[Command.IN_MESSAGE_INDEX])
        }

    companion object {
        private val formatter: HexFormat = HexFormat.of()
    }
}