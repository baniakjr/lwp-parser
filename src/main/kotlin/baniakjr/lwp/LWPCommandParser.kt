package baniakjr.lwp

import baniakjr.lwp.LWPByteValue.Companion.fromByte
import baniakjr.lwp.LWPMask.Companion.fromByte
import java.util.*

class LWPCommandParser {

    private val formatter: HexFormat = HexFormat.of()

    fun parseCommand(message: ByteArray): String {
        if (message.size < Command.IN_MESSAGE_INDEX + 1) {
            return formatter.formatHex(message)
        }
        val commandType = fromByte(
            Command::class.java, message[Command.IN_MESSAGE_INDEX]
        )
        if (commandType == null) {
            return formatter.formatHex(message)
        }
        val commandDescription = when (commandType) {
            Command.HUB_PROPERTY -> processHubPropertyData(message)
            Command.ALERT -> processAlert(message)
            Command.ATTACHED_IO -> processAttachedIO(message)
            Command.ERROR -> processError(message)
            Command.PORT_INFORMATION_REQUEST -> processPortInformationRequest(message)
            Command.PORT_MODE_INFORMATION_REQUEST -> processPortModeRequest(message)
            Command.PORT_INPUT_FORMAT_SETUP_SINGLE -> processPortInputFormatSetupSingle(message)
            Command.PORT_INFORMATION -> processPortInformation(message)
            Command.PORT_MODE_INFORMATION -> processPortModeInformation(message)
            Command.PORT_VALUE_SINGLE -> processPortValueSingle(message)
            Command.PORT_INPUT_FORMAT_SINGLE -> processInputFormatSingle(message)
            Command.VIRTUAL_PORT_SETUP -> virtualPortSetup(message)
            Command.PORT_OUTPUT -> portOutputCommand(message)
            Command.PORT_OUTPUT_COMMAND_FEEDBACK -> portOutputCommandFeedback(message)
            else -> ""
        }
        return commandDescription.ifBlank { formatter.formatHex(message) }
    }

    private fun <C> getNameIfKnown(lwpClass: Class<C>, value: Byte): String where C : Enum<C>?, C : LWPByteValue? {
        val result = fromByte(lwpClass, value)
        return result?.name ?: formatter.toHexDigits(value)
    }

    private fun processHubPropertyData(message: ByteArray): String {
        if (message.size < HubProperty.MSG_WO_DATA_LENGTH) {
            return ""
        }
        val propertyId = getNameIfKnown(
            HubProperty::class.java,
            message[HubProperty.IN_MESSAGE_INDEX]
        )
        val operation = getNameIfKnown(
            HubPropertyOperation::class.java,
            message[HubPropertyOperation.IN_MESSAGE_INDEX]
        )
        val value = formatter.formatHex(message, HubProperty.DATA_START_INDEX, message.size)
        return if(value.isNotBlank()) "HubProperty $propertyId $operation $value" else "HubProperty $propertyId $operation"
    }

    private fun processAlert(message: ByteArray): String {
        if (message.size > AlertType.MAX_MSG_LENGTH || message.size < AlertType.MSG_WO_DATA_LENGTH) {
            return ""
        }
        val type = getNameIfKnown(AlertType::class.java, message[AlertType.IN_MESSAGE_INDEX])
        val operation = getNameIfKnown(
            AlertOperation::class.java,
            message[AlertOperation.IN_MESSAGE_INDEX]
        )
        var msg = "Alert type: $type operation: $operation"
        if (message.size == AlertType.MAX_MSG_LENGTH) {
            val value = formatter.toHexDigits(message[AlertType.DATA_INDEX])
            msg = "$msg val: $value"
        }
        return msg
    }

    private fun processAttachedIO(message: ByteArray): String {
        val length = message.size
        if (length != 5 && length != 9 && length != 15) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        val event = message[4]
        var msg = "Attached IO port: $port"
        msg = when {
            message.size == 5 && event.toInt() == 0x00 -> {
                "$msg Event: Detached"
            }
            message.size == 9 && event.toInt() == 0x02 -> {
                "$msg Event: Attached Virtual"
            }
            message.size == 15 && event.toInt() == 0x01 -> {
                "$msg Event: Attached"
            }
            else -> {
                "$msg Event: $event"
            }
        }
        val value = formatter.formatHex(message, 5, message.size)
        return "$msg Rest: $value"
    }

    private fun processError(message: ByteArray): String {
        if (message.size != ErrorCode.ERROR_MSG_LENGTH) {
            return ""
        }
        val command = getNameIfKnown(
            Command::class.java,
            message[ErrorCode.COMMAND_IN_MESSAGE_INDEX]
        )
        val code = getNameIfKnown(ErrorCode::class.java, message[ErrorCode.IN_MESSAGE_INDEX])
        return "Error from: $command code: $code"
    }

    private fun portOutputCommand(message: ByteArray): String {
        if (message.size < 6) {
            return ""
        }
        val port = getNameIfKnown(Port::class.java, message[3])
        val startup = getNameIfKnown(StartupCompletion::class.java, message[4])
        val subCommand = getNameIfKnown(PortOutputSubCommand::class.java, message[5])
        if (message[3] == Port.PLAYVM.value && message.size == 13) {
            return playVMCommand(message, port, startup, subCommand)
        } else {
            var msg = "PortOutputCommand port: $port"
            if (message[4] != StartupCompletion.IMMEDIATE_WITH_FEEDBACK.value) {
                msg = "$msg act:$startup"
            }
            msg = "$msg sub:$subCommand"
            if (message[5] == PortOutputSubCommand.WRITE_DIRECT_MODE.value) {
                val mode = message[6]
                val value = formatter.formatHex(message, 7, message.size)
                return "$msg mode: $mode val: $value"
            }
            val value = formatter.formatHex(message, 6, message.size)
            return "$msg val: $value"
        }
    }

