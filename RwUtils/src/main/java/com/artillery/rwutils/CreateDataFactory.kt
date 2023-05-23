package com.artillery.rwutils

import android.text.format.DateFormat
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.fillZeros
import com.artillery.rwutils.exts.toByte
import com.artillery.rwutils.model.AlarmClock
import com.artillery.rwutils.model.BWeather
import com.artillery.rwutils.model.ContactsItem
import com.artillery.rwutils.model.DistanceUnit
import com.artillery.rwutils.model.NoticeType
import com.artillery.rwutils.model.ProcessDataRequest
import com.artillery.rwutils.model.RemindItem
import com.artillery.rwutils.model.TemperatureUnit
import com.artillery.rwutils.type.DayFormatType
import com.artillery.rwutils.type.Gender
import com.artillery.rwutils.type.SwitchType
import com.artillery.rwutils.type.SystemType
import com.artillery.rwutils.type.ZhType
import com.artillery.rwutils.type.getLanguageType
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.Utils
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.experimental.or

/**
 * @author : zhiweizhu
 * create on: 2023/5/22 下午3:55
 */
object CreateDataFactory {

    /**
     * 设置日期时间
     */
    fun createDateTime(): ByteBuffer {
        val calendar = Calendar.getInstance()
        //UTC时间戳
        val timeInSecond = calendar.timeInMillis / 1000
        //时区
        val timeZone: TimeZone = calendar.timeZone
        val offset: Int = timeZone.rawOffset / 1000 / 60 / 60
        val isHour24 = DateFormat.is24HourFormat(Utils.getApp())
        val language = LanguageUtils.getAppContextLanguage()
        return createDateTime(
            timeInSecond.toInt(),
            offset,
            (if (isHour24) DayFormatType.HOUR_24.ordinal else DayFormatType.HOUR_12.ordinal).toByte(),
            language.getLanguageType().ordinal.toByte(),
            (if (language.language.equals(Locale.SIMPLIFIED_CHINESE.language)) ZhType.YES_ZH.ordinal else ZhType.NO_ZH.ordinal).toByte(),
            SystemType.ANDROID.ordinal.toByte()
        )
    }

    /**
     * 设置日期时间
     */
    fun createDateTime(
        timeInSecond: Int,
        timeZone: Int,
        hour12: Byte,
        otherLanguage: Byte,
        isChinese: Byte,
        os: Byte,
        cmd: Byte = BleConstantData.CMD_SET_DATE_TIME.toByte(),
    ): ByteBuffer {
        return ByteBuffer.allocate(20).apply {
            order(ByteOrder.BIG_ENDIAN)
            //命令
            put(cmd)
            //UTC时间戳到秒
            putInt(timeInSecond)
            //时区
            putInt(timeZone * 60 * 60)
            //时间格式
            put(hour12)
            //其他语言
            put(otherLanguage)
            //是否是中文
            put(isChinese)
            //操作系统
            put(os)
        }
    }


    /**
     * 获取手环功能列表
     */
    fun createWatchFunctionList(): ByteBuffer {
        return ByteBuffer.allocate(2).apply {
            put(BleConstantData.HEAD_0X03)
            put(BleConstantData.RESERVED_DEF_0X00)
        }
    }

    /**
     * 设备设置信息
     */
    fun createSettingUserInfo(
        weight: Short,
        age: Byte,
        height: Byte,
        stepLength: Byte,
        gender: Gender,  //0x00:女 0x01:男
        targetStep: Int,
    ): ByteBuffer {
        return ByteBuffer.allocate(20).apply {
            put(BleConstantData.CMD_SET_USER_INFO.toByte())
            put(0x01)
            putShort(weight)
            put(age)
            put(height)
            put(stepLength)
            put(if (gender == Gender.Woman) 0x00 else 0x01)
            putInt(targetStep)
        }
    }

