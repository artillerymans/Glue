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
}