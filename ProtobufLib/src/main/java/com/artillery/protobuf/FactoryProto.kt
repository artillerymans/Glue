package com.artillery.protobuf

import com.artillery.protobuf.model.watch_cmds
import com.artillery.protobuf.utils.crcJW002
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.TimeZone

/**
 * @author : zhiweizhu
 * create on: 2023/7/14 上午11:43
 */
class FactoryProto {


    fun createBytes(): List<ByteArray>{
        val bytes = watch_cmds.newBuilder()
            .setCmd(watch_cmds.cmd_t.CMD_GET_BASE_PARAM)
            .setResponse(true)
            .setSeconds((System.currentTimeMillis() / 1000).toInt())
            .setTimezone(TimeZone.getDefault().rawOffset)
            .build().toByteArray()

        val length = bytes.size.toUShort()

        val crc = bytes.crcJW002()

        LogUtils.d("createBytes: crc = ${crc.toString(16)}")
        LogUtils.d("createBytes: ${ConvertUtils.bytes2HexString(bytes)}")
        LogUtils.d("createBytes: length = $length")

        val list = bytes.toMutableList().chunked(15)

        val byteList = mutableListOf<ByteArray>()
        list.forEach { value ->
            byteList.add(
                ByteBuffer.allocate(6 + 15).apply {
                    order(ByteOrder.LITTLE_ENDIAN)
                    putShort("A55A".toUShort(16).toShort())
                    putShort(length.toShort())
                    putShort(crc.toShort())
                    put(value.toByteArray())
                }.array()
            )
        }
        byteList.forEach { bytes ->
            LogUtils.d("createBytes: ${ConvertUtils.bytes2HexString(bytes)}")
        }
        return byteList
    }

}