    /**
     * 设置通知开关
     * @param heartRateInterval 单位分钟
     */
    fun createSettingAppNoticeEnables(
        skypeEnable: SwitchType = SwitchType.OFF,
        lineEnable: SwitchType = SwitchType.OFF,
        incomingCallEnable: SwitchType = SwitchType.OFF,
        smsNoticeEnable: SwitchType = SwitchType.OFF,
        wxNoticeEnable: SwitchType = SwitchType.OFF,
        qqNoticeEnable: SwitchType = SwitchType.OFF,
        kakaoTalkEnable: SwitchType = SwitchType.OFF,
        facebookEnable: SwitchType = SwitchType.OFF,
        twitterEnable: SwitchType = SwitchType.OFF,
        whatsappEnable: SwitchType = SwitchType.OFF,
        linkedinEnable: SwitchType = SwitchType.OFF,
        viberEnable: SwitchType = SwitchType.OFF,
        instagramEnable: SwitchType = SwitchType.OFF,
        messengerEnable: SwitchType = SwitchType.OFF,
        otherAppEnable: SwitchType = SwitchType.OFF,
        liftScreenLightEnable: SwitchType = SwitchType.OFF,
        heartRateWhileEnable: SwitchType = SwitchType.OFF,
        heartRateInterval: Byte = 5,
    ): ByteBuffer {
        return ByteBuffer.allocate(20).apply {
            put(BleConstantData.CMD_SET_USER_INFO.toByte())
            put(0x02)
            put(skypeEnable.toByte())
            put(lineEnable.toByte())
            put(incomingCallEnable.toByte())
            put(smsNoticeEnable.toByte())
            put(wxNoticeEnable.toByte())
            put(qqNoticeEnable.toByte())
            put(kakaoTalkEnable.toByte())
            put(facebookEnable.toByte())
            put(twitterEnable.toByte())
            put(whatsappEnable.toByte())
            put(linkedinEnable.toByte())
            put(viberEnable.toByte())
            put(instagramEnable.toByte())
            put(messengerEnable.toByte())
            put(otherAppEnable.toByte())
            put(liftScreenLightEnable.toByte())
            put(heartRateWhileEnable.toByte())
            put(heartRateInterval)
        }
    }


    /**
     * 设置闹钟 最大设置数量3个
     */
    fun createSettingsAlarmClock(
        list: MutableList<AlarmClock>,
    ): ByteBuffer {
        return ByteBuffer.allocate(20).apply {
            put(BleConstantData.CMD_SET_USER_INFO.toByte())
            put(0x03)
            list.forEach { item ->
                put(item.enable.toByte())
                putShort(item.startTime)
                put(item.choiceDays.fold(0.toByte()) { acc, day -> acc or day })
            }
        }
    }

    /**
     * 设置久坐提醒,勿扰模式,喝水提醒
     * 按照久坐、勿扰、喝水等顺序依次设置
     */
    fun createSettingRemind(
        list: List<RemindItem>,
    ): ByteBuffer {
        return ByteBuffer.allocate(20).apply {
            put(BleConstantData.CMD_SET_USER_INFO.toByte())
            put(0x04)
            list.forEach { item ->
                put(item.enable.toByte())
                put(item.interval)
                put(item.startHour)
                put(item.startMinute)
                put(item.endHour)
                put(item.endMinute)
            }
        }
    }


    /**
     * 查找手环
     */
    fun createFindWatchDevice(
        enable: SwitchType,
    ): ByteBuffer {
        return ByteBuffer.allocate(2).apply {
            put(BleConstantData.CMD_FIND_WATCH_DEVICE)
            put(enable.toByte())
        }
    }


    /**
     * 蓝牙拍照 手机通知蓝牙进入、退出拍照
     */
    fun createTakePhoto(enable: SwitchType): ByteBuffer {
        return ByteBuffer.allocate(2).apply {
            put(BleConstantData.CMD_TAKE_PHOTO)
            put(enable.toByte())
        }
    }

