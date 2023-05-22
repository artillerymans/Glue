package com.artillery.rwutils

import android.text.format.DateFormat
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.toByte
import com.artillery.rwutils.model.AlarmClock
import com.artillery.rwutils.model.RemindItem
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
object FactoryUtils {

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
    ): ByteBuffer{
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
    fun createWatchFunctionList(): ByteBuffer{
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
        targetStep: Int
    ): ByteBuffer{
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
        heartRateInterval: Byte = 5
    ): ByteBuffer{
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
        list: MutableList<AlarmClock>
    ): ByteBuffer{
        return ByteBuffer.allocate(20).apply {
            put(BleConstantData.CMD_SET_USER_INFO.toByte())
            put(0x03)
            list.forEach { item ->
                put(item.enable.toByte())
                putShort(item.startTime)
                put(item.choiceDays.fold(0.toByte()) { acc, day  -> acc or day})
            }
        }
    }

    /**
     * 设置久坐提醒,勿扰模式,喝水提醒
     * 按照久坐、勿扰、喝水等顺序依次设置
     */
    fun createSettingRemind(
        list: List<RemindItem>
    ): ByteBuffer{
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














}