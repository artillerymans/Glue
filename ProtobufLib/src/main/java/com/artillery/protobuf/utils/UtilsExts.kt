package com.artillery.protobuf.utils

import org.bouncycastle.crypto.AsymmetricCipherKeyPair
import org.bouncycastle.crypto.generators.ECKeyPairGenerator
import org.bouncycastle.crypto.params.ECDomainParameters
import org.bouncycastle.crypto.params.ECKeyGenerationParameters
import org.bouncycastle.crypto.params.ECPrivateKeyParameters
import org.bouncycastle.crypto.params.ECPublicKeyParameters
import org.bouncycastle.jce.ECNamedCurveTable
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec
import java.security.MessageDigest
import java.security.SecureRandom
import java.security.Security
import java.util.TimeZone

/**
 * @author : zhiweizhu
 * create on: 2023/7/15 上午9:21
 */

fun TimeZone.zoneToInt(): Int{
    val rawOffsetMillis = this.rawOffset
    return rawOffsetMillis / (1000 * 60 * 60)
}

fun Long.millisecond2Seconds(): Int{
    return (this / 1000).toInt()
}

/**
 * 根据size大小切割成 二维数组
 * 备注：如果需要不足的位补齐的功能使用自带的 chunked函数
 */
fun ByteArray.cut2ListByteArray(size: Int): List<ByteArray>{
    if (size <= 0) return emptyList()
    return withIndex()
        .groupBy { it.index / size }
        .values
        .map { it.map { indexedValue -> indexedValue.value } }
        .map { list -> list.toByteArray()}
}


/**
 * 随机生成 size 个字节数组的随机值
 */
fun createRandomByteArray(size: Int): ByteArray{
    val random = SecureRandom()
    val randomBytes = ByteArray(size)
    random.nextBytes(randomBytes)
    return randomBytes
}

/**
 * 把Int类型通过sha256进行加密获取byteArray类型
 */
fun Int.sha256ToByteArray(): ByteArray{
    val sha256 = MessageDigest.getInstance("SHA-256")
    return sha256.digest(this.toString().toByteArray())
}

fun ByteArray.sha256ToByteArray(): ByteArray {
    val sha256 = MessageDigest.getInstance("SHA-256")
    return sha256.digest(this)
}


fun createPkeySkey(): Pair<ByteArray, ByteArray>{
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

    return Pair(pKey1, sKey1)


}