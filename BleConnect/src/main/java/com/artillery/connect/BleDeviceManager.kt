package com.artillery.connect

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Handler
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BuildConfig
import no.nordicsemi.android.ble.annotation.WriteType
import no.nordicsemi.android.ble.ktx.suspend

/**
 * @author : zhiweizhu
 * create on: 2023/5/24 上午11:27
 */
class BleDeviceManager(context: Context = Utils.getApp()) : BleManager(context) {

    private var mReadNotificationCharacteristic: BluetoothGattCharacteristic? = null
    private var mWriteCharacteristic: BluetoothGattCharacteristic? = null

    private val mDefaultScope = CoroutineScope(Dispatchers.Default)

    private var mReadNotificationCharacteristicChannel: SendChannel<ByteArray>? = null

    override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
        val service = gatt.getService(ConstantServiceUUID.MAIN_SERVICE_UUID)
        service?.let {
            mReadNotificationCharacteristic =
                it.getCharacteristic(ConstantServiceUUID.READ_NOTIFY_SERVICE_UUID)
            mWriteCharacteristic =
                it.getCharacteristic(ConstantServiceUUID.WRITE_SERVICE_UUID)
        }
        return mReadNotificationCharacteristic != null && mWriteCharacteristic != null
    }

    override fun log(priority: Int, message: String) {
        if (BuildConfig.DEBUG) {
            LogUtils.d("Ble Log: $message")
        }
    }


    fun connectDevice(
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

    fun setChannel(channel: Channel<ByteArray>){
        mReadNotificationCharacteristicChannel = channel
    }

    fun post(bytes: List<ByteArray>, writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE){
        mDefaultScope.launch {
            send(bytes, writeType)
        }
    }

    private suspend fun send(bytes: List<ByteArray>,
                             @WriteType writeType: Int = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE){
        bytes.forEachIndexed{ index, data ->
            val tempData = suspendCancellableCoroutine { continuation ->
                writeCharacteristic(
                    mWriteCharacteristic,
                    data,
                    writeType
                ).done {
                    continuation.resumeWith(Result.success(data))
                }.fail { device, status ->
                    continuation.resumeWith(Result.failure(Exception("写入数据失败，状态码 ->$status")))
                }.enqueue()
            }
            LogUtils.d("写入数据$index:->${ConvertUtils.bytes2HexString(tempData)}")
        }
    }


    override fun initialize() {
        super.initialize()

        requestMtu(238)
            .enqueue()

        setNotificationCallback(mReadNotificationCharacteristic).with { device, data ->
            LogUtils.d("initialize: 通知回调 --> ${ConvertUtils.bytes2HexString(data.value)}")
            data.value?.let { bytes ->
                if (bytes.isNotEmpty()) {
                    mDefaultScope.launch {
                        mReadNotificationCharacteristicChannel?.send(bytes)
                    }
                }
            }
        }

        enableNotifications(mReadNotificationCharacteristic)
            .done {
                LogUtils.d("initialize: Enabled mReadNotificationCharacteristic Notifications")
            }
            .fail { device, status ->
                val deviceMac = device?.address.orEmpty()
                LogUtils.d("initialize:通知 deviceMac = $deviceMac ==> $status")
                disconnect().enqueue()
            }
            .enqueue()

        readCharacteristic(mReadNotificationCharacteristic).with { device, data ->
            LogUtils.d("initialize: 读操作 --> ${ConvertUtils.bytes2HexString(data.value)}")
        }.enqueue()

        bluetoothDevice?.also {
            if (it.bondState == BluetoothDevice.BOND_NONE){
                //发起配对
                bluetoothDevice?.createBond()
            }
        }


        /*beginAtomicRequestQueue()
            .add(
                enableNotifications(mReadNotificationCharacteristic)
                    .done {
                        LogUtils.d("initialize: Enabled mReadNotificationCharacteristic Notifications")
                    }
                    .fail { device, status ->
                        val deviceMac = device?.address.orEmpty()
                        LogUtils.d("initialize: deviceMac = $deviceMac ==> $status")
                        disconnect().enqueue()
                    }
            )
            .done {
                val deviceName = it?.address.orEmpty()
                LogUtils.d("initialize: deviceName ===> Initialized")
            }
            .enqueue()*/

    }

    override fun onServicesInvalidated() {
        super.onServicesInvalidated()
        LogUtils.d("onServicesInvalidated: 外围的")
        mReadNotificationCharacteristic = null
        mWriteCharacteristic = null
    }


}