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
import com.artillery.connect.base.ABaseBleManager
import com.artillery.connect.manager.BleDeviceManager
import com.artillery.glue.model.DebugBaseItem
import com.artillery.glue.model.DebugDataType
import com.artillery.connect.manager.JW002BleManage
import com.artillery.protobuf.ProtoBufHelper
import com.artillery.protobuf.model.watch_cmds
import com.artillery.protobuf.utils.crcJW002
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.crc.crc
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.blankj.utilcode.util.ConvertUtils
import com.blankj.utilcode.util.GsonUtils
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
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 下午5:59
 */
class JW002ConnectViewModel : ViewModel() {
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
            } catch (e: SecurityException) {
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

        JW002BleManage.getInstance().setBleNotifyDataChannel(mBleDataChangeChannel)

        if (!JW002BleManage.getInstance().isConnect()) {
            viewModelScope.launch {
                JW002BleManage.getInstance().connectStateFlow().collect { state ->
                    LogUtils.d("connect: $state")
                    _connectStatusFlow.value = state
                }
            }
            JW002BleManage.getInstance().connect(device)
        }
    }


    /**
     * 写入数据
     */
    fun writeByteArray(bytes: ByteArray, characteristicType: Int = ABaseBleManager.WRITE) {
        writeByteArrays(listOf(bytes), characteristicType)
    }

    /**
     * 写入List ByteArray
     */
    fun writeByteArrays(bytes: List<ByteArray>, characteristicType: Int = ABaseBleManager.WRITE) {
        JW002BleManage.getInstance().also {
            noticeRefreshUI(bytes, DebugDataType.write)
            it.post(bytes, characteristicType)
        }
    }


    init {

        viewModelScope.launch {
            for (value in mBleDataChangeChannel) {
                ThreadUtils.getMainHandler().run {
                    //蓝牙回来的数据
                    LogUtils.d(
                        "接收到数据 => ${
                            ConvertUtils.bytes2HexString(
                                value.second.toBuffer(
                                    ByteOrder.LITTLE_ENDIAN
                                ).array()
                            )
                        }"
                    )
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



    fun pack(pair: Pair<Int, ByteArray>) {
        ProtoBufHelper.getInstance().receive(pair.second) { value ->

            if (value == null){
                LogUtils.d("pack: Ack 数据--->")
                return@receive
            }

            writeByteArray(
                ProtoBufHelper.getInstance().sendAckReply(0),
                JW002BleManage.WriteACK
            )

            val cmd = value.cmd
            LogUtils.d("pack: cmd -> ${cmd.name}")
            when (cmd) {
                watch_cmds.cmd_t.CMD_GET_BASE_PARAM -> {  // 获取基本信息
                    value.baseParam?.let { baseParamT ->
                        LogUtils.d("pack: baseParamT -> ${GsonUtils.toJson(baseParamT)}")
                        val mtuSize = baseParamT.mMtu
                        LogUtils.d("pack: 设置组包中的mut大小 -> $mtuSize")
                        //蓝牙设置mtu成功进行设置组包中的mtu大小
                        ProtoBufHelper.getInstance().setMtuSize(mtuSize)

                        /*JW002BleManage.getInstance().setMtuSize(mtuSize) { number ->

                            //未绑定
                            if (baseParamT.mIsBind == 0) {
                                writeByteArrays(
                                    ProtoBufHelper.getInstance().sendCMD_BIND_DEVICE()
                                )
                            }
                        }*/


                    }
                }

                watch_cmds.cmd_t.CMD_GET_DEVICE_INFO -> {  //获取设备信息
                    value.devInfo?.let { deviceInfo ->
                        LogUtils.d("pack: ${GsonUtils.toJson(deviceInfo)}")
                    }
                }

                watch_cmds.cmd_t.CMD_RING_PHONE_CTRL -> { //查找手机
                    value.ctrlCode?.let { data ->
                        LogUtils.d("pack: ${if (data.mCode == 0) "不响铃" else "响铃"}")
                    }
                }
                watch_cmds.cmd_t.CMD_SET_PHONE_INFO -> {  //手表信息发送设备
                    value.phoneInfo?.let { data ->
                        LogUtils.d("pack: phoneInfo -> ${GsonUtils.toJson(data)}")
                    }
                    LogUtils.d("pack: CMD_SET_PHONE_INFO -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_BIND_DEVICE -> {   //绑定设备
                    LogUtils.d("pack: CMD_BIND_DEVICE = ${value.ctrlCode.mCode}")
                    /*when (value.ctrlCode.mCode){
                        3 -> JW002BleManage.getInstance().bindDevice()
                    }*/
                }

                watch_cmds.cmd_t.CMD_SET_MESSAGE_SWITCH -> {   //设置通知开关
                    LogUtils.d("pack: CMD_SET_MESSAGE_SWITCH = ${value.errCode.err}")
                }

                watch_cmds.cmd_t.CMD_SET_MESSAGE_DATA -> {   //发送消息数据
                    LogUtils.d("pack: CMD_SET_MESSAGE_DATA = ${value.errCode.err}")
                }

                watch_cmds.cmd_t.CMD_GET_HR_CONFIG -> { //获取心率配置信息
                    value.hrConfig?.let {
                        LogUtils.d("pack: CMD_GET_HR_CONFIG = ${GsonUtils.toJson(it)}")
                    }
                }

                watch_cmds.cmd_t.CMD_SET_HR_CONFIG -> {  //设置心率信息
                    LogUtils.d("pack: CMD_SET_HR_CONFIG = ${value.errCode.err}")
                }

                watch_cmds.cmd_t.CMD_SYNC_HR_DETECT_VAL -> {  //同步心率测量值
                    LogUtils.d("pack: CMD_SYNC_HR_DETECT_VAL -> ${value.hrVal}")
                }

                watch_cmds.cmd_t.CMD_GET_SPO2_CONFIG -> {  //获取血氧配置
                    LogUtils.d("pack:CMD_GET_SPO2_CONFIG -> ${value.spo2Config}")
                }

                watch_cmds.cmd_t.CMD_SET_SPO2_CONFIG -> {  //设置血氧配置
                    LogUtils.d("pack: CMD_SET_SPO2_CONFIG -> ${value.errCode.err}")
                }

                watch_cmds.cmd_t.CMD_SYNC_SPO2_DETECT_VAL -> {  //同步最新血氧
                    LogUtils.d("pack: CMD_SYNC_SPO2_DETECT_VAL -> ${GsonUtils.toJson(value.hrVal)}")
                }
                watch_cmds.cmd_t.CMD_GET_STRESS_CONFIG -> {  //获取压力配置
                    LogUtils.d("pack: CMD_GET_STRESS_CONFIG -> ${value.stressConfig}")
                }

                watch_cmds.cmd_t.CMD_SET_STRESS_CONFIG -> {  //设置压力配置
                    LogUtils.d("pack: CMD_SET_STRESS_CONFIG -> ${value.errCode}")
                }
                watch_cmds.cmd_t.CMD_SYNC_STRESS_DETECT_VAL -> { //同步最新压力值
                    LogUtils.d("pack: CMD_SYNC_STRESS_DETECT_VAL -> ${value.stressVal}")
                }

                watch_cmds.cmd_t.CMD_GET_LONG_SIT_CONFIG -> { //获取久坐配置
                    LogUtils.d("pack: CMD_GET_LONG_SIT_CONFIG -> ${value.longsitInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_LONG_SIT_CONFIG -> { //设置久坐配置
                    LogUtils.d("pack: CMD_SET_LONG_SIT_CONFIG -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_GET_NOTDISTURB_CONFIG -> { //获取勿扰配置
                    LogUtils.d("pack: CMD_GET_NOTDISTURB_CONFIG -> ${value.notdisturbInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_NOTDISTURB_CONFIG -> { //设置勿扰配置
                    LogUtils.d("pack: CMD_SET_NOTDISTURB_CONFIG -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_CLOCK_ALARM_CONFIG -> { //同步闹钟配置
                    LogUtils.d("pack: CMD_SYNC_CLOCK_ALARM_CONFIG -> ${value.alarmInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_CLOCK_ALARM_CONFIG -> { //设置闹钟配置
                    LogUtils.d("pack: CMD_SET_CLOCK_ALARM_CONFIG -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_DRINK_ALARM_CONFIG -> { //同步喝水配置
                    LogUtils.d("pack: CMD_SYNC_DRINK_ALARM_CONFIG -> ${value.alarmInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_DRINK_ALARM_CONFIG -> { //设置喝水配置
                    LogUtils.d("pack: CMD_SET_DRINK_ALARM_CONFIG -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_MEDI_ALARM_CONFIG -> { //同步吃药配置
                    LogUtils.d("pack: CMD_SYNC_MEDI_ALARM_CONFIG -> ${value.alarmInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_MEDI_ALARM_CONFIG -> { //设置吃药配置
                    LogUtils.d("pack: CMD_SET_MEDI_ALARM_CONFIG -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_COUNTRY_INFO -> { //设置国家信息
                    LogUtils.d("pack: CMD_SET_COUNTRY_INFO -> ${value.countryInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_TIME_INFO -> { //设置时间信息
                    LogUtils.d("pack: CMD_SET_TIME_INFO -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_TIME_FMT -> { //设置时间格式
                    LogUtils.d("pack: CMD_SET_TIME_FMT -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_METRIC_INCH -> { //设置公英制
                    LogUtils.d("pack: CMD_SET_METRIC_INCH -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_BRIGHT_DURATION -> { //设置亮屏时间
                    LogUtils.d("pack: CMD_SET_BRIGHT_DURATION -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_MENU_STYLE -> { //设置菜单风格
                    LogUtils.d("pack: CMD_SET_MENU_STYLE -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_MENU_STYLE -> { //获取菜单风格
                    LogUtils.d("pack: CMD_SET_MENU_STYLE -> ${value.ctrlCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_DAY_SPORT_TARGET -> { //同步每天运动目标
                    LogUtils.d("pack: CMD_SYNC_DAY_SPORT_TARGET -> ${value.daySportInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_DAY_SPORT_TARGET -> { //设置每天运动目标
                    LogUtils.d("pack: CMD_SET_DAY_SPORT_TARGET -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_ACTUAL_STEP_INFO -> { //同步实时步数
                    LogUtils.d("pack: CMD_SYNC_ACTUAL_STEP_INFO -> ${value.daySportInfo}")
                }

                watch_cmds.cmd_t.CMD_SYNC_BATTERY_INFO -> { //同步电量
                    LogUtils.d("pack: CMD_SYNC_BATTERY_INFO -> ${value.batteryInfo}")
                }

                watch_cmds.cmd_t.CMD_RING_WATCH_CTRL -> { //手机控制手表响铃
                    LogUtils.d("pack: CMD_RING_WATCH_CTRL -> ${value.errCode}")
                }
                watch_cmds.cmd_t.CMD_CTRL_PHONE_TAKE_PICTURE -> {  //手表控制手机拍照
                    LogUtils.d("pack: CMD_CTRL_PHONE_TAKE_PICTURE -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_WEATHER_INFO -> {  //手机天气信息
                    LogUtils.d("pack: CMD_SET_WEATHER_INFO -> ${value.weatherInfo}")
                }
                watch_cmds.cmd_t.CMD_MUSIC_CTRL -> {  //手表控制播放
                    LogUtils.d("pack: CMD_MUSIC_CTRL -> ${value.musicCtrlInfo}")
                }

                watch_cmds.cmd_t.CMD_PHONE_MUSIC_INFO_UPDATE -> {  //手机更新播放信息
                    LogUtils.d("pack: CMD_MUSIC_CTRL -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_DEVICE_MODE -> {  //设置设备模式
                    LogUtils.d("pack: CMD_SET_DEVICE_MODE -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_PHONE_CALL_CTRL -> {  //设置设备模式
                    LogUtils.d("pack: CMD_PHONE_CALL_CTRL -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_GET_MENU_SEQUENCE_DATA -> {  //获取菜单序列编码
                    LogUtils.d("pack: CMD_GET_MENU_SEQUENCE_DATA -> ${value.menuSequenceInfo}")
                }

                watch_cmds.cmd_t.CMD_SET_MENU_SEQUENCE_DATA -> {  //设置菜单序列编码
                    LogUtils.d("pack: CMD_SET_MENU_SEQUENCE_DATA -> ${value.menuSequenceInfo}")
                }

                watch_cmds.cmd_t.CMD_PHONE_APP_SET_STATUS -> {  //设置手机状态
                    LogUtils.d("pack: CMD_PHONE_APP_SET_STATUS -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SET_WATCH_EVENT -> {  //通告手表事件
                    LogUtils.d("pack: CMD_SET_WATCH_EVENT -> ${value.errCode}")
                }
                watch_cmds.cmd_t.CMD_SET_LOG_INFO_DATA -> {  //设置Log记录上报
                    LogUtils.d("pack: CMD_SET_LOG_INFO_DATA -> ${value.errCode}")
                }
                watch_cmds.cmd_t.CMD_GET_LOG_INFO_DATA -> {  //获取Log记录上报
                    LogUtils.d("pack: CMD_SET_LOG_INFO_DATA -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CMD_SYNC_DIAL_CONFIG_DATA -> {  //获取表盘信息
                    LogUtils.d("pack: CMD_SYNC_DIAL_CONFIG_DATA -> ${value.dialConfig}")
                }

                watch_cmds.cmd_t.CMD_GET_HEALTH_DATA -> {  //获取某一天健康数据
                    LogUtils.d("pack: CMD_GET_HEALTH_DATA -> ${value.historyResponse}")
                }

                watch_cmds.cmd_t.CMD_GET_MUL_SPORT_IS_RUNNING -> {  //获取多运动是否正在运行
                    LogUtils.d("pack: CMD_GET_MUL_SPORT_IS_RUNNING -> ${value.ctrlCode}")
                }

                watch_cmds.cmd_t.CMD_SET_MUL_SPORT_STATUS -> {  //多运动状态控制
                    LogUtils.d("pack: CMD_SET_MUL_SPORT_STATUS -> ${value.mulSportParam}")
                }

                watch_cmds.cmd_t.CMD_SET_MUL_SPORT_STATUS -> {  //多运动状态控制
                    LogUtils.d("pack: CMD_SET_MUL_SPORT_STATUS -> ${value.mulSportParam}")
                }

                watch_cmds.cmd_t.CMD_GET_MUL_SPORT_RECORD_ABSTRACT -> {  //获取多运动记录摘要
                    LogUtils.d("pack: CMD_GET_MUL_SPORT_RECORD_ABSTRACT -> ${value.mulSportAbstract}")
                }

                watch_cmds.cmd_t.CMD_GET_MUL_SPORT_RECORD_DATA -> {  //获取多运动记录和日志数据
                    LogUtils.d("pack: CMD_GET_MUL_SPORT_RECORD_ABSTRACT -> ${value.mulSportRecord}")
                }

                watch_cmds.cmd_t.CMD_UPDATE_FILE_NOTIFY -> {  //通告文件升级
                    LogUtils.d("pack: CMD_UPDATE_FILE_NOTIFY -> ${value.errCode}")
                }

                watch_cmds.cmd_t.CDM_UPDATE_FILE_DATA_REQUEST -> {  //请求文件数据
                    LogUtils.d("pack: CMD_UPDATE_FILE_NOTIFY -> ${value.fileRequstParam}")
                }

                watch_cmds.cmd_t.CMD_UPDATE_FILE_RESULT_NOTIFY -> {  //通告升级结果
                    LogUtils.d("pack: CMD_UPDATE_FILE_NOTIFY -> ${value.fileResult}")
                }

                watch_cmds.cmd_t.CMD_CONTACTS_GET -> {  //获取所有通讯录
                    LogUtils.d("pack: CMD_CONTACTS_GET -> ${GsonUtils.toJson(value)}")
                    value.allContactsInfo?.mContactsList?.forEach { item ->
                        LogUtils.d("pack: name = ${item.mName.toStringUtf8()}, number = ${item.mNumber.toStringUtf8()}")
                    }

                }

                watch_cmds.cmd_t.CMD_CONTACTS_SET -> {  //设置通讯录
                    LogUtils.d("pack: CMD_CONTACTS_GET -> ${value.contactsInfo}")
                }

                watch_cmds.cmd_t.CMD_CONTACTS_SYNC -> {  //获取通讯录所有
                    LogUtils.d("pack: CMD_CONTACTS_SYNC -> ${value.contactsInfo}")
                }






















                else -> {
                    LogUtils.d("pack: 未知命令 -> ${cmd.name}， error = ${value.errCode?.err}")
                }
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
        bytes.forEachIndexed { index, bytes ->
            val watch = analysisByteArray(bytes)
            if (watch != null) {
                val date = TimeUtils.getNowDate()
                list.add(
                    0,
                    DebugBaseItem.DebugItem(
                        type,
                        TimeUtils.date2String(
                            date,
                            TimeUtils.getSafeDateFormat("MM-dd HH:mm:ss SSS")
                        ),
                        bytes,
                        GsonUtils.toJson(watch),
                        index,
                        watch.cmd.name
                    )
                )
            }

        }
        _readWriteListFlow.value = list
    }


    private val mCacheBytes by lazy(LazyThreadSafetyMode.NONE) {
        mutableListOf<Byte>()
    }

    fun analysisByteArray(bytes: ByteArray): watch_cmds? {
        //转换成小端
        val buffer = ByteBuffer.wrap(bytes).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }
        //包头
        val tempHeader = buffer.short.toUShort().toString(16)
        if (!ProtoBufHelper.Companion.Ble.BLE_HEADER.equals(tempHeader, true)) {
            LogUtils.d("analysisByteArray: 包头为 => $tempHeader, 不进行解析")
            return null
        }
        //数据长度
        val length = buffer.short.toUShort()
        //校验和
        val crc = buffer.short.toUShort()
        val tempBytes = ByteArray(buffer.remaining())
        buffer.get(tempBytes)

        //说明当前一包数据就够了 不需要进行合包
        if (length == tempBytes.size.toUShort()) {
            return ProtoBufHelper.getInstance().bytes2WatchCmd(tempBytes, crc)
        } else {
            //走到这里说明当前数据是分包了的
            mCacheBytes.addAll(tempBytes.toList())
            val tempLength = mCacheBytes.size.toUShort()
            if (length == tempLength) {
                val tempCacheBytes = mCacheBytes.toByteArray()
                //crc校验通过
                val watch = ProtoBufHelper.getInstance().bytes2WatchCmd(tempCacheBytes, crc)
                watch?.let {
                    mCacheBytes.clear()
                    return it
                }
            }
            return null
        }
    }


}