    private fun playVMCommand(message: ByteArray, port: String, startup: String, subCommand: String): String {
        val msg = StringBuilder("PortOutputCommand port:").append(port)
        if (message[4] != StartupCompletion.IMMEDIATE_WITH_FEEDBACK.value) {
            msg.append(" act:").append(startup)
        }
        if (message[5] != PortOutputSubCommand.WRITE_DIRECT_MODE.value) {
            msg.append(" sub:").append(subCommand)
        }
        if (message[6].toInt() != 0x00 || message[7].toInt() != 0x03 || message[8].toInt() != 0x00) {
            msg.append(" mid:").append(formatter.formatHex(message, 6, 9))
        }
        msg.append(" Speed:").append(message[9].toInt()).append(" [").append(formatter.toHexDigits(message[9]))
            .append("]")
            .append(" Steer:").append(message[10].toInt()).append(" [").append(formatter.toHexDigits(message[10]))
            .append("]")
            .append(" PlayVm:").append(
                fromByte(
                    PlayVmOperation::class.java,
                    message[11]
                )
            )
        if (message[12].toInt() != 0x00) {
            msg.append(" LastByte:").append(formatter.toHexDigits(message[12]))
        }
        return msg.toString()
    }

    private fun portOutputCommandFeedback(message: ByteArray): String {
        if (message.size < 5) {
            return ""
        }
        var value = Arrays.copyOfRange(message, 3, message.size)
        val msg = StringBuilder("Port Output Feedback")
        while (value.size >= 2) {
            msg.append(" Port: ").append(getNameIfKnown(Port::class.java, value[0])).append(" Feedback: ").append(
                fromByte(
                    PortFeedback::class.java, value[1]
                )
            )
            value = Arrays.copyOfRange(value, 2, value.size)
        }
        return msg.toString()
    }

    private fun processPortModeRequest(message: ByteArray): String {
        if (message.size < 6) {
            return ""
        }
        val port = getNameIfKnown(Port::class.java, message[3])
        val mode = getNameIfKnown(PortMode::class.java, message[4])
        val type = getNameIfKnown(InformationType::class.java, message[5])
        return "PortModeInformationRequest port: $port mode: $mode type: $type"
    }

    private fun processPortInformationRequest(message: ByteArray): String {
        if (message.size != 5) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        val type = getNameIfKnown(
            PortInformationType::class.java,
            message[PortInformationType.IN_MESSAGE_INDEX]
        )
        return "PortInformationRequest port:$port type:$type"
    }

    private fun processPortInputFormatSetupSingle(message: ByteArray): String {
        if (message.size != 10) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        val mode = message[4]
        val notification = message[9].toInt() == 0x01
        val delta = formatter.formatHex(message, 5, message.size - 1)
        val notificationString = if (notification) "enabled" else "disabled"
        return "PortInputFormatSetupSingle port: $port mode: $mode delta: $delta notification: $notificationString"
    }

    private fun processPortInformation(message: ByteArray): String {
        if (message.size < 7) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        val type = getNameIfKnown(
            PortInformationType::class.java,
            message[PortInformationType.IN_MESSAGE_INDEX]
        )
        val value = formatter.formatHex(message, 5, message.size)
        return "Port Information port: $port type: $type value: $value"
    }

    private fun processPortModeInformation(message: ByteArray): String {
        if (message.size < 7) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        val mode = getNameIfKnown(PortMode::class.java, message[4])
        val type = getNameIfKnown(
            InformationType::class.java,
            message[InformationType.IN_MESSAGE_INDEX]
        )
        val value = formatter.formatHex(message, 6, message.size)
        return "Port Mode Information port: $port mode: $mode type: $type value: $value"
    }

    private fun processPortValueSingle(message: ByteArray): String {
        if (message.size < 5) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        //        if(message[Port.IN_INFORMATION_MESSAGE_INDEX]==Port.PLAYVM.getValue()) {
//            byte[] value = Arrays.copyOfRange(message, 5, message.length);
//
//            return "Port Value Single Port:" + port + " value:" + formatter.formatHex(value);
//        }
        val value = formatter.formatHex(message, 5, message.size)
        return "Port Value Single Port: $port value: $value"
    }

    private fun virtualPortSetup(message: ByteArray): String {
        if (message.size > 6 || message.size < 5) {
            return ""
        }
        if (message[3].toInt() == 0x01) {
            val port1 = getNameIfKnown(Port::class.java, message[4])
            val port2 = getNameIfKnown(Port::class.java, message[5])
            return "VirtualPortSetup connected $port1 $port2"
        } else if (message[3].toInt() == 0x00) {
            val port1 = getNameIfKnown(Port::class.java, message[4])
            return "VirtualPortSetup disconnected $port1"
        }
        return ""
    }

    private fun processInputFormatSingle(message: ByteArray): String {
        if (message.size != 10) {
            return ""
        }
        val port = getNameIfKnown(
            Port::class.java,
            message[Port.IN_INFORMATION_MESSAGE_INDEX]
        )
        val mode = message[4]
        val notification = message[9].toInt() == 0x01
        val delta = formatter.formatHex(message, 5, message.size - 1)
        val notificationString = if (notification) "enabled" else "disabled"
        return "PortInputFormatSingle port: $port mode: $mode delta: $delta notification: $notificationString"
    }
}
