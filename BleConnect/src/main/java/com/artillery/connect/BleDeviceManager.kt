package com.artillery.connect

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.content.Context
import android.os.Handler
import android.util.Log
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import no.nordicsemi.android.ble.BleManager
import no.nordicsemi.android.ble.BuildConfig
import no.nordicsemi.android.ble.data.Data

/**
 * @author : zhiweizhu
 * create on: 2023/5/24 上午11:27
 */
class BleDeviceManager(val context: Context) : BleManager(context) {

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


    fun post(bytes: List<ByteArray>){
        mDefaultScope.launch {
            send(bytes)
        }
    }

    private suspend fun send(bytes: List<ByteArray>){
        suspendCancellableCoroutine { continuation ->
            writeCharacteristic(
                mWriteCharacteristic,
                "text".toByteArray(),
                BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
            )
                .done {
                continuation.resumeWith(Result.success(Unit))
            }.fail { device, status ->
                continuation.resumeWith(Result.failure(Exception("Could not set animationFactor: $status")))
            }.enqueue()
        }
    }


    override fun initialize() {
        super.initialize()

        requestMtu(238)
            .enqueue()

        setNotificationCallback(mReadNotificationCharacteristic).with { device, data ->
            data.value?.let { bytes ->
                if (bytes.isNotEmpty()) {
                    mDefaultScope.launch {
                        mReadNotificationCharacteristicChannel?.send(bytes)
                    }
                }
            }
        }

        beginAtomicRequestQueue()
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
            .enqueue()

    }

    override fun onServicesInvalidated() {
        super.onServicesInvalidated()
        LogUtils.d("onServicesInvalidated: 外围的")
        mReadNotificationCharacteristic = null
        mWriteCharacteristic = null
    }


}