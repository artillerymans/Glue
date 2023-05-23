package com.artillery.rwutils

import com.artillery.rwutils.exts.toBytes
import com.artillery.rwutils.exts.toBytesLowerThree
import com.blankj.utilcode.util.ConvertUtils
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

        val intValue3764 = 0x01348902
        println(ConvertUtils.bytes2HexString(intValue3764.toBytesLowerThree()))


        /* val buffer = ByteBuffer.allocate(4) // 分配 4 个字节的 ByteBuffer

         val value = 0x12345678 // 写入的整数值

         buffer.putInt(value) // 写入整数值

         val bytes = ByteArray(3) // 分配 3 个字节的数组

         buffer.position(1) // 将 position 设置为 1，跳过第一个字节
         buffer.get(bytes) // 获取后面三个字节
         val hexString: String =  ConvertUtils.bytes2HexString(bytes) // 将字节数组转换为 hex string

         println(hexString) // 输出 hex string*/


        /*
                val int = 0xffffff
                println()

                val short = (0xfffe).toShort()
                println(short)
                println(short.toUShort())


                val value = 30
                val buffer = ByteBuffer.allocate(20)
                buffer.putInt(value)
                fillZeros(buffer)
                buffer.flip()

                while (buffer.hasRemaining()){
                    val byte = buffer.get()
                    println(byte)
                }*/



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