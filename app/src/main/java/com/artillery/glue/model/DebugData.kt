package com.artillery.glue.model


enum class DebugDataType{
    write,
    notice,
    noticeAck
}

sealed class DebugBaseItem{

    data class PackItem(
        val json: String,
        val hexString: String
    ): DebugBaseItem()

    data class DebugItem(
        val type: DebugDataType,
        val formatTime: String,
        val nativeData: ByteArray,
        val hexString: String,
        val order: Int,
        val hexCmd:String
    ): DebugBaseItem() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as DebugItem

            if (type != other.type) return false
            if (formatTime != other.formatTime) return false
            if (!nativeData.contentEquals(other.nativeData)) return false
            if (hexString != other.hexString) return false
            if (order != other.order) return false
            if (hexCmd != other.hexCmd) return false

            return true
        }

        override fun hashCode(): Int {
            var result = type.hashCode()
            result = 31 * result + formatTime.hashCode()
            result = 31 * result + nativeData.contentHashCode()
            result = 31 * result + hexString.hashCode()
            result = 31 * result + order
            result = 31 * result + hexCmd.hashCode()
            return result
        }


    }
}

