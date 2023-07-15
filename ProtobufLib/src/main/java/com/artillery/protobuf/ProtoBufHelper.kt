package com.artillery.protobuf

import com.artillery.protobuf.model.key_consult_t
import com.artillery.protobuf.model.watch_cmds
import com.artillery.protobuf.model.watch_cmds.Builder
import com.artillery.protobuf.model.watch_cmds.cmd_t
import com.artillery.protobuf.utils.crcJW002
import com.artillery.protobuf.utils.createBytes
import com.artillery.protobuf.utils.createPkeySkey
import com.artillery.protobuf.utils.createRandomByteArray
import com.artillery.protobuf.utils.createWatchCommand
import com.artillery.protobuf.utils.sha256ToByteArray
import com.blankj.utilcode.util.LogUtils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 上午11:09
 */
class ProtoBufHelper private constructor(){

    companion object{

        object Ble{
            const val BLE_HEADER = "A55A"
        }



        fun getInstance() = Helper.instance
    }


    //包头
    private val head by lazy {
        Ble.BLE_HEADER.toUShort(16).toShort()
    }

    //包头+2 数据长度+2 crc校验位+2 == 6
    private val dataFixedLength: Int = 6


    /**
     * 每包的数据字节
     */
    private var mMtuSize: Int = 23


    /**
     * 数据缓存使用 合包时候用到
     */
    private val mCacheBytes by lazy(LazyThreadSafetyMode.NONE){
        mutableListOf<Byte>()
    }


    private object Helper{
        val instance = ProtoBufHelper()
    }

    fun setMtuSize(size: Int){
        if (size >= 23){
            mMtuSize = size
        }
    }

    /**
     * 获取基本配置参数
     */
    fun sendCMD_GET_BASE_PARAM(): List<ByteArray>{
        return sendNoParameters(cmd_t.CMD_GET_BASE_PARAM)
    }

    /**
     * 绑定设备
     */
    fun sendCMD_BIND_DEVICE(): List<ByteArray>{
        return sendNoParameters(cmd_t.CMD_BIND_DEVICE)
    }


    fun sendCMD_KEY_CONSULT(): List<ByteArray>{
        val uid = UInt.MAX_VALUE.toInt()
        val k1 = uid.sha256ToByteArray()
        val pair = createPkeySkey()
        var R11 = aesEncrypt()
        return createBase{
            cmd = cmd_t.CMD_KEY_CONSULT
            setKeyConsult(
                key_consult_t.newBuilder()
                    .setMStep(1)
                    .setMRandom()
                    .build()
            )
        }
    }

    /**
     * 获取设备信息
     */
    fun sendCMD_GET_DEVICE_INFO(): List<ByteArray>{
        return sendNoParameters(cmd_t.CMD_GET_DEVICE_INFO)
    }

    /**
     * 发送无参的数据包
     */
    private fun sendNoParameters(cmdT: cmd_t): List<ByteArray>{
        return createBase { setCmd(cmdT) }
    }

    /**
     * 构建最基本的 watch_cmds 对象数据
     */
    private inline fun createBase(onParams: Builder.() -> Builder): List<ByteArray>{
        return createWatchCommand(onParams).createBytes(head, mMtuSize, dataFixedLength)
    }


    /**
     * 接收数据包
     */
    fun receive(bytes: ByteArray, onAnalysis: (watch_cmds) -> Unit){
        //转换成小端
        val buffer = ByteBuffer.wrap(bytes).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }
        //包头
        val tempHeader = buffer.short.toUShort().toString(16)
        if (!Ble.BLE_HEADER.equals(tempHeader, true)){
            LogUtils.d("receive: 包头为 => $tempHeader, 不进行解析")
            return
        }

        //数据长度
        val length = buffer.short.toUShort()
        //校验和
        val crc = buffer.short.toUShort()
        val tempBytes = ByteArray(buffer.remaining())
        buffer.get(tempBytes)

        //说明当前一包数据就够了 不需要进行合包
        if (length == tempBytes.size.toUShort()){
            //计算一下crc
            val tempCrc = tempBytes.crcJW002()
            //crc校验通过
            val watch = bytes2WatchCmd(tempBytes, crc)
            watch?.let {
                onAnalysis.invoke(it)
            }
        }else {
            //走到这里说明当前数据是分包了的
            mCacheBytes.addAll(tempBytes.toList())
            val tempLength = mCacheBytes.size.toUShort()
            if (length == tempLength){
                //计算一下crc
                val tempCacheBytes = mCacheBytes.toByteArray()
                //crc校验通过
                val watch = bytes2WatchCmd(tempCacheBytes, crc)
                watch?.let {
                    onAnalysis.invoke(it)
                    mCacheBytes.clear()
                }
            }
        }
    }





    private fun bytes2WatchCmd(bytes: ByteArray, crc: UShort): watch_cmds?{
        //计算一下crc
        val tempCrc = bytes.crcJW002()
        //crc校验通过
        return if (tempCrc == crc){
            watch_cmds.parseFrom(bytes)
        } else {
            LogUtils.d("receive: crc未校验通过")
            null
        }
    }


    /**
     * @param plaintextData 需要加密的数据
     * @param encryptionKey 加密密钥
     */
    private fun aesEncrypt(plaintextData: String, encryptionKey: String): String {
        val keySpec = SecretKeySpec(encryptionKey.toByteArray(), "AES")
        val cipher = Cipher.getInstance("AES")
        cipher.init(Cipher.ENCRYPT_MODE, keySpec)

        val encryptedBytes = cipher.doFinal(plaintextData.toByteArray())
        val encryptedData = Base64.getEncoder().encodeToString(encryptedBytes)

        return encryptedData
    }




}



