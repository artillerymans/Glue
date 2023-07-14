package com.artillery.connect

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Handler
import android.util.Log
import com.artillery.connect.base.ABaseBleManager
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.delay
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
class BleDeviceManager(context: Context = Utils.getApp()) : ABaseBleManager(context) {

    private var mReadNotificationCharacteristic: BluetoothGattCharacteristic? = null
    private var mWriteCharacteristic: BluetoothGattCharacteristic? = null
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

    fun setChannel(channel: Channel<ByteArray>){
        mReadNotificationCharacteristicChannel = channel
    }


    override fun onGetCharacteristic(type: Int): BluetoothGattCharacteristic? {
       return when(type){
            WRITE -> mWriteCharacteristic
            READ -> mReadNotificationCharacteristic
            NOTIFICATION -> mReadNotificationCharacteristic
            else -> null
        }
    }


    override fun initialize() {
        super.initialize()

        requestMtu(238)
            .done {

            }
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

    }

    override fun onServicesInvalidated() {
        super.onServicesInvalidated()
        LogUtils.d("onServicesInvalidated: 外围的")
        mReadNotificationCharacteristic = null
        mWriteCharacteristic = null
    }


}