    /**
     * 蓝牙开始拍照
     */
    fun createStartTakePhoto(): ByteBuffer {
        return ByteBuffer.allocate(2).apply {
            put(BleConstantData.CMD_START_TAKE_PHOTO.toByte())
            put(0x01)
        }
    }


    /**
     * 获取电池电量
     */
    fun createBatteryLevel(): ByteBuffer {
        return ByteBuffer.allocate(1).apply {
            put(BleConstantData.CMD_BATTER_LEVEL)
        }
    }

    /**
     * 获取软件版本
     */
    fun createSoftVersion(): ByteBuffer {
        return ByteBuffer.allocate(1).apply {
            put(BleConstantData.CMD_SOFT_VERSION)
        }
    }

    /**
     * 获取设备运动数据
     * 步数里程运动时长
     */
    fun createStepsByTime(
        data: ProcessDataRequest.Steps,
    ): ByteBuffer {
        return createDataByTime(data)
    }

    /**
     * 睡眠数据
     */
    fun createSleepsByTime(
        data: ProcessDataRequest.Sleeps,
    ): ByteBuffer {
        return createDataByTime(data)
    }

    /**
     * 获取心率 血氧 血压
     */
    fun createHeartRateByTime(
        data: ProcessDataRequest.HeartRates,
    ): ByteBuffer {
        return createDataByTime(data)
    }

    /**
     * 运动数据获取
     */
    private fun createDataByTime(
        data: ProcessDataRequest,
    ): ByteBuffer {
        return ByteBuffer.allocate(4).apply {
            put(data.cmd)
            put(data.toYearByte())
            put(data.month.toByte())
            put(data.day.toByte())
        }
    }

    /**
     * 心率实时测试和测试时间
     *
     * @param byte 0x00 心率测量
     * @param byte 0x01 血压测量
     * @param byte 0x02 血氧测量
     * @param byte 0x03 All
     */
    fun createSwitchListen(
        enable: SwitchType,
        byte: Byte,
    ): ByteBuffer {
        return ByteBuffer.allocate(3).apply {
            put(BleConstantData.CMD_SWITCH_HEART_LISTEN)
            put(byte)
            put(enable.toByte())
        }
    }


    /**
     * 恢复出厂设置
     */
    fun createResetFactorySetting(): ByteBuffer {
        return ByteBuffer.allocate(4).apply {
            put(0x71)
            put(0x01)
            put(0x02)
            put(0x03)
        }
    }

    /**
     * 创建推送消息
     */
    fun createMessagePush(text: String, type: NoticeType, cmd: Byte = 0x73): List<ByteBuffer> {
        return mutableListOf<ByteBuffer>().apply {
            val content = text.toByteArray()
            var packet: ByteBuffer
            var offset = 0
            while (offset < content.size) {
                val remaining = content.size - offset
                val length = if (remaining > 17) 17 else remaining
                packet = ByteBuffer.allocate(length + 3)
                packet.put(cmd)  //命令码
                packet.put((size + 1).toByte()) //序列号从1开始
                packet.put(type.value.toByte())  //通知的类型
                packet.put(content, offset, length)
                packet.flip()
                add(packet)
                offset += length
            }
        }
    }


    /**
     * 获取当前手环心率
     */
    fun createCurrentHeartRate(
        cmd: Byte = 0x17,
    ): ByteBuffer {
        return ByteBuffer.allocate(4).apply {
            put(cmd)
            put(0)
            put(0)
            put(0)
        }
    }


