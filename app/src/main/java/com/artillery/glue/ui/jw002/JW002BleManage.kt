package com.artillery.glue.ui.jw002

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import androidx.collection.ArraySet
import com.artillery.connect.BleDeviceManager
import com.artillery.connect.base.ABaseBleManager
import com.artillery.connect.interfaces.IConnectListening
import com.artillery.connect.interfaces.IDataChangeListening
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow
import java.util.UUID

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 下午6:02
 */
class JW002BleManage {

    private var mJW002Manage: JW002Manage? = null



    companion object {

        private const val FilterUUID: String = "0000fee7-0000-1000-8000-00805f9b34fb"
        private const val ServiceSCServiceUUIDMain: String = "000001ff-0000-0100-8000-00807c5634fb"
        private const val ServiceSCCharacteristicsWrite: String = "000002ff-0000-0100-8000-00807c5634fb"
        private const val ServiceSCCharacteristicsNotify: String = "000003ff-0000-0100-8000-00807c5634fb"
        private const val ServiceSCACKCharacteristicsWrite: String = "000004ff-0000-0100-8000-00807c5634fb"
        private const val ServiceSCACKCharacteristicsNotify: String = "000005ff-0000-0100-8000-00807c5634fb"

        const val WriteACK = 100
        const val NotifyACK = 101


        fun getInstance() = Helper.instance
    }


    private object Helper {
        val instance = JW002BleManage()
    }

    fun connect(device: BluetoothDevice) {
        clear()
        mJW002Manage = JW002Manage()
        mJW002Manage?.connectByBluetoothDevice(device)
    }

    fun disConnect(device: BluetoothDevice){
        mJW002Manage?.let { manage ->
            manage.disconnect()
                .done {
                    LogUtils.d("disConnect: 断开成功 -> ${device.address}")
                    manage.close()
                    clear()
                }
                .fail { device, status ->
                    LogUtils.d("disConnect: 断开失败 -> ${device?.address}")
                }
                .enqueue()
        }
    }


    fun post(bytes: ByteArray, characteristicType: Int = ABaseBleManager.WRITE) {
        post(listOf(bytes), characteristicType)
    }

    fun post(list: List<ByteArray>, characteristicType: Int = ABaseBleManager.WRITE) {
        mJW002Manage?.post(list, characteristicType)
    }


    private var mNotificationCharacteristicChannel: SendChannel<Pair<Int, ByteArray>>? = null

    fun setBleNotifyDataChannel(channel: Channel<Pair<Int, ByteArray>>){
        mNotificationCharacteristicChannel = channel
    }


    private val mBleDataChangeList: ArraySet<IDataChangeListening> by lazy(LazyThreadSafetyMode.NONE){
        ArraySet()
    }


    fun registerBleDataChangeListening(call: IDataChangeListening){
        mBleDataChangeList.add(call)
    }

    fun unregisterBleDataChangeListening(call: IDataChangeListening){
        mBleDataChangeList.remove(call)
    }

