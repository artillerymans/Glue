package com.artillery.rwutils.exts

import android.graphics.Bitmap
import com.artillery.rwutils.type.SwitchType
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer


/**
 * 转换成开关实际byte
 */
fun SwitchType.toByte(): Byte{
    return if (this == SwitchType.ON) 0x01 else 0x00
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

fun Int.toBytes(): ByteArray{
    val byteBuffer = ByteBuffer.allocate(4).also {
        it.putInt(this)
        it.flip()
    }
    return byteBuffer.array()
}

/**
 * 取Int类型的低3位字节
 */
fun Int.toBytesLowerThree(): ByteArray{
    val byteBuffer = ByteBuffer.allocate(4).also {
        it.putInt(this)
        it.flip()
    }
    byteBuffer.position(1)
    val dst = ByteArray(3)
    byteBuffer.get(dst)
    return dst
}

fun ByteArray.toBuffer(): ByteBuffer{
    return ByteBuffer.wrap(this)
}

fun zeroByte(): Byte{
    return 0.toByte()
}

fun Byte.byte2Int(): Int{
    return this.toInt() and 0xff
}




fun Bitmap.toByteArrays(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.use {
        recycle()
        it.toByteArray()
    }
}