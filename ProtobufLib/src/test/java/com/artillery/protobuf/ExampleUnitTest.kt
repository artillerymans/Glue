package com.artillery.protobuf

import com.artillery.protobuf.model.battery_info_t
import com.artillery.protobuf.model.watch_cmds
import com.artillery.protobuf.utils.createRandomByteArray
import com.artillery.protobuf.utils.cut2ListByteArray
import com.artillery.protobuf.utils.sha256ToByteArray
import com.blankj.utilcode.util.ConvertUtils
import org.junit.Test

import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECNamedDomainParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import java.security.Security

import org.junit.Assert.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun check_crc() {
        val list = byteArrayOf(0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07)
        /*val chunkSize = 10555

        *//*val chunkedResultList = list.withIndex()
            .groupBy { it.index / chunkSize }
            .values
            .map { it.map { indexedValue -> indexedValue.value } }*//*

        val chunkedResult = list.cut2ListByteArray(chunkSize)


        for (chunk in chunkedResult) {
            println(chunk.toList())
        }*/
        val length = list.size.toShort()
        val dataFixedLength = 6
        val mMtuSize = dataFixedLength + 6
        val chunkSize = mMtuSize - dataFixedLength
        val tempList = list.cut2ListByteArray(chunkSize).map { value ->
            ByteBuffer.allocate(value.size + dataFixedLength).apply {
                order(ByteOrder.LITTLE_ENDIAN)
                putShort("A55A".toUShort(16).toShort())
                putShort(length)
                putShort(15)
                put(value)
            }.array()
        }

        tempList.forEach { bytes ->
            println(bytes.toList())
        }

    }


    @Test
    fun encryption(){
        val maxValue = UInt.MAX_VALUE
        println(maxValue.toString(16))
        /*   val userID = 80
           val key = hashToKeyValue(userID)
           println(key)

           createPKey1Skey1()

           val bytes = createRandomByteArray(32)
           println(bytes.size)
           println(bytes.toList())

           val bytes48 = createRandomByteArray(48)
           println(bytes48.size)
           println(bytes48.toList())*/


        val k1 = createRandomByteArray(32)
        val k2 = createRandomByteArray(32)
        val k3 = k1 + k2
        println("k3 size = ${k3.size}")
        val tempShaBytes = k3.sha256ToByteArray()
        println("tempShaBytes size = ${tempShaBytes.size}")
        println("tempShaBytes ${tempShaBytes.toList()}")


        println("=====================================")

        createPKey1Skey1()




    }

    /**
     * 把UserID通过Sha256转换成keyStringHex
     */
    fun hashToKeyValue(userID: Int): String {
        val sha256 = MessageDigest.getInstance("SHA-256")
        val hashedBytes = sha256.digest(userID.toString().toByteArray())

        // 将字节数组转换为十六进制字符串
        val stringBuilder = StringBuilder()
        for (hashedByte in hashedBytes) {
            val hex = String.format("%02x", hashedByte)
            stringBuilder.append(hex)
        }

        // 构建键值对字符串
        val keyValue = "key=${userID}&value=${stringBuilder.toString()}"
        return keyValue
    }


    fun createPKey1Skey1(){
        Security.addProvider(org.bouncycastle.jce.provider.BouncyCastleProvider())

        val curveName = "secp256k1"
        val ecSpec: ECNamedCurveParameterSpec = ECNamedCurveTable.getParameterSpec(curveName)
        val domainParams = ECDomainParameters(ecSpec.curve, ecSpec.g, ecSpec.n, ecSpec.h)
        val keyGen = ECKeyPairGenerator()
        val random = SecureRandom()
        val keyGenParams = ECKeyGenerationParameters(domainParams, random)
        keyGen.init(keyGenParams)

        val keyPair: AsymmetricCipherKeyPair = keyGen.generateKeyPair()
        val privateKey: ECPrivateKeyParameters = keyPair.private as ECPrivateKeyParameters
        val publicKey: ECPublicKeyParameters = keyPair.public as ECPublicKeyParameters

        val pKey1 = publicKey.q.getEncoded(false)
        val sKey1 = privateKey.d.toByteArray()

        println("Public Key (pKey1): ${pKey1.toList()}")
        println("Private Key (sKey1): ${sKey1.toList()}")
    }


    @Test
    fun binaryTest(){
        sendCMD_SET_MESSAGE_SWITCH(MessageSwitch.Sms(1), MessageSwitch.Gmail(1))
    }


    @Test
    fun setAlarm(){
        println("40".toInt(16).toString(2))
        println("20".toInt(16).toString(2))
        println("10".toInt(16).toString(2))
    }


    fun sendCMD_SET_MESSAGE_SWITCH(vararg values: MessageSwitch){
        val defValue = UInt.MIN_VALUE
        var tempValue = 0u
        values.forEach { msg ->
            val tempTypeValue = when(msg.type){
                MsgType.All -> msg.value.toUInt()
                MsgType.Instagram -> msg.value.toUInt() shl 1
                MsgType.Linkedin -> msg.value.toUInt() shl 2
                MsgType.Twitter -> msg.value.toUInt() shl 3
                MsgType.FaceBook -> msg.value.toUInt() shl 4
                MsgType.FaceTime -> msg.value.toUInt() shl 5
                MsgType.Feixin -> msg.value.toUInt() shl 6
                MsgType.Line -> msg.value.toUInt() shl 7
                MsgType.Sound -> msg.value.toUInt() shl 8
                MsgType.Gmail -> msg.value.toUInt() shl 9
                MsgType.Webook -> msg.value.toUInt() shl 10
                MsgType.Wechat -> msg.value.toUInt() shl 11
                MsgType.QQ -> msg.value.toUInt() shl 12
                MsgType.Sms -> msg.value.toUInt() shl 13
                MsgType.Call -> msg.value.toUInt() shl 14
                MsgType.Skype -> msg.value.toUInt() shl 15
                MsgType.DingTalk -> msg.value.toUInt() shl 16
                MsgType.AliWangWang -> msg.value.toUInt() shl 17
                MsgType.Alipay -> msg.value.toUInt() shl 18
                MsgType.KakaoTalk -> msg.value.toUInt() shl 19
                MsgType.Qianiu -> msg.value.toUInt() shl 20
                MsgType.WhatsApp -> msg.value.toUInt() shl 21
                MsgType.Pinterest -> msg.value.toUInt() shl 22
                MsgType.OtherApp -> msg.value.toUInt() shl 23
                MsgType.Message -> msg.value.toUInt() shl 24
                else -> defValue
            }
            tempValue = tempTypeValue xor defValue xor tempValue
        }

        println(tempValue.toString(2))

    }
}