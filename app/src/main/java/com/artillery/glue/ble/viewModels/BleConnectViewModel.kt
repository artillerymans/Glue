package com.artillery.glue.ble.viewModels

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artillery.connect.BleDeviceManager
import com.artillery.connect.BleHelper
import com.artillery.glue.model.DebugBaseItem
import com.artillery.glue.model.DebugDataType
import com.artillery.rwutils.AnalyzeDataFactory
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.artillery.rwutils.model.Aggregate
import com.artillery.rwutils.model.SDD
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.GsonUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ThreadUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.ToastUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午4:41
 */
class BleConnectViewModel : ViewModel() {

    private val mBleDataChangeChannel: Channel<ByteArray> by lazy(LazyThreadSafetyMode.NONE) {
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
                            BluetoothDevice.BOND_BONDED -> addDevice(device)
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
                bluetoothManager.adapter.bondedDevices.forEach { device -> addDevice(device) }
            }catch (e: SecurityException){
                e.printStackTrace()
            }

        } else {
            LogUtils.d("Cannot enable BLE services as either there is no Bluetooth adapter or it is disabled")
        }
    }

    private fun disableBleServices() {
        BleHelper.getInstance().closeAll()
    }


    private var mCurrentDevice: BleDeviceManager? = null
    private val _connectStatusFlow = MutableStateFlow<ConnectionState?>(null)
    val connectStatusFlow: StateFlow<ConnectionState?> = _connectStatusFlow

    /**
     * 读写数据集合
     */
    private var _readWriteListFlow = MutableStateFlow<List<DebugBaseItem>>(emptyList())
    val readWriteListFlow: StateFlow<List<DebugBaseItem>> = _readWriteListFlow


    private fun addDevice(device: BluetoothDevice) {
        if (!BleHelper.getInstance().isContains(device.address)) {
            mCurrentDevice = BleDeviceManager().also {
                it.connectDevice(device)
                it.setChannel(mBleDataChangeChannel)
                viewModelScope.launch {
                    it.stateAsFlow().collect { state ->
                        _connectStatusFlow.value = state
                    }
                }
                BleHelper.getInstance().saveBleDeviceManagerByKey(device.address, it)
            }
        } else {
            mCurrentDevice = BleHelper.getInstance().getBleDeviceManager(device.address)
            mCurrentDevice?.also {
                it.setChannel(mBleDataChangeChannel)
                _connectStatusFlow.value = ConnectionState.Initializing
                viewModelScope.launch {
                    it.stateAsFlow().collect {
                        _connectStatusFlow.value = it
                    }
                }
                if (it.connectionState == BluetoothProfile.STATE_DISCONNECTED) {
                    it.connectDevice(device)
                }
            }

        }
    }


    private fun removeDevice(device: BluetoothDevice) {
        BleHelper.getInstance().remove(device.address)?.close()
    }

    fun disConnect() {
        BleHelper.getInstance().getBleDeviceManager()?.let {
            if (it.isConnected) {
                it.bluetoothDevice?.let { device ->
                    removeDevice(device)
                }
            }
        }
    }

    fun connect(device: BluetoothDevice) {
        addDevice(device)
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
        BleHelper.getInstance().getBleDeviceManager()?.also {
            //在UI上展示 调试使用
            noticeRefreshUI(bytes, DebugDataType.write)
            it.post(bytes)
        } ?: let {
            LogUtils.d("writeByteArrays: BleHelper.getInstance().getBleDeviceManager() == null")
        }
    }


    init {

        viewModelScope.launch {
            for (value in mBleDataChangeChannel) {
                ThreadUtils.getMainHandler().run {
                    //蓝牙回来的数据
                    LogUtils.d("接收到数据 => ${ConvertUtils.bytes2HexString(value)}")
                    noticeRefreshUI(
                        listOf(value),
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

    fun pack(bytes: ByteArray) {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        when (cmd) {
            BleConstantData.REPLY_CMD_81 -> {
                val state = buffer.get().byte2Int()
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        "设置日期时间${desString(state)}",
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_82 -> {
                val index = buffer.get().byte2Int()
                val state = buffer.get().byte2Int()
                val des = when (index) {
                    0x01 -> {
                        "设置用户信息${desString(state)}"
                    }

                    0x02 -> {
                        "设置通知开关${desString(state)}"
                    }

                    0x03 -> {
                        "设置闹钟${desString(state)}"
                    }

                    0x04 -> {
                        "设置勿扰、久坐、喝水提醒等${desString(state)}"
                    }
                    0x05 -> {
                        "设置表盘、单位等${desString(state)}"
                    }

                    else -> {
                        "设置未知的信息序号$index"
                    }
                }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        des,
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_94 -> {
                val battery = buffer.get().byte2Int()
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        "当前电量: $battery",
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_D1 -> {
                val state = buffer.get().byte2Int()
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        "查找手环->${
                            when (state) {
                                0x01 -> "图标显示并振动"
                                0x02 -> "图标不显示，并关闭振动"
                                BleConstantData.FAIL_BLE_CODE -> "失败"
                                else -> "未知状态"
                            }
                        }",
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_53 -> {
                val state = buffer.get().byte2Int()
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        "寻找手机: ${desSwitch(state)}",
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_B8 -> {
                val resutl = AnalyzeDataFactory.analyze0xB8For0x38(bytes)
                val des = resutl.data?.let {
                    if (it.order == 0xfffe){
                        "预备发送背景: ${if(it.state == 0) "失败" else "成功"}"
                    }else {
                        "发送完成背景: ${if(it.state == 0) "失败" else "成功"}"
                    }
                }.orEmpty().ifEmpty { "发送背景图片解数据异常" }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        des,
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_B9 -> {
                val resutl = AnalyzeDataFactory.analyze0xB9For0x39(bytes)
                val des = resutl.data?.let {
                    if (it.order == 0xfffffe){
                        "预备发送Bin: ${if(it.state == 0) "失败" else "成功"}"
                    }else {
                        "发送完成Bin: ${if(it.state == 0) "失败" else "成功"}"
                    }
                }.orEmpty().ifEmpty { "发送Bin解数据异常" }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        des,
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_9F -> {
                val result = AnalyzeDataFactory.analyze0x9fFor0x1f(buffer.array())
                LogUtils.d("pack: ${GsonUtils.toJson(result)}")
                val stringBuilder = StringBuilder().apply {
                    appendLine("编译日期：${result.data?.versionDes}")
                    append("版本：${result.data?.versionCode}")
                }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuilder.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_93 -> {
                val result = AnalyzeDataFactory.analyze0x93For0x13(buffer.array())
                LogUtils.d("pack: ${GsonUtils.toJson(result)}")
                val stringBuilder = StringBuilder().apply {
                    result.data?.let { data ->
                        if (data.hour == 0xff) {
                            appendLine("总步数-> ${data.year}-${data.month}-${data.day}")
                        } else {
                            appendLine("步数-> ${data.year}-${data.month}-${data.day} ${data.hour}")
                        }
                        append(GsonUtils.toJson(result.data))
                    } ?: append("无步数信息")
                }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuilder.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }


            BleConstantData.REPLY_CMD_95 -> {
                val result = AnalyzeDataFactory.analyze0x95For0x15(buffer.array())
                LogUtils.d("pack: ${GsonUtils.toJson(result)}")
                val stringBuilder = StringBuilder().apply {
                    result.data?.let { data ->
                        if (data.isNotEmpty()) {
                            appendLine(
                                "睡眠数据日期->${
                                    TimeUtils.millis2String(
                                        data.last().timeStamp.toLong(),
                                        TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                                    )
                                }"
                            )

                            data.forEach { sleep ->
                                appendLine(GsonUtils.toJson(sleep))
                            }
                        }
                    } ?: append("无睡眠信息")
                }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuilder.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_96 -> {
                val result = AnalyzeDataFactory.analyze0x96For0x16(buffer.array())
                LogUtils.d("pack: ${GsonUtils.toJson(result)}")
                val stringBuilder = StringBuilder().apply {
                    result.data?.let { data ->
                        if (data.isNotEmpty()) {
                            appendLine(
                                "心率数据日期->${
                                    TimeUtils.millis2String(
                                        data.last().timeStamp.toLong(),
                                        TimeUtils.getSafeDateFormat("yyyy-MM-dd")
                                    )
                                }"
                            )

                            data.forEach { item ->
                                appendLine(GsonUtils.toJson(item))
                            }
                        }
                    } ?: append("无心率血压血氧信息")
                }
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuilder.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }


            BleConstantData.REPLY_CMD_D2 -> {
                val state = buffer.get().byte2Int()
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        "App进入拍照: ${desSwitch(state)}",
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_B3 -> {
                val state = buffer.get().byte2Int()
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("实时数据->")
                    if (state == 0x01) {
                        appendLine("总步数: ${buffer.int.toUInt()}")
                        appendLine("卡路里：${buffer.short.toUShort()}")
                        appendLine("总里程(米)：${buffer.int.toUInt()}")
                        appendLine("活动时长(秒)：${buffer.int.toUInt()}")
                    } else {
                        append("无效")
                    }
                }



                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_E0 -> {
                val result = AnalyzeDataFactory.analyze0xE0For0x60(buffer.array())
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("实时测量血压心率血氧 开关->")
                    result.data?.let {
                        append(GsonUtils.toJson(it))
                    }
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }
            BleConstantData.REPLY_CMD_E1 -> {
                val result = AnalyzeDataFactory.analyze0xE1(buffer.array())
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("实时测量血压心率血氧->")
                    result.data?.let {
                        append(GsonUtils.toJson(it))
                    }
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_F1 -> {
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("收到恢复出厂设置->")
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_72 -> {
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("手表低电量->")
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }

            BleConstantData.REPLY_CMD_EE -> {
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("收到错误信息返回->")
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }
            BleConstantData.REPLY_CMD_97 -> {

                val result = AnalyzeDataFactory.analyze0x97For0x17(bytes)

                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("收到当前血压血氧血糖->")
                    result.data?.let { data ->
                        append(GsonUtils.toJson(data))
                    }
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }
            BleConstantData.REPLY_CMD_85 -> {
                //天气同步
                val result = AnalyzeDataFactory.analyze0x85For05(bytes)

                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("天气数据设置->${result.isSuccess()}")
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )

            }

            BleConstantData.REPLY_CMD_84 -> {
                //天气同步
                val result = AnalyzeDataFactory.analyze0x84For04(bytes)
                //实时数据
                val stringBuffer = StringBuilder().apply {
                    appendLine("同步紫外线->${result.isSuccess()}")
                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )

            }

            BleConstantData.REPLY_CMD_E2 -> {
                //通知开关
                val result = AnalyzeDataFactory.analyze0xE2For0x62(bytes)
                //
                val stringBuffer = StringBuilder().apply {
                    appendLine("读取信息->")
                    result.data?.let {
                        when (it) {
                            is Aggregate.NoticeAggregate -> {
                                appendLine("类型->通知")
                                append(
                                    GsonUtils.toJson(it)
                                )
                            }
                            is Aggregate.AlarmAggregate -> {
                                appendLine("类型->闹钟")
                                append(GsonUtils.toJson(it))
                               /* val startTime = it.alarmClocks.last().toHourMinute()
                                append("最后一个闹钟: ${startTime.first}:${startTime.second}")*/
                            }
                            is Aggregate.SddAggregate -> {
                                appendLine("类型->提醒")
                                it.sdds.forEach {sdd ->
                                    when(sdd){
                                        is SDD.Sedentary -> {
                                            appendLine("久坐: ${GsonUtils.toJson(sdd)}")
                                        }
                                        is SDD.DrinkingWater -> {
                                            appendLine("喝水: ${GsonUtils.toJson(sdd)}")
                                        }
                                        is SDD.DonTDisturb -> {
                                            appendLine("勿扰: ${GsonUtils.toJson(sdd)}")
                                        }
                                    }
                                }
                            }
                            is Aggregate.ClockDialUnitAggregate -> {
                                appendLine("类型->温度、单位")
                                appendLine("-->${GsonUtils.toJson(it)}")
                            }
                            else -> {

                            }
                        }
                    } ?: append("无效信息")

                }

                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        stringBuffer.toString(),
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )

            }



            else -> {
                noticeRefreshUI(
                    DebugBaseItem.PackItem(
                        "未知的命令码：0x${cmd.toString(16)}",
                        ConvertUtils.bytes2HexString(bytes)
                    )
                )
            }
        }
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