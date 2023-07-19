package com.artillery.protobuf.utils

import com.artillery.protobuf.model.watch_cmds
import com.artillery.protobuf.model.watch_cmds.Builder
import com.artillery.protobuf.model.watch_cmds.newBuilder
import com.blankj.utilcode.util.LogUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.TimeZone

/**
 * @author : zhiweizhu
 * create on: 2023/7/15 上午9:25
 */


fun watch_cmds.createBytes(head: Short, mtuSize: Int, fixedLength: Int): List<ByteArray>{
    val data = toByteArray()
    val chunkSize = mtuSize - fixedLength
    val crc = data.crcJW002().toShort()
    val length = data.size.toShort()
    return data.cut2ListByteArray(chunkSize).map { value ->
        //使用数据长度 + 固定长度 为了规避当数据并没有mtu大小的时候出现末尾补齐的问题
        //如果需要末尾补齐是0的 使用 allocate(mMtuSize)
        ByteBuffer.allocate(value.size + fixedLength).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putShort(head)
            putShort(length)
            putShort(crc)
            put(value)
        }.array()
    }
}

/**
 * 穿件一个通用的命令模板
 */
inline fun createWatchCommand(onParams: Builder.() -> Builder): watch_cmds {
    return newBuilder()
        .createDefParams()
        .onParams()
        .build()
}


/**
 * 创建默认参数
 */
fun Builder.createDefParams(isResponse: Boolean = true): Builder{
    return this.setResponse(isResponse)
        .setSeconds(System.currentTimeMillis().millisecond2Seconds())
        .setTimezone(TimeZone.getDefault().zoneToInt())
}