    /**
     * 蓝牙连接状态流
     */
    private val _connectStatusFlow = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.UNKNOWN))
    private val mConnectStatusFlow: StateFlow<ConnectionState> = _connectStatusFlow

    private val mConnectStatusList: ArraySet<IConnectListening> by lazy(LazyThreadSafetyMode.NONE){
        ArraySet()
    }


    fun registerConnectListening(call: IConnectListening){
        mConnectStatusList.add(call)
    }

    fun unregisterConnectListening(call: IConnectListening){
        mConnectStatusList.remove(call)
    }


    /**
     * 通过这个流可以观察到状态
     */
    fun connectStateFlow(): StateFlow<ConnectionState>{
        return mConnectStatusFlow
    }



    fun getBleState(): ConnectionState {
        return _connectStatusFlow.value
    }

    fun isConnect(): Boolean {
        return mJW002Manage?.isConnected ?: false
    }


    fun clear() {
        mJW002Manage?.run {
            disconnect().enqueue()
            close()
        }
        _connectStatusFlow.value = ConnectionState.Disconnected(ConnectionState.Disconnected.Reason.UNKNOWN)
        mJW002Manage = null
    }


    private inner class JW002Manage : ABaseBleManager() {

        private var mWriteCharacteristic: BluetoothGattCharacteristic? = null
        private var mWriteAckCharacteristic: BluetoothGattCharacteristic? = null
        private var mNotifyCharacteristic: BluetoothGattCharacteristic? = null
        private var mNotifyAckCharacteristic: BluetoothGattCharacteristic? = null

        init {
            mDefaultScope.launch {
                stateAsFlow().collect {
                    _connectStatusFlow.value = it

                    mConnectStatusList.forEach { value ->
                        value.onConnectChange(it)
                    }
                }
            }
        }


        override fun isRequiredServiceSupported(gatt: BluetoothGatt): Boolean {
            val service = gatt.getService(UUID.fromString(ServiceSCServiceUUIDMain))
            service?.let {
                mWriteCharacteristic =
                    it.getCharacteristic(UUID.fromString(ServiceSCCharacteristicsWrite))
                mWriteAckCharacteristic =
                    it.getCharacteristic(UUID.fromString(ServiceSCACKCharacteristicsWrite))
                mNotifyCharacteristic =
                    it.getCharacteristic(UUID.fromString(ServiceSCCharacteristicsNotify))
                mNotifyAckCharacteristic =
                    it.getCharacteristic(UUID.fromString(ServiceSCACKCharacteristicsNotify))
            }

            return mWriteCharacteristic != null
                    && mWriteAckCharacteristic != null
                    && mNotifyCharacteristic != null
                    && mNotifyAckCharacteristic != null
        }

        override fun initialize() {
            super.initialize()
            requestMtu(23)
                .done {
                    LogUtils.d("initialize: requestMtu size 23")
                }.enqueue()

            setNotificationCallback(mNotifyCharacteristic)
                .with { device, data ->
                    LogUtils.d(
                        "initialize: mNotifyCharacteristic -> 数据 -> ${
                            ConvertUtils.bytes2HexString(
                                data.value
                            )
                        }"
                    )

                    data.value?.let { bytes ->
                        if (bytes.isNotEmpty()) {

                            mBleDataChangeList.forEach { value ->
                                value.onDataChange(NOTIFICATION, bytes)
                            }
                            mDefaultScope.launch {
                                mNotificationCharacteristicChannel?.send(Pair(NOTIFICATION, bytes))
                            }
                        }
                    }
                }

            enableNotifications(mNotifyCharacteristic)
                .done {
                    LogUtils.d("initialize: enableNotifications -> mNotifyCharacteristic")
                }
                .fail { device, status ->
                    val deviceMac = device?.address.orEmpty()
                    LogUtils.d("initialize: mNotifyCharacteristic 通知 deviceMac = $deviceMac ==> $status")
                    disconnect().enqueue()
                }
                .enqueue()

            setNotificationCallback(mNotifyAckCharacteristic)
                .with { device, data ->
                    LogUtils.d(
                        "initialize: mNotifyAckCharacteristic -> 数据 -> ${
                            ConvertUtils.bytes2HexString(
                                data.value
                            )
                        }"
                    )

                    data.value?.let { bytes ->
                        if (bytes.isNotEmpty()) {
                            mBleDataChangeList.forEach { value ->
                                value.onDataChange(NotifyACK, bytes)
                            }

                            mDefaultScope.launch {
                                mNotificationCharacteristicChannel?.send(Pair(NotifyACK, bytes))
                            }
                        }
                    }
                }

            enableNotifications(mNotifyAckCharacteristic)
                .done {
                    LogUtils.d("initialize: enableNotifications -> mNotifyAckCharacteristic")
                }
                .fail { device, status ->
                    val deviceMac = device?.address.orEmpty()
                    LogUtils.d("initialize: mNotifyAckCharacteristic 通知 deviceMac = $deviceMac ==> $status")
                    disconnect().enqueue()
                }
                .enqueue()


        }


        override fun onGetCharacteristic(type: Int): BluetoothGattCharacteristic? {
            return when (type) {
                WRITE -> mWriteCharacteristic
                WriteACK -> mWriteAckCharacteristic
                NOTIFICATION -> mNotifyCharacteristic
                NotifyACK -> mNotifyAckCharacteristic
                else -> null
            }
        }

        override fun onServicesInvalidated() {
            super.onServicesInvalidated()
            mWriteCharacteristic = null
            mWriteAckCharacteristic = null
            mNotifyCharacteristic = null
            mNotifyAckCharacteristic = null
        }
    }

}