    /**
     * 天气数据
     */
    fun createWeather(
        list: List<BWeather>,   //当天天气在第0位
        cmd: Byte = 0x05,
    ): ByteBuffer {
        return ByteBuffer.allocate(20).apply {
            put(cmd)
            list.forEach { bWeather ->
                //天气类型
                put(bWeather.type.value.toByte())
                if (bWeather is BWeather.TodayWeather) {
                    //当前温度正负
                    put(bWeather.currentTemperatureType.value.toByte())
                    //当前温度
                    put(bWeather.currentTemperature.toByte())
                }
                //最低温度
                put(bWeather.minType.value.toByte())
                put(bWeather.minTemperatureValue.toByte())
                //最高温度
                put(bWeather.maxType.value.toByte())
                put(bWeather.maxTemperatureValue.toByte())
            }
        }
    }


    /**
     * 紫外线 大气压
     */
    fun createUltravioletRaysAndPressure(
        ultravioletRays: Byte,   //75代表7.5  紫外线
        pressure: Short, //大气压  不带小数点
        humidity: Byte, //湿度
        cmd: Byte = 0x04,
    ): ByteBuffer {
        return ByteBuffer.allocate(5).apply {
            put(cmd)
            put(ultravioletRays)
            putShort(pressure)
            put(humidity)
        }
    }


    /**
     * 读取各种开关 提醒 抬腕亮屏 心率检测开关
     */
    fun createReadNoticeEnables(): ByteBuffer{
        return createRead(0x02)
    }

    /**
     * 读取闹钟
     */
    fun createReadAlarms(): ByteBuffer{
        return createRead(0x03)
    }

    /**
     * 读取久坐提醒 勿扰模式 喝水提醒
     */
    fun createReadNoticeSettings():ByteBuffer{
        return createRead(0x04)
    }


    /**
     * 读取表盘 温度单位 距离单位
     * 华摄氏 英制 公制
     */
    fun createReadClockDialUnit(): ByteBuffer {
        return createRead(0x05)
    }

    private fun createRead(
        serialNumber: Int,
        cmd: Byte = 0x62
    ): ByteBuffer {
        return ByteBuffer.allocate(2).apply {
            put(cmd)
            put(serialNumber.toByte())
        }
    }

    /**
     * 设置表盘 摄氏度 华摄氏 英制公制等
     */
    fun createSetClockCompany(
        clockNumber: Int = 0,  //表盘序号0-15
        temperatureUnit: TemperatureUnit,
        distanceUnit: DistanceUnit,
        serialNumber: Int = 2,
        cmd: Byte = 0x02
    ): ByteBuffer{
        return ByteBuffer.allocate(5).apply {
            put(cmd)
            put(serialNumber.toByte())
            put(clockNumber.toByte())
            put(temperatureUnit.value.toByte())
            put(distanceUnit.value.toByte())
        }
    }


    /**
     * 同步联系人
     */
    fun createSyncContacts(
        list: List<ContactsItem>,
        cmd: Byte = 0x66
    ): List<ByteBuffer>{
        return mutableListOf<ByteBuffer>().apply {
            list.forEachIndexed { index, item ->
                //最大值为1000
                val tempIndex = index.toShort()
                add(createByteBuffer(tempIndex, 0, item.name, cmd))
                add(createByteBuffer(tempIndex, 1, item.mobile, cmd))
            }
        }
    }

    private fun createByteBuffer(serialNumber: Short, type: Int, text: String, cmd: Byte):ByteBuffer{
        return ByteBuffer.allocate(20).apply {
            put(cmd)
            putShort(serialNumber)
            put(type.toByte())  //0 表示写入的是姓名 1 表示写入的是电话号码
            put(0)  //预留的 默认0
            val dataBytes = text.toByteArray()
            if (limit() >= dataBytes.size){
                put(dataBytes)
                //如果后续还有空间对进行补0
                if (hasRemaining()){
                    fillZeros()
                }
            }else {
                put(dataBytes.toMutableList().subList(0, limit()).toByteArray())
            }
            flip()
        }
    }


    /**
     * 获取屏幕规格
     */
    fun createScreenSpecifications(cmd: Byte = 0x67): ByteBuffer {
        return ByteBuffer.allocate(1).apply {
            put(cmd)
        }
    }












}
