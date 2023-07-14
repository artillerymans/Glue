package com.artillery.protobuf

import com.artillery.protobuf.model.battery_info_t
import com.artillery.protobuf.model.watch_cmds
import com.blankj.utilcode.util.ConvertUtils
import org.junit.Test

import org.junit.Assert.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun check_crc() {
        val valueU = "A55A".toUShort(16)
        val value = valueU.toShort()
        println(value)
        println(valueU)

        val hex = value.toUShort()
        val hexU = hex.toString(16)
        println(hex)
        println(hexU)

    }
}