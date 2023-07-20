package com.artillery.protobuf

import com.artillery.protobuf.model.alarm_clock_t
import com.artillery.protobuf.model.alarm_info_t
import com.artillery.protobuf.model.bright_times_t
import com.artillery.protobuf.model.calendar_info_t
import com.artillery.protobuf.model.contacts_all_info_t
import com.artillery.protobuf.model.contacts_info_set_t
import com.artillery.protobuf.model.contacts_info_t
import com.artillery.protobuf.model.country_info_t
import com.artillery.protobuf.model.ctrl_code_t
import com.artillery.protobuf.model.day_sport_info_t
import com.artillery.protobuf.model.file_info_t
import com.artillery.protobuf.model.file_result_t
import com.artillery.protobuf.model.health_history_request_t
import com.artillery.protobuf.model.health_history_response_t
import com.artillery.protobuf.model.hr_config_info_t
import com.artillery.protobuf.model.key_consult_t
import com.artillery.protobuf.model.longsit_info_t
import com.artillery.protobuf.model.menu_sequence_info_t
import com.artillery.protobuf.model.message_info_t
import com.artillery.protobuf.model.message_type_set_t
import com.artillery.protobuf.model.mul_sport_ctrl_param_t
import com.artillery.protobuf.model.music_ctrl_info_t
import com.artillery.protobuf.model.music_info_t
import com.artillery.protobuf.model.notdisturb_info_t
import com.artillery.protobuf.model.phone_info_t
import com.artillery.protobuf.model.spo2_config_info_t
import com.artillery.protobuf.model.stress_config_info_t
import com.artillery.protobuf.model.time_fmt_t
import com.artillery.protobuf.model.user_info_t
import com.artillery.protobuf.model.watch_cmds
import com.artillery.protobuf.model.watch_cmds.Builder
import com.artillery.protobuf.model.watch_cmds.cmd_t
import com.artillery.protobuf.model.weather_day_info_t
import com.artillery.protobuf.model.weather_info_t
import com.artillery.protobuf.utils.crcJW002
import com.artillery.protobuf.utils.createBytes
import com.artillery.protobuf.utils.createPkeySkey
import com.artillery.protobuf.utils.createRandomByteArray
import com.artillery.protobuf.utils.createWatchCommand
import com.artillery.protobuf.utils.isNotEmpty
import com.artillery.protobuf.utils.sha256ToByteArray
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.DeviceUtils
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.RomUtils
import com.google.protobuf.kotlin.toByteStringUtf8
import java.net.Authenticator.RequestorType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.time.Year
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 上午11:09
 */
class ProtoBufHelper private constructor() {

    companion object {

        object Ble {
            const val BLE_HEADER = "A55A"
            const val BLE_HEADER_ACK = "AA55"
            const val BLE_HEADER_ACK_REPLY = "7766"
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
    private val mCacheBytes by lazy(LazyThreadSafetyMode.NONE) {
        mutableListOf<Byte>()
    }


    private object Helper {
        val instance = ProtoBufHelper()
    }

    fun setMtuSize(size: Int) {
        if (size >= 23) {
            mMtuSize = size
        }
    }

    /**
     * 获取基本配置参数
     */
    fun sendCMD_GET_BASE_PARAM(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_BASE_PARAM)
    }

    /**
     * 绑定设备
     */
    fun sendCMD_BIND_DEVICE(
        userID: Int = UInt.MAX_VALUE.toInt(),
        userName: String = "alex",
        age: Int = 18,
        gender: Int = 0,
        height: Int = 180,
        weight: Int = 75,
        wearStyle: Int = 1,
    ): List<ByteArray> {
        return createBase {
            setCmd(cmd_t.CMD_BIND_DEVICE)
            setUserInfo(
                user_info_t.newBuilder()
                    .setMUsrid(userID)
                    .setMGender(gender)
                    .setMAge(age)
                    .setMHeight(height)
                    .setMWeight(weight)
                    .setMWearstyle(wearStyle)
                    .setMUserName(userName.toByteStringUtf8())
                    .build()
            )
        }
    }

    /**
     * 获取设备信息
     */
    fun sendCMD_GET_DEVICE_INFO(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_DEVICE_INFO)
    }

