package com.artillery.rwutils.exts

import android.graphics.Bitmap
import android.graphics.Matrix
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
fun Short.short2Int(): Int{
    return this.toInt() and 0xffff
}

fun Int.rgb888toRgb555(): Int {
    return this shr 19 and 31 shl 11 or (this shr 10 and 63 shl 5) or (this shr 3 and 31)
}

fun Bitmap.scaleBitmap(newWidth: Float, newHeight: Float): Bitmap {
    // 计算缩放比例
    val scaleWidth = newWidth / width
    val scaleHeight = newHeight / height
    // 取得想要缩放的matrix参数
    val matrix = Matrix()
    matrix.postScale(scaleWidth, scaleHeight)
    // 得到新的图片
    return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

fun Short.short2Int(): Int{
    return this.toInt() and 0xffff
}


fun Int?.orEmpty(def: Int = 0): Int{
    return this ?: def
}




fun Bitmap.toByteArrays(): ByteArray {
    val stream = ByteArrayOutputStream()
    this.compress(Bitmap.CompressFormat.PNG, 100, stream)
    return stream.use {
        recycle()
        it.toByteArray()
    }
}