package com.artillery.rwutils.model

import androidx.annotation.Keep
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.type.SwitchType


/**
 *
 * 闹钟提醒
 */
@Keep
data class AlarmClock(
    var enable: SwitchType,
    var startTime: Short,  //高位在前
    var choiceDays: MutableList<Byte>,
)


/**
 * 久坐提醒
 * 勿扰模式
 * 喝水提醒
 */
@Keep
data class RemindItem(
    var enable: SwitchType,
    var interval: Byte,  //提醒间隔
    var startHour: Byte,   //开始结束时分
    var startMinute: Byte,
    var endHour: Byte,
    var endMinute: Byte,
)


/**
 * 请求运动数据 步数 睡眠 心率血压 血氧
 */
sealed class ProcessDataRequest(val cmd: Byte, val year: Int, val month: Int, val day: Int) {

    class Steps(year: Int, month: Int, day: Int, cmd: Byte = BleConstantData.CMD_0x13) :
        ProcessDataRequest(cmd, year, month, day)

    class Sleeps(year: Int, month: Int, day: Int, cmd: Byte = BleConstantData.CMD_0x15) :
        ProcessDataRequest(cmd, year, month, day)

    class HeartRates(
        year: Int,
        month: Int,
        day: Int,
        cmd: Byte = BleConstantData.CMD_0x16,
    ) : ProcessDataRequest(cmd, year, month, day)

    class Blood(
        year: Int,
        month: Int,
        day: Int,
        cmd: Byte = BleConstantData.CMD_0x18,
    ): ProcessDataRequest(cmd, year, month, day)

    fun toYearByte(): Byte {
        return if (year >= 2000) {
            (year - 2000).toByte()
        } else {
            year.toByte()
        }
    }
}

sealed class BloodTimeType(val value: Int){
    class Random(value: Int = 0): BloodTimeType(value)
    class Emptiness(value: Int = 1): BloodTimeType(value)
    class AfterBreakfast(value: Int = 2): BloodTimeType(value)
    class BeforeLunch(value: Int = 3): BloodTimeType(value)
    class AfterLunch(value: Int = 4): BloodTimeType(value)
    class BeforeDinner(value: Int = 5): BloodTimeType(value)
    class AfterDinner(value: Int = 6): BloodTimeType(value)
    class BeforeBed(value: Int = 7): BloodTimeType(value)
}


/**
 * 消息推送类型
 */
sealed class NoticeType(val value: Int){
    class InCall(value: Int = 0): NoticeType(value)
    class Sms(value: Int = 1): NoticeType(value)
    class WeiXin(value: Int = 2): NoticeType(value)
    class QQ(value: Int = 3): NoticeType(value)
    class QingNiu(value: Int = 4): NoticeType(value)
    class Facebook(value: Int = 5): NoticeType(value)
    class Twitter(value: Int = 6): NoticeType(value)
    class WhatsApp(value: Int = 7): NoticeType(value)
    class Linkedin(value: Int = 8): NoticeType(value)
    class Skype(value: Int = 9): NoticeType(value)
    class Line(value: Int = 10): NoticeType(value)
    class KakaoTalk(value: Int = 11): NoticeType(value)
}


/**
 * 天气类型
 */
sealed class WeatherType(val value: Int){
    class 阴(value: Int = 0): WeatherType(value)
    class 晴(value: Int = 1): WeatherType(value)
    class 多云(value: Int = 2): WeatherType(value)
    class 阵雨(value: Int = 3): WeatherType(value)
    class 雷阵雨(value: Int = 4): WeatherType(value)
    class 雷雨(value: Int = 5): WeatherType(value)
    class 大雨(value: Int = 6): WeatherType(value)
    class 中雨(value: Int = 7): WeatherType(value)
    class 小雨(value: Int = 8): WeatherType(value)
    class 大雪(value: Int = 9): WeatherType(value)
    class 中雪(value: Int = 10): WeatherType(value)
    class 小雪(value: Int = 11): WeatherType(value)
    class 雾(value: Int = 12): WeatherType(value)
    class 霾(value: Int = 13): WeatherType(value)
}



/**
 * 温度零上或者零下
 */
sealed class TemperatureType(val value: Int){
    class AboveZero(value: Int = 0): TemperatureType(value)
    class BelowZero(value: Int = 1): TemperatureType(value)
}


/**
 * 天气类型
 */
sealed class BWeather(val type: WeatherType,
                      val minType: TemperatureType,
                      val minTemperatureValue: Int,
                      val maxType: TemperatureType,
                      val maxTemperatureValue: Int
){

    class TodayWeather(
        type: WeatherType,  //天气类型
        val currentTemperatureType: TemperatureType,   //当前温度正负
        val currentTemperature: Int,   //当前温度
        minType: TemperatureType,  //最低温度正负
        minTemperatureValue: Int,
        maxType: TemperatureType,  //最高温度正负
        maxTemperatureValue: Int
    ): BWeather(type, minType, minTemperatureValue, maxType, maxTemperatureValue)

    class OtherDayWeather(
        type: WeatherType,
        minType: TemperatureType,
        minTemperatureValue: Int,
        maxType: TemperatureType,
        maxTemperatureValue: Int
    ): BWeather(type, minType, minTemperatureValue, maxType, maxTemperatureValue)

}


sealed class TemperatureUnit(val value: Int){
    /*摄氏度*/
    class Centigrade(value: Int = 0): TemperatureUnit(value)
    /*华氏度*/
    class HuaCelsius(value: Int = 1): TemperatureUnit(value)
}

sealed class DistanceUnit(val value: Int){
    /*公制*/
    class MetricSystem(value: Int = 0): DistanceUnit(value)
    /*英制*/
    class EnglishSystem(value: Int = 1): DistanceUnit(value)
}


/**
 * 联系人
 */
data class ContactsItem(
    var name: String,
    var mobile: String
)


data class BleResult<T>(
    val cmd:Int = -1,
    var code: Int = -1,
    var data: T? = null
){
    fun isSuccess():Boolean {
        return code == BleConstantData.SUCCESS_BLE_CODE
    }
}