    /**
     * 发送手机信息到手表设备
     */
    fun sendCMD_SET_PHONE_INFO(): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_PHONE_INFO
            setPhoneInfo(
                phone_info_t.newBuilder()
                    .setMLanguage(JW002Language.systemLanguage().value)
                    .setMAppversion(AppUtils.getAppVersionCode())
                    .setMPhonemodel(0)
                    .setMSystemversion(DeviceUtils.getSDKVersionCode())
                    .build()
            )
        }
    }

    /**
     * 设置短信开关
     */
    fun sendCMD_SET_MESSAGE_SWITCH(vararg values: MessageSwitch): List<ByteArray> {
        val defValue = UInt.MIN_VALUE
        var tempValue = 0u
        values.forEach { msg ->
            val tempTypeValue = when (msg.type) {
                MsgType.All -> msg.value.toUInt()
                MsgType.Instagram -> msg.value.toUInt() shl 1
                MsgType.Linkedin -> msg.value.toUInt() shl 2
                MsgType.Twitter -> msg.value.toUInt() shl 3
                MsgType.FaceBook -> msg.value.toUInt() shl 4
                MsgType.FaceTime -> msg.value.toUInt() shl 5
                MsgType.Feixin -> msg.value.toUInt() shl 6
                MsgType.Line -> msg.value.toUInt() shl 7
                MsgType.Sound -> msg.value.toUInt() shl 8
                MsgType.Gmail -> msg.value.toUInt() shl 9
                MsgType.Webook -> msg.value.toUInt() shl 10
                MsgType.Wechat -> msg.value.toUInt() shl 11
                MsgType.QQ -> msg.value.toUInt() shl 12
                MsgType.Sms -> msg.value.toUInt() shl 13
                MsgType.Call -> msg.value.toUInt() shl 14
                MsgType.Skype -> msg.value.toUInt() shl 15
                MsgType.DingTalk -> msg.value.toUInt() shl 16
                MsgType.AliWangWang -> msg.value.toUInt() shl 17
                MsgType.Alipay -> msg.value.toUInt() shl 18
                MsgType.KakaoTalk -> msg.value.toUInt() shl 19
                MsgType.Qianiu -> msg.value.toUInt() shl 20
                MsgType.WhatsApp -> msg.value.toUInt() shl 21
                MsgType.Pinterest -> msg.value.toUInt() shl 22
                MsgType.OtherApp -> msg.value.toUInt() shl 23
                MsgType.Message -> msg.value.toUInt() shl 24
                else -> defValue
            }
            tempValue = tempTypeValue xor defValue xor tempValue
        }
        LogUtils.d("sendCMD_SET_MESSAGE_SWITCH: $tempValue")
        return createBase {
            cmd = cmd_t.CMD_SET_MESSAGE_SWITCH
            setMsgType(
                message_type_set_t.newBuilder()
                    .setMType(tempValue.toInt())
                    .build()
            )
        }
    }


    /**
     * 发送消息数据到手表
     */
    fun sendCMD_SET_MESSAGE_DATA(
        msgType: MsgType,
        title: String,
        content: String,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_MESSAGE_DATA
            setMsgData(
                message_info_t.newBuilder()
                    .setMType(msgType.value)
                    .setMMsg(content.toByteStringUtf8())
                    .setMTitle(title.toByteStringUtf8())
                    .build()
            )
        }
    }


    /**
     * 获取心率配置信息
     * 应答包 hr_config_info_t
     */
    fun sendCMD_GET_HR_CONFIG(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_HR_CONFIG)
    }


    /**
     * 设置心率配置信息
     * @param enable 自动检测开关
     * @param interval 自动检测时间 单位分钟
     * @param warmingMax 预警值上限
     * @param warmingMin 预警值下限
     * 应答 err_code
     */
    fun sendCMD_SET_HR_CONFIG(
        enable: SwitchType,
        interval: Int,
        warmingMax: Int,
        warmingMin: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_HR_CONFIG
            setHrConfig(
                hr_config_info_t.newBuilder()
                    .setMAuto(enable.value)
                    .setMInterval(interval)
                    .setMWarmingUpper(warmingMax)
                    .setMWarmingLower(warmingMin)
                    .build()
            )
        }
    }


    /**
     * 同步最新心率值
     *
     * 应答 hr_val_t
     */
    fun sendCMD_SYNC_HR_DETECT_VAL(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_HR_DETECT_VAL)
    }

    /**
     * 获取血氧配置
     *
     * 应答 spo2_config_info_t
     */
    fun sendCMD_GET_SPO2_CONFIG(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_SPO2_CONFIG)
    }

    /**
     * 设置血氧配置信息
     * @param auto 夜间自动检测
     * @param interval 自动检测时间 单位分钟
     * @param warmingMax 预警值上限
     * @param warmingMin 预警值下限
     * 应答 err_code
     */
    fun sendCMD_SET_SPO2_CONFIG(
        enable: SwitchType,
        interval: Int,
        warmingMax: Int,
        warmingMin: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_SPO2_CONFIG
            setSpo2Config(
                spo2_config_info_t.newBuilder()
                    .setMNightAuto(enable.value)
                    .setMNightInterval(interval)
                    .setMWarmingUpper(warmingMax)
                    .setMWarmingLower(warmingMin)
                    .build()
            )
        }
    }

    /**
     * 同步最新血氧检测值
     */
    fun sendCMD_SYNC_SPO2_DETECT_VAL(): List<ByteArray> {
        return sendNoParameters(
            cmd_t.CMD_SYNC_SPO2_DETECT_VAL
        )
    }


    /**
     * 获取压力配置信息
     * 应答 stress_config_info
     */
    fun sendCMD_GET_STRESS_CONFIG(): List<ByteArray> {
        return sendNoParameters(
            cmd_t.CMD_GET_STRESS_CONFIG
        )
    }

    /**
     * 设置压力配置信息
     *  @param enable 夜间自动检测
     *  @param interval 检测间隔时间
     *  应答 err_code
     */
    fun sendCMD_SET_STRESS_CONFIG(
        enable: SwitchType,
        interval: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_STRESS_CONFIG
            setStressConfig(
                stress_config_info_t.newBuilder()
                    .setMAuto(enable.value)
                    .setMInterval(interval)
                    .build()
            )
        }
    }


    /**
     * 同步最新压力测量值
     * 应答 stress_config_info_t
     */
    fun sendCMD_SYNC_STRESS_DETECT_VAL(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_STRESS_DETECT_VAL)
    }


    /**
     * 获取久坐配置信息
     *
     * 应答 logsit_info
     */
    fun sendCMD_GET_LONG_SIT_CONFIG(): List<ByteArray> {
        return sendNoParameters(
            cmd_t.CMD_GET_LONG_SIT_CONFIG
        )
    }

    /**
     * 设置久坐配置信息
     * @param enable 开关
     * @param duration 持续时间
     * @param sHour 开始时间 时
     * @param sMinute 开始时间 分
     * @param eHour 结束时间 时
     * @param eMinute 结束时间 分
     * @param nEnable 勿扰使能开关
     * @param nsHour 勿扰开始时间 时
     * @param nsMinute 勿扰开始时间 分
     * @param neHour 勿扰结束时间 时
     * @param neMinute 勿扰结束时间 分
     * 应答 err_code
     */
    fun sendCMD_SET_LONG_SIT_CONFIG(
        enable: SwitchType,
        duration: Int = 0,
        sHour: Int = 0,
        sMinute: Int = 0,
        eHour: Int = 0,
        eMinute: Int = 0,
        nEnable: Int = 0,
        nsHour: Int = 0,
        nsMinute: Int = 0,
        neHour: Int = 0,
        neMinute: Int = 0,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_LONG_SIT_CONFIG
            setLongsitInfo(
                longsit_info_t.newBuilder()
                    .setMEable(enable.value)
                    .setMDuration(duration)
                    .setMStartHour(sHour)
                    .setMStartMinute(sMinute)
                    .setMEndHour(eHour)
                    .setMEndMinute(eMinute)

                    .setMNotdisturbEnable(nEnable)
                    .setMNotdisturbStartHour(nsHour)
                    .setMNotdisturbStartMinute(nsMinute)
                    .setMNotdisturbEndHour(neHour)
                    .setMNotdisturbEndMinute(neMinute)
                    .build()
            )
        }
    }

    /**
     * 获取勿扰配置信息
     * 应答 notdisturb_info_t
     */
    fun sendCMD_GET_NOTDISTURB_CONFIG(): List<ByteArray> {
        return sendNoParameters(
            cmd_t.CMD_GET_NOTDISTURB_CONFIG
        )
    }

    /**
     * 设置勿扰配置信息
     * @param enable 开关
     * @param type 1 为定时模式 0为全天模式
     * @param sHour 开始小时
     * @param sMinute 开始分钟
     * @param eHour 结束小时
     * @param eMinute 结束分钟
     *
     * 应答 err_code
     */
    fun sendCMD_SET_NOTDISTURB_CONFIG(
        enable: SwitchType,
        type: Int,
        sHour: Int = 0,
        sMinute: Int = 0,
        eHour: Int = 0,
        eMinute: Int = 0,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_NOTDISTURB_CONFIG
            setNotdisturbInfo(
                notdisturb_info_t.newBuilder()
                    .setMEnable(enable.value)
                    .setMType(type)
                    .setMStartHour(sHour)
                    .setMStartMinute(sMinute)
                    .setMEndHour(eHour)
                    .setMEndMinute(eMinute)
                    .build()
            )
        }
    }


    /**
     * 同步闹钟信息
     *
     * 应答 alarm_info_t
     */
    fun sendCMD_SYNC_CLOCK_ALARM_CONFIG(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_CLOCK_ALARM_CONFIG)
    }


    /**
     * 创建闹钟
     * @param user 是否有效 要不要显示出来
     * @param enable 是否打开
     * @param type 0 只执行一次 1重复执行
     * @param repeat 重复类型 bit0~bit6 代表礼拜一到礼拜天 如果闹钟只执行一次为0
     * @param hour 时间 时
     * @param minute 时间 分
     * @param mark 备注
     */
    fun createAlarm(
        user: Int,
        enable: SwitchType,
        type: Int,
        repeat: Int,
        hour: Int,
        minute: Int,
        mark: String,
    ): alarm_clock_t {
        return alarm_clock_t.newBuilder()
            .setMUse(user)
            .setMEnable(enable.value)
            .setMType(type)
            .setMRepeat(repeat)
            .setMHour(hour)
            .setMMinute(minute)
            .setMRemark(mark.toByteStringUtf8())
            .build()
    }

    /**
     * 设置闹钟信息
     * 创建内容 参看
     * @see createAlarm
     */
    fun sendCMD_SET_CLOCK_ALARM_CONFIG(enable: SwitchType, list: List<alarm_clock_t>): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_CLOCK_ALARM_CONFIG
            setAlarmInfo(
                alarm_info_t.newBuilder()
                    .setMEnable(enable.value)
                    .apply {
                        list.forEach { alarmClockT ->
                            addMInfo(alarmClockT)
                        }
                    }
                    .build()
            )
        }
    }

    /**
     * 同步喝水闹钟信息
     */
    fun sendCMD_SYNC_DRINK_ALARM_CONFIG(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_DRINK_ALARM_CONFIG)
    }

    /**
     * 设置喝水闹钟信息
     */
    fun sendCMD_SET_DRINK_ALARM_CONFIG(enable: SwitchType,list: List<alarm_clock_t>): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_DRINK_ALARM_CONFIG
            setAlarmInfo(
                alarm_info_t.newBuilder()
                    .setMEnable(enable.value)
                    .apply {
                        list.forEach { alarmClockT ->
                            addMInfo(alarmClockT)
                        }
                    }
                    .build()
            )
        }
    }

    /**
     * 同步吃药闹钟
     */
    fun sendCMD_SYNC_MEDI_ALARM_CONFIG(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_MEDI_ALARM_CONFIG)
    }


    /**
     * 设置吃药闹钟信息
     */
    fun sendCMD_SET_MEDI_ALARM_CONFIG(enable: SwitchType, list: List<alarm_clock_t>): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_MEDI_ALARM_CONFIG
            setAlarmInfo(
                alarm_info_t.newBuilder()
                    .setMEnable(enable.value)
                    .apply {
                        list.forEach {  alarmClockT ->
                            addMInfo(alarmClockT)
                        }
                    }
                    .build()
            )
        }
    }


    /**
     * 设置国家信息
     * @param countryName 国家名称
     * @param zone 时区
     */
    fun sendCMD_SET_COUNTRY_INFO(
        countryName: String, zone: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_COUNTRY_INFO
            setCountryInfo(
                country_info_t.newBuilder()
                    .setMName(countryName.toByteStringUtf8())
                    .setMTimezone(zone)
                    .build()
            )
        }
    }

    /**
     * 设置时间
     * @param 年月日时分秒
     * @param zone 时区
     */
    fun sendCMD_SET_TIME_INFO(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        zone: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_TIME_INFO
            setCalendarInfo(
                calendar_info_t.newBuilder()
                    .setMYear(year)
                    .setMMonth(month)
                    .setMDay(day)
                    .setMHour(hour)
                    .setMMinute(minute)
                    .setMSecond(second)
                    .setMTimeZone(zone)
                    .build()
            )
        }
    }


    /**
     * 设置时间格式
     * @param format 0为24小时制 1为12小时制
     */
    fun sendCMD_SET_TIME_FMT(format: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_TIME_FMT
            setTimeFmt(
                time_fmt_t.newBuilder()
                    .setMFmt(format)
                    .build()
            )
        }
    }


    /**
     * 设置公英制
     * @param format 0为公制 1为英制
     */
    fun sendCMD_SET_METRIC_INCH(format: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_METRIC_INCH
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(format)
                    .build()
            )
        }
    }

    /**
     * 设置亮屏时长
     * @param time 时长 单位秒
     */
    fun sendCMD_SET_BRIGHT_DURATION(time: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_BRIGHT_DURATION
            setBrightTimes(
                bright_times_t.newBuilder()
                    .setMBrightTime(time)
                    .build()
            )
        }
    }


    /**
     * 设置菜单风格
     * @param style 样式 0蜂窝 1瀑布流
     */
    fun sendCMD_SET_MENU_STYLE(style: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_MENU_STYLE
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(style)
                    .build()
            )
        }
    }


    /**
     * 同步每天运动目标
     * 应答 day_sport_info
     */
    fun sendCMD_SYNC_DAY_SPORT_TARGET(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_DAY_SPORT_TARGET)
    }

    /**
     * 设置每天运动目标
     * @param step 每天步数目标
     * @param caloriesKcal 每天卡路里目标
     * @param distance 每天距离目标
     * @param time 每天运动时长目标
     * @param count 每天运动次数目标
     */
    fun sendCMD_SET_DAY_SPORT_TARGET(
        step: Int,
        caloriesKcal: Int,
        distance: Int,
        time: Int,
        count: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_DAY_SPORT_TARGET
            setDaySportInfo(
                day_sport_info_t.newBuilder()
                    .setMStep(step)
                    .setMCaloriesKcal(caloriesKcal)
                    .setMDistanceM(distance)
                    .setMActimeSec(time)
                    .setMActCount(count)
                    .build()
            )
        }
    }

    /**
     * 同步实时步数信息
     * 应答 day_sport_info_t
     */
    fun sendCMD_SYNC_ACTUAL_STEP_INFO(): List<ByteArray> {
        return sendNoParameters(
            cmd_t.CMD_SYNC_ACTUAL_STEP_INFO
        )
    }

    /**
     * 同步电量信息
     * 应答 battery_info_t
     */
    fun sendCMD_SYNC_BATTERY_INFO(): List<ByteArray> {
        return sendNoParameters(
            cmd_t.CMD_SYNC_BATTERY_INFO
        )
    }

    /**
     * 手机控制手表响铃
     * @param enable 0不响铃 1响铃
     */
    fun sendCMD_RING_WATCH_CTRL_VALUE(enable: SwitchType): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_RING_WATCH_CTRL
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(enable.value)
                    .build()
            )
        }
    }

    /**
     * 手表控制手机响
     *  cmd_t.CMD_RING_PHONE_CTRL
     */

    /**
     * 手表控制手机拍照
     * cmd_t.CMD_CTRL_PHONE_TAKE_PICTURE
     */


    /**
     * 创建天气数据
     *  @param climate 气候类型
     *  @param temperature 当前气温
     *  @param temperatureMax 当前气温最高
     *  @param temperatureMin 当前气温最低
     *  @param pm pm2.5 两个字节表示
     *  @param aqi 空气指数 两个字节表示
     *  @param humidity 湿度
     *  @param uv 紫外线强度
     */
    fun createWeatherDay(
        climate: Climate,
        temperature: Int,
        temperatureMax: Int,
        temperatureMin: Int,
        pm: Int,
        aqi: Int,
        humidity: Int,
        uv: Int,
    ): weather_day_info_t {
        return weather_day_info_t.newBuilder()
            .setMClimate(climate.value)
            .setMTemperature(temperature)
            .setMTemperatureMax(temperatureMax)
            .setMTemperatureMin(temperatureMin)
            .setMPm25(pm)
            .setMAqi(aqi)
            .setMHumidity(humidity)
            .setMUv(uv)
            .build()
    }

    /**
     * 设置天气信息
     * @param year 年
     * @param month 月
     * @param day 日
     * @param city 城市名称
     * @param list 对应的天气信息列表
     */
    fun sendCMD_SET_WEATHER_INFO(
        year: Int,
        month: Int,
        day: Int,
        city: String,
        list: List<weather_day_info_t>,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_WEATHER_INFO
            setWeatherInfo(
                weather_info_t.newBuilder()
                    .setMYear(year)
                    .setMMonth(month)
                    .setMDay(day)
                    .setMCityName(city.toByteStringUtf8())
                    .apply {
                        list.forEachIndexed { index, weather ->
                            setMDaysInfo(
                                index,
                                weather
                            )
                        }
                    }

                    .build()
            )
        }
    }


    /**
     * 手表控制手机播放音乐指令
     */
    /* fun receive() {
         cmd_t.CMD_MUSIC_CTRL
         music_ctrl_info_t.newBuilder()
             .setMCode(0) //播放状态
             */
    /**
     * 0 播放
     * 1 暂停
     * 3 下一首
     * 4 上一首
     * 5 音量上调
     * 6 音量下调
     * 7 advance_repeat_mode
     * 8 advance_shuffle_mode
     * 9 skip_forward
     * 10 skip_backward
     * 11 like_track
     * 12 dislike_track
     * 13 book_mark_track
     * 14 reserved
     * 15 设置音量
     *//*
            .setMVolume(9)  //音量 0-100
            .build()
    }*/

    /**
     * 手机更新音乐播放信息
     */
    fun sendCMD_PHONE_MUSIC_INFO_UPDATE(
        state: MusicState,
        volume: Int,
        titleStr: String?,
        wordsStr: String?,
        duration: Int,
        elapsed: Int,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_PHONE_MUSIC_INFO_UPDATE
            setMusicInfo(
                music_info_t.newBuilder()
                    .setMCode(state.value)
                    .setMVolume(volume)
                    .setMTitle(titleStr.orEmpty().toByteStringUtf8())
                    .setMWords(wordsStr.orEmpty().toByteStringUtf8())
                    .setMDurationMs(duration)
                    .setMElapsedMs(elapsed)
                    .build()
            )
        }
    }


    /**
     * 设置设备模式
     * @param model 模式
     */
    fun sendCMD_SET_DEVICE_MODE(model: DeviceModel): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_DEVICE_MODE
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(model.value)
                    .build()
            )
        }
    }

    /**
     * 通话控制 fw -> app
     */
    /* fun sendCMD_PHONE_CALL_CTRL(call: PhoneCall): List<ByteArray>{
         return createBase {
             cmd = cmd_t.CMD_PHONE_CALL_CTRL
             setCtrlCode(
                 ctrl_code_t.newBuilder()
                     .setMCode(call.state)
                     .build()
             )
         }
     }*/


    /**
     * 获取菜单序列编码
     * @param type 1 一级左右滑动菜单
     * 应答 menu_sequence_info_t
     */
    fun sendCMD_GET_MENU_SEQUENCE_DATA(type: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_GET_MENU_SEQUENCE_DATA
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(type)
                    .build()
            )
        }
    }


    /**
     * 设置菜单序列编码
     * @param type 菜单类型 1 一级左右滑动菜单
     * @param data 菜单页面类型数组 {1，3，4，5}
     * @param supportCount 支持多少个菜单页面
     * @param support 支持的菜单序列 每个bit对应的页面 支持置1 否则为0
     */
    fun sendCMD_SET_MENU_SEQUENCE_DATA(
        type: Int,
        data: IntArray,
        supportCount: Int? = null,
        support: Int? = null,
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_MENU_SEQUENCE_DATA
            setMenuSequenceInfo(
                menu_sequence_info_t.newBuilder()
                    .setMType(type)
                    .setMData(data.toString().toByteStringUtf8())
                    .apply {
                        if (supportCount != null) {
                            mSupportCount = supportCount
                        }
                        if (support != null) {
                            mSupport = support
                        }
                    }
                    .build()
            )
        }
    }


    /**
     * 设置手机状态
     * @param state bit0  1前台运行 否则0
     */
    fun sendCMD_PHONE_APP_SET_STATUS(state: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_PHONE_APP_SET_STATUS
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(state)
                    .build()
            )
        }
    }

    /**
     * 获取log记录
     */
    fun sendCMD_GET_LOG_INFO_DATA(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_LOG_INFO_DATA)
    }


    /**
     * 获取表盘配置信息
     * 应答 dial_config_data_t
     */
    fun sendCMD_SYNC_DIAL_CONFIG_DATA(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_SYNC_DIAL_CONFIG_DATA)
    }


    /**
     * @param type 同步数据类型
     *
     * 应答 health_history_response_t
     */
    fun sendCMD_GET_HEALTH_DATA(
        year: Int,
        month: Int,
        day: Int,
        type: HealthDataType,
    ): List<ByteArray> {

        return createBase {
            cmd = cmd_t.CMD_GET_HEALTH_DATA
            setHistoryRequest(
                health_history_request_t.newBuilder()
                    .setMYear(year)
                    .setMMonth(month)
                    .setMDay(day)
                    .setMType(type.value)
                    .build()
            )
        }
    }

    /**
     * 判断多运动是否正在运行
     *
     * 应答 ctrl_code_t  code 1 运动正在运行  0 运动没有运行
     */
    fun sendCMD_GET_MUL_SPORT_IS_RUNNING(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_MUL_SPORT_IS_RUNNING)
    }


    /**
     * 多运动状态控制
     * @param mode 运动模式
     * @param status 运动状态
     * @param duration 运动时长 单位秒
     * @param heartRate 心率 可选
     * @param step 步数 可选
     * @param caloriesKcal 卡路里 可选
     * @param distance 距离 可选
     * @param speed10Mh 当前速度 单位10m/km 可选
     * @param avgSpeed10Mh 平均速度 可选
     * @param pacing 当前配速 单位 s/km 秒每公里 可选
     * @param avgPacing 平均配速 单位10m/km 可选
     * @param lat 纬度 可选
     * @param lng 经度 可选
     * 应答 mul_sport_ctrl_param_t 可选
     */
    fun sendCMD_SET_MUL_SPORT_STATUS(
        mode: SportMode,
        status: SportState,
        duration: Int,
        heartRate: Int? = null,
        step: Int? = null,
        caloriesKcal: Int? = null,
        distance: Int? = null,
        speed10Mh: Int? = null,
        avgSpeed10Mh: Int? = null,
        pacing: Int? = null,
        avgPacing: Int? = null,
        lat: Float? = null,
        lng: Float? = null
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_SET_MUL_SPORT_STATUS
            setMulSportParam(
                mul_sport_ctrl_param_t.newBuilder()
                    .setMMode(mode.value)
                    .setMStatus(status.value)
                    .setMDuration(duration)
                    .apply {
                        if (heartRate.isNotEmpty()){
                            mHrval = heartRate!!
                        }

                        if (step.isNotEmpty()){
                            mStep = step!!
                        }

                        if (caloriesKcal.isNotEmpty()){
                            mCaloriesCal = caloriesKcal!!
                        }

                        if (distance.isNotEmpty()){
                            mDistanceM = distance!!
                        }

                        if (speed10Mh.isNotEmpty()){
                            mSpeed10MH = speed10Mh!!
                        }

                        if (avgSpeed10Mh.isNotEmpty()){
                            mAvgSpeed10MH = avgSpeed10Mh!!
                        }

                        if (pacing.isNotEmpty()){
                            mSpaceSKm = pacing!!
                        }

                        if (avgPacing.isNotEmpty()){
                            mAvgSpaceSKm = avgPacing!!
                        }

                        if (lat.isNotEmpty()){
                            mLatitude = lat!!
                        }

                        if (lng.isNotEmpty()){
                            mLongitude = lng!!
                        }
                    }
                    .build()
            )
        }
    }


    /**
     * 获取多运动记录摘要
     * 应答 mul_sport_abstract_t
     */
    fun sendCMD_GET_MUL_SPORT_RECORD_ABSTRACT(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_GET_MUL_SPORT_RECORD_ABSTRACT)
    }


    /**
     * 获取多运动记录和日志数据
     * @param timeStamp 上一条指令获取的时间戳
     * 应答 mul_sport_record_t
     */
    fun sendCMD_GET_MUL_SPORT_RECORD_DATA(timeStamp: Int): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_GET_MUL_SPORT_RECORD_DATA
            setCtrlCode(
                ctrl_code_t.newBuilder()
                    .setMCode(timeStamp)
                    .build()
            )
        }
    }

    /**
     * 通告文件升级
     * @param fileID 文件ID
     * @param fileName 文件名称
     * @param fileSize 文件大小
     *
     */
    fun sendCMD_UPDATE_FILE_NOTIFY(
        fileID: Int,
        fileName:String,
        fileSize: Int
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_UPDATE_FILE_NOTIFY
            setFileInfo(
                file_info_t.newBuilder()
                    .setMFileId(fileID)
                    .setMFileSize(fileSize)
                    .setMFileName(fileName.toByteStringUtf8())
                    .build()
            )
        }
    }

    /**
     * 获取所有通信录内容
     * 应答 contacts_info_t
     */
    fun sendCMD_CONTACTS_GET(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_CONTACTS_GET)
    }

    /**
     * 设置指定通讯录内容
     */
    fun sendCMD_CONTACTS_SET(
        operate: ContactOperate,
        serials: Int,
        nameStr: String = "",
        numberStr: String = ""
    ): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_CONTACTS_SET
            setContactsInfoSet(
                contacts_info_set_t.newBuilder()
                    .setMOperate(operate.value)
                    .setMSerials(serials)
                    .setMName(nameStr.toByteStringUtf8())
                    .setMNumber(numberStr.toByteStringUtf8())
                    .build()
            )
        }
    }


    /**
     * 创建通讯录数据实体类
     */
    fun createContactsInfo(
        nameStr: String,
        numberStr: String
    ): contacts_info_t {
        return contacts_info_t.newBuilder()
            .setMName(nameStr.toByteStringUtf8())
            .setMNumber(numberStr.toByteStringUtf8())
            .build()
    }

    /**
     * 设置全部通讯录内容
     */
    fun sendCMD_CONTACTS_SYNC(list: List<contacts_info_t>): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_CONTACTS_SYNC
            setAllContactsInfo(
                contacts_all_info_t.newBuilder()
                    .apply {
                        list.forEach { contactsInfoT ->
                            addMContacts(contactsInfoT)
                        }
                    }
                    .build()
            )
        }
    }






  /*  fun send(): List<ByteArray> {
        return createBase {
            cmd = cmd_t.CMD_
        }
    }

    fun sendNot(): List<ByteArray> {
        return sendNoParameters(cmd_t.CMD_)
    }*/


    /**
     * 发送无参的数据包
     */
    private fun sendNoParameters(cmdT: cmd_t): List<ByteArray> {
        return createBase { setCmd(cmdT) }
    }

    /**
     * 构建最基本的 watch_cmds 对象数据
     */
    private inline fun createBase(onParams: Builder.() -> Builder): List<ByteArray> {
        return createWatchCommand(onParams).createBytes(head, mMtuSize, dataFixedLength)
    }

    fun sendAckReply(code: Byte): ByteArray {
        return ByteBuffer.allocate(4).apply {
            order(ByteOrder.LITTLE_ENDIAN)
            putShort(Ble.BLE_HEADER_ACK.toUShort(16).toShort())
            put(code)
            put(code)
        }.array()
    }


    /**
     * 接收数据包
     */
    fun receive(bytes: ByteArray, onAnalysis: (watch_cmds) -> Unit) {
        //转换成小端
        val buffer = ByteBuffer.wrap(bytes).apply {
            order(ByteOrder.LITTLE_ENDIAN)
        }
        //包头
        val tempHeader = buffer.short.toUShort().toString(16)
        if (!Ble.BLE_HEADER.equals(tempHeader, true)) {
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
        if (length == tempBytes.size.toUShort()) {
            //crc校验通过
            val watch = bytes2WatchCmd(tempBytes, crc)
            watch?.let {
                onAnalysis.invoke(it)
            }
        } else {
            //走到这里说明当前数据是分包了的
            mCacheBytes.addAll(tempBytes.toList())
            val tempLength = mCacheBytes.size.toUShort()
            if (length == tempLength) {
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


    fun bytes2WatchCmd(bytes: ByteArray, crc: UShort): watch_cmds? {
        //计算一下crc
        val tempCrc = bytes.crcJW002()
        //crc校验通过
        return if (tempCrc == crc) {
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



