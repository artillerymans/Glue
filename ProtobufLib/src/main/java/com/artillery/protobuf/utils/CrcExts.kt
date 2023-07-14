package com.artillery.protobuf.utils

/**
 * @author : zhiweizhu
 * create on: 2023/7/14 下午6:00
 */
fun ByteArray.crcJW002(crc: Int = 0): UShort{
    var crcResult = crc
    for (element in this) {
        crcResult = ((crcResult shr 8) and 0xFF or (crcResult shl 8)) and 0xFFFF
        crcResult = crcResult xor element.toUByte().toInt()
        crcResult = crcResult xor ((crcResult and 0xFF) shr 4)
        crcResult = crcResult xor ((crcResult shl 8) shl 4)
        crcResult = crcResult xor (((crcResult and 0xFF) shl 4) shl 1)
    }

    return crcResult.toUShort()
}