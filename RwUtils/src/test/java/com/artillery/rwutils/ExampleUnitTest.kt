package com.artillery.rwutils

import org.junit.Test

import java.nio.ByteBuffer

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {


    @Test
    fun verification(){
        val value = 30
        val buffer = ByteBuffer.allocate(20)
        buffer.putInt(value)
        fillZeros(buffer)
        buffer.flip()

        while (buffer.hasRemaining()){
            val byte = buffer.get()
            println(byte)

        }

    }

    fun fillZeros(buffer: ByteBuffer) {
        while (buffer.hasRemaining()){
            buffer.put(0)
        }
    }

    @Test
    fun addition_isCorrect() {

        val textStr = "从2001年开始我们就一直在玩英雄联盟的游戏,并且我们还是组建了很多个战队,包括很多全职选手"

        val list = createMessagePush(textStr, 8)
        list.forEach { item ->
            println(toHexString(item))
        }
        val nexStr = parseMessagePush(list)

        println(nexStr)
    }

    fun createMessagePush(str: String, type: Byte, cmd: Byte = 0x73): List<ByteBuffer>{
        return mutableListOf<ByteBuffer>().apply {
            val content = str.toByteArray()
            var packet: ByteBuffer
            var offset = 0
            while (offset < content.size) {
                val remaining = content.size - offset
                val length = if (remaining > 17) 17 else remaining
                packet = ByteBuffer.allocate(length + 3)
                packet.put(cmd)
                packet.put((size + 1).toByte())
                packet.put(type)
                packet.put(content, offset, length)
                packet.flip()
                add(packet)
                offset += length
            }
        }
    }

    fun toHexString(buffer: ByteBuffer): String {
        return buffer.asReadOnlyBuffer().apply {
            //如果是写模式 则切换充读模式
            if (!hasRemaining()){
                flip()
            }
        }.let { buffer ->
            buildString {
                while (buffer.hasRemaining()) {
                    append(String.format("%02x", buffer.get()))
                    if (buffer.hasRemaining()){
                        append(",")
                    }
                }
            }
        }
    }

    fun parseMessagePush(packets: List<ByteBuffer>): String {
        val lastByteBuffer = packets.last()
        val lastSize = lastByteBuffer.limit() - 3
        val length = (packets.size - 1) * 17 + lastSize
        println(length)

        val content = ByteArray(length)
        packets.forEachIndexed { index, byteBuffer ->
            byteBuffer.position(3)
            byteBuffer.get(content, index * 17, byteBuffer.remaining())
        }
        return String(content, Charsets.UTF_8)
    }

    fun mergeBuffers(buffers: List<ByteBuffer>): ByteArray {
        val size = buffers.sumOf { it.remaining() }
        val merged = ByteArray(size)
        var offset = 0
        for (buffer in buffers) {
            buffer.get(merged, offset, buffer.remaining())
            offset += buffer.remaining()
        }
        return merged
    }
}