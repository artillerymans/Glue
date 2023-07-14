package com.artillery.glue.ble.viewModels
import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artillery.connect.manager.BleDeviceManager
import com.artillery.glue.model.DebugBaseItem
import com.artillery.glue.model.DebugDataType
import com.artillery.connect.manager.JW002BleManage
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 下午5:59
 */
class JW002ConnectViewModel: ViewModel() {
    private val mBleDataChangeChannel: Channel<Pair<Int, ByteArray>> by lazy(LazyThreadSafetyMode.NONE) {
        Channel()
    }

    private val bluetoothObserver by lazy(LazyThreadSafetyMode.NONE) {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val bluetoothState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE,
                            -1
                        )
                        when (bluetoothState) {
                            BluetoothAdapter.STATE_ON -> enableBleServices()
                            BluetoothAdapter.STATE_OFF -> disableBleServices()
                        }
                    }

                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        LogUtils.d("Bond state changed for device ${device?.address}: ${device?.bondState}")
                        when (device?.bondState) {
                            BluetoothDevice.BOND_BONDED -> connect(device)
                            BluetoothDevice.BOND_NONE -> removeDevice(device)
                        }
                    }

                }
            }
        }
    }


    private fun enableBleServices() {
        val bluetoothManager =
            Utils.getApp().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        if (bluetoothManager.adapter?.isEnabled == true) {
            LogUtils.d("Enabling BLE services")
            if (ActivityCompat.checkSelfPermission(
                    Utils.getApp(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ToastUtils.showShort("蓝牙权限Manifest.permission.BLUETOOTH_CONNECT 未授予")
            }
            try {
                bluetoothManager.adapter.bondedDevices.forEach { device -> connect(device) }
            }catch (e: SecurityException){
                e.printStackTrace()
            }

        } else {
            LogUtils.d("Cannot enable BLE services as either there is no Bluetooth adapter or it is disabled")
        }
    }

    private fun disableBleServices() {
        JW002BleManage.getInstance().clear()
    }


    private var mCurrentDevice: BleDeviceManager? = null
    private val _connectStatusFlow = MutableStateFlow<ConnectionState?>(null)
    val connectStatusFlow: StateFlow<ConnectionState?> = _connectStatusFlow

    /**
     * 读写数据集合
     */
    private var _readWriteListFlow = MutableStateFlow<List<DebugBaseItem>>(emptyList())
    val readWriteListFlow: StateFlow<List<DebugBaseItem>> = _readWriteListFlow




    private fun removeDevice(device: BluetoothDevice) {
        JW002BleManage.getInstance().disConnect(device)
    }

    fun disConnect() {
        JW002BleManage.getInstance().clear()
    }

    fun connect(device: BluetoothDevice) {
        if (!JW002BleManage.getInstance().isConnect()){
            viewModelScope.launch {
                JW002BleManage.getInstance().connectStateFlow().collect { state ->
                    _connectStatusFlow.value = state
                }
            }
            JW002BleManage.getInstance().connect(device)
        }
    }


    /**
     * 写入数据
     */
    fun writeByteArray(bytes: ByteArray) {
        writeByteArrays(listOf(bytes))
    }

    /**
     * 写入List ByteArray
     */
    fun writeByteArrays(bytes: List<ByteArray>) {
        JW002BleManage.getInstance().also {
            noticeRefreshUI(bytes, DebugDataType.write)
            it.post(bytes)
        }
    }


    init {

        viewModelScope.launch {
            for (value in mBleDataChangeChannel) {
                ThreadUtils.getMainHandler().run {
                    //蓝牙回来的数据
                    LogUtils.d("接收到数据 => ${ConvertUtils.bytes2HexString(value.second)}")
                    noticeRefreshUI(
                        listOf(value.second),
                        if (value.first == JW002BleManage.NotifyACK)
                            DebugDataType.noticeAck
                        else
                            DebugDataType.notice
                    )
                    pack(value)
                }
            }
        }

        Utils.getApp().registerReceiver(bluetoothObserver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        })
        enableBleServices()
    }


    override fun onCleared() {
        super.onCleared()
        Utils.getApp().unregisterReceiver(bluetoothObserver)
    }


    /**
     * 缓存用的
     */
    private val mCacheList: MutableList<ByteArray> by lazy(LazyThreadSafetyMode.NONE) {
        mutableListOf()
    }

    fun pack(pair: Pair<Int, ByteArray>) {
        val buffer = pair.second.toBuffer()

    }

    private fun noticeRefreshUI(item: DebugBaseItem.PackItem) {
        val list = _readWriteListFlow.value.toMutableList()
        list.add(0, item)
        _readWriteListFlow.value = list
    }

    private fun noticeRefreshUI(bytes: List<ByteArray>, type: DebugDataType) {
        val list = _readWriteListFlow.value.toMutableList()
        list.addAll(
            0,
            bytes.mapIndexed { index, values ->
                val date = TimeUtils.getNowDate()
                val hexCmd = values[0].byte2Int().toString(16)
                DebugBaseItem.DebugItem(
                    type,
                    TimeUtils.date2String(date, TimeUtils.getSafeDateFormat("MM-dd HH:mm:ss SSS")),
                    values,
                    ConvertUtils.bytes2HexString(values),
                    index,
                    hexCmd
                )
            }.toList()
        )
        _readWriteListFlow.value = list
    }

    private fun desString(state: Int): String {
        return when (state) {
            BleConstantData.SUCCESS_BLE_CODE -> "->成功"
            BleConstantData.FAIL_BLE_CODE -> "->失败"
            else -> "->未知"
        }
    }

    private fun desSwitch(state: Int): String {
        return when (state) {
            0x01 -> "->开"
            0x00 -> "->关"
            else -> "->未知"
        }
    }



}