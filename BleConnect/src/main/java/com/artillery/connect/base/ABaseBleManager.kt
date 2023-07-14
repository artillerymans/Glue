package com.artillery.connect.base

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.ble.ktx.suspend


/**
 * @author : zhiweizhu
 * create on: 2023/7/13 下午4:34
 */
abstract class ABaseBleManager(context: Context = Utils.getApp()): BleManager(context) {

    companion object{
        const val WRITE = 0
        const val READ = 1
        const val NOTIFICATION = 2
    }

    protected val mDefaultScope by lazy(LazyThreadSafetyMode.NONE){
        CoroutineScope(Dispatchers.Default)
    }


    override fun log(priority: Int, message: String) {
        LogUtils.d("ABaseBleManager log: $message")
    }


    fun connectByBluetoothDevice(
        device: BluetoothDevice,
        retry: Int = 3,
        retryDelay: Int = 3000,
        timeout: Long = 15_000
    ){
        mDefaultScope.launch {
            try {
                connect(device)
                    .retry(retry, retryDelay)
                    .timeout(timeout)
                    .useAutoConnect(true)
                    .suspend()
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }



    fun post(
        bytes: List<ByteArray>,
        characteristicType: Int = WRITE,
        writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE,
        delay: Long = 0L
    ){
        mDefaultScope.launch {
            if (delay > 0){
                delay(delay)
            }
            send(
                bytes,
                characteristicType,
                writeType
            )
        }
    }

    private suspend fun send(
        bytes: List<ByteArray>,
        type: Int = WRITE,
        @WriteType writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
    ){
        var indexRetry = 0
        bytes.forEachIndexed{ index, data ->
            try {
                val tempData = suspendCancellableCoroutine { continuation ->
                    indexRetry = index
                    writeCharacteristic(
                        onGetCharacteristic(type),
                        data,
                        writeType
                    ).done {
                        continuation.resumeWith(Result.success(data))
                    }.fail { device, status ->
                        continuation.resumeWith(Result.failure(Exception("写入数据失败，状态码 ->$status")))
                    }.enqueue()
                }
                LogUtils.d("写入数据$index:->${ConvertUtils.bytes2HexString(tempData)}")
            }catch (e: Exception){
                e.printStackTrace()
                return@forEachIndexed
            }finally {
                //数据多的话进行延时处理
                if (bytes.size > 10){
                    delay(100)
                }
            }
        }

        if (indexRetry != 0){
            val tempBytes = bytes.subList(indexRetry, bytes.size)
            if (tempBytes.isNotEmpty()){
                post(
                    tempBytes,
                    type,
                    writeType,
                    3000L
                )
            }
        }
    }

    abstract fun onGetCharacteristic(type: Int): BluetoothGattCharacteristic?

}