package com.artillery.rwutils

import com.blankj.utilcode.util.LogUtils
import org.junit.Test

import org.junit.Assert.*
import java.nio.ByteBuffer
import kotlin.experimental.or

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        val hour = 9
        val minute = 30
        val buffer = ByteBuffer.allocate(2)
        val startTime: Short = ((hour shl 8) or minute).toShort()
//        buffer.put(9)
//        buffer.put(30)
        buffer.putShort(startTime)
        buffer.flip()
        val value = buffer.short
        println(value)

        val hourTemp = (value.toInt() shr 8) and 0xFF
        val minuteTemp = value.toInt() and 0xFF
        println(hourTemp)
        println(minuteTemp)

        val zr = 0x40
        val z1 = 0x01
        val z3 = 0x04
        val list = mutableListOf<Byte>(
            zr.toByte(),
            z3.toByte()
        )
        val result = list.fold(0.toByte()){acc, value -> acc or value}
        println(result.toString(2))


    }
}