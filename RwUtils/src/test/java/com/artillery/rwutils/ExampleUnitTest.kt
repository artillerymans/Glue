package com.artillery.rwutils

import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.toBytes
import com.artillery.rwutils.exts.toBytesLowerThree
import com.blankj.utilcode.util.ConvertUtils
import com.artillery.rwutils.crc.crc
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.artillery.rwutils.exts.zeroByte
import com.artillery.rwutils.model.ContactsItem
import com.artillery.rwutils.model.SleepInfoData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import org.junit.Test
import java.awt.font.TextAttribute

import java.nio.ByteBuffer
import java.time.LocalDateTime
import kotlin.experimental.and

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {

    @Test
    fun test0x95(){
        val bytes = ConvertUtils.hexString2Bytes("9500026477A86C0F026477ABF00F01")
        println(bytes.contentToString())
        val result = AnalyzeDataFactory.analyze0x95For0x15(bytes)
        println(result)

    }


    @Test
    fun testpAr(){
        val value = "See [testing documentation](http://d.android.com/tools/testing)" +
                ".Example local unit test, which will execute on the development machine (host)." +
                "ExampleUnitTest" +
                "ExampleUnitTest" +
                "ExampleUnitTest" +
                "ExampleUnitTest" +
                "ExampleUnitTest" +
                "ExampleUnitTest" +
                "ExampleUnitTest" +
                "ExampleUnitTest5"

        println(test1(value.toByteArray()))


    }


    private fun test1(data: ByteArray): ByteArray{
        val chunkSize = 25
        return ByteBuffer.allocate(data.size / chunkSize).apply {
            var a: Byte = 0
            var index = 7

            for (i in data.indices step chunkSize) {
                val endIndex = (i + chunkSize).coerceAtMost(data.size)
                val sub = data.sliceArray(i until endIndex)

                for (byteValue in sub) {
                    val bit: Byte = if (byteValue.toInt() == 0xFF) 0 else 1
                    a = (a.toInt() or (bit.toInt() shl index)).toByte()
                    if (index == 0) {
                        put(a)
                        a = 0
                        index = 7
                    } else {
                        index--
                    }
                }
            }
        }.array()
    }


    @Test
    fun testSyncContacts(){
        val list = mutableListOf<ContactsItem>().apply {
            add(ContactsItem("华为消费者服务热线", "950800"))
            add(ContactsItem("西乡派出所", "27925110"))
        }
        println(list)
        val byteArrays = CreateDataFactory.createSyncContacts(list)
        println(byteArrays)

    }

    @Test
    fun analysis0x97(){

        val longHex = 0xffffffff


        println(longHex)
        println(longHex.toInt())

        /*val hexString = "9742705261"
        val bytes = ConvertUtils.hexString2Bytes(hexString)
        println("size = ${bytes.size}")
        val result = AnalyzeDataFactory.analyze0x97For0x17(ConvertUtils.hexString2Bytes(hexString))
        println(result.data)*/
    }

    @Test
    fun ansy0x13(){

        var localDateTime = LocalDateTime.now()
        localDateTime = localDateTime.minusDays(1L)

        println("month = ${localDateTime.monthValue}")
        println("day = ${localDateTime.dayOfMonth}")


        val hexString = "1317051A"
        val bytes = ConvertUtils.hexString2Bytes(hexString)
        val buffer = bytes.toBuffer()
        println("cmd=${buffer.get().byte2Int().toString(16)}")
        println("year=${buffer.get().byte2Int()}")
        println("month=${buffer.get().byte2Int()}")
        println("day=${buffer.get().byte2Int()}")

    }

    @Test
    fun ansy0x02_04(){
        //val hexString = "0204003C0900121E011700081E013C0900121E00"
        //val hexString = "0204003C0900121E001700081E003C0900121E00"
        val hexString = "0204003C0900121E001700081E003C0900121E"
        val bytes = ConvertUtils.hexString2Bytes(hexString)
        val buffer = bytes.toBuffer()
        println("命令码= ${buffer.get().byte2Int().toString(16)}")
        println("序号= ${buffer.get().byte2Int()}")
        println("久坐开关= ${buffer.get().byte2Int()}")
        println("久坐间隔= ${buffer.get().byte2Int()}")
        println("久坐开始= ${buffer.get().byte2Int()}:${buffer.get().byte2Int()}")
        println("久坐结束= ${buffer.get().byte2Int()}:${buffer.get().byte2Int()}")
        println("勿扰开关= ${buffer.get().byte2Int()}")
        println("勿扰开始= ${buffer.get().byte2Int()}:${buffer.get().byte2Int()}")
        println("勿扰结束= ${buffer.get().byte2Int()}:${buffer.get().byte2Int()}")
        println("喝水开关= ${buffer.get().byte2Int()}")
        println("喝水间隔= ${buffer.get().byte2Int()}")
        println("喝水开始= ${buffer.get().byte2Int()}:${buffer.get().byte2Int()}")
        println("喝水结束= ${buffer.get().byte2Int()}:${buffer.get().byte2Int()}")
    }


    @Test
    fun ansy0xb3(){
        val hexString = "B30100000C4A0074000008C50000067E"
        val bytes = ConvertUtils.hexString2Bytes(hexString)
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
       if (cmd.byte2Int() == BleConstantData.REPLY_CMD_B3) {
           //0x01：实时步数数据 有效
           val number1 = buffer.get()
           //总步数
           val step = buffer.int
           //卡路里
           val calorie = buffer.short
           //总里程 单位米
           val totalMileage = buffer.int
           //活动时长 单位秒
           val activeTime = buffer.int

           println("step = $step, $calorie, $totalMileage, $activeTime")
       }
    }

    @OptIn(DelicateCoroutinesApi::class)
    @Test
    fun testCoordination(){
        val job = CoroutineScope(Dispatchers.Default).launch {
            println("------->")
            a23()
            println("ppppp------->")
        }
        Thread.sleep(100000)
    }

    suspend fun a23(){
        val list = (0..100).toList()
        list.forEach {index ->
            val text = suspendCancellableCoroutine<String> {
                println("index = $index")
                it.resumeWith(Result.success("$index"))

            }

            println("0000000000000 -> 返回的数据 = $text")

        }

    }


    @Test
    fun bytesSum(){
        val a1 = 0x01
        val a2 = 0x02
        val a3 = 0x04
        val a4 = 0x07
        println(a1.toString(2))
        println(a2.toString(2))
        println(a3.toString(2))
        println(a4.toString(2))
        println((a1 and a2).toString(2))

        val result = 0x01
        println(result.toString(2))
        println("是否支持： A1 = ${(result and a1) > zeroByte()}")
        println("是否支持： A2 = ${(result and a2) > zeroByte()}")
        println("是否支持： A3 = ${(result and a3) > zeroByte()}")

    }

    @Test
    fun wrapBytes(){
        val textBytes = "HD01_A1".toByteArray()
        val tempBytes = textBytes.toMutableList().apply {
            add(0)
        }.toByteArray()
        println(ConvertUtils.bytes2HexString(tempBytes))

        val buffer = tempBytes.toBuffer()

        val versionBytes = ByteArray(8).apply {
            buffer.get(this)
        }.map { byte -> if (byte == 0.toByte()) '0' else byte.toInt().toChar() }.joinToString("")
        println(versionBytes)
    }


    @Test
    fun createByteArrays(){
        val bytes = ByteArray(8).apply {
            ByteBuffer.wrap(this).apply {
                put(0x08)
                put(0x09)
                put(0x23)
                put(0x10)
                putInt(120)
            }
        }

        println(ConvertUtils.bytes2HexString(bytes))

    }

    @Test
    fun crcTest(){
        val text: String = "Example local unit test, which will execute on the development machine (host)."
        val crc = text.toByteArray().crc()
        println(crc.toUInt())
        println(crc)
        //2320888715

    }

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