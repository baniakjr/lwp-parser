package baniakjr.lwp

import com.google.gson.annotations.SerializedName

@JvmRecord
data class OuterElement(
    @field:SerializedName("_source") val source: SourceBT
) {
    @JvmRecord
    data class SourceBT(val layers: Layer)

    @JvmRecord
    data class Layer(val bluetooth: BluetoothElement, val btatt: BtAttElement)

    @JvmRecord
    data class BluetoothElement(
        @field:SerializedName("bluetooth.src") val source: String,
        @field:SerializedName("bluetooth.dst") val destination: String
    )

    @JvmRecord
    data class BtAttElement(
        @field:SerializedName("btatt.value") val value: String?
    )
}