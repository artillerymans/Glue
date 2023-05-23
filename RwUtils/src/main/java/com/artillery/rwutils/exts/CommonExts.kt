package com.artillery.rwutils.exts

import com.artillery.rwutils.type.SwitchType
import java.nio.ByteBuffer


/**
 * 转换成开关实际byte
 */
fun SwitchType.toByte(): Byte{
    return if (this == SwitchType.OFF) 0x00 else 0x01
}


/**
 * 对ByteBuffer剩余的空间 补0
 * 注意 必须要先确定当前是写的模式
 */
fun ByteBuffer.fillZeros() {
    while (hasRemaining()){
        put(0)
    }
}