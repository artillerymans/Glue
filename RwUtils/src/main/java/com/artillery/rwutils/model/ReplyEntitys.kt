package com.artillery.rwutils.model

import androidx.annotation.Keep
import com.artillery.rwutils.type.SwitchType

/**
 * 实时数据
 */
@Keep
data class RealTimeData(
    var step: Int,   //总步数
    var calorie: Short,  //卡路里
    var totalMileage: Int,  //总里程 单位米
    var activeTime: Int   //活动时长 单位秒
)


/**
 * 当天步数信息
 */
@Keep
data class CurrentDayStepsData(
    val year: Int,
    val month: Int,
    val day: Int,
    val hour: Int,  //如果等于0xff则表示是当日获取的最后一包数据
    var steps: Int,
    var calorie: Short,
    var mileage: Int,
    var sportTime: Short
)


@Keep
data class SleepInfoData(
    var order: Short,
    var timeStamp: Int,
    var sleepDuration: Int, //单位分钟 已经乘以5了
    var sleepType: Int //1深睡 2浅水 3醒着
)

@Keep
data class SoftVersionModel(
    var versionDes: String,  //编译日期(Sep 12,2017)
    var versionCode: String  //版本号,例如 1.0 (data[12] = 1,data[13] = 0)
)


/**
 * 血压 血氧 心率等
 */
@Keep
data class ThreeElements(
    var timeStamp: Int, //单位秒
    var heartRateValue: Int,
    var maxBloodPressure: Int,  //最大血压
    var minBloodPressure: Int,
    var bloodOxygen: Int //血氧
)

/**
 * 开启实时测量的
 */
sealed class MeasureType(var enable: SwitchType) {
    class HeartRate(enable: SwitchType) : MeasureType(enable)
    class BloodPressure(enable: SwitchType) : MeasureType(enable)
    class BloodOxygen(enable: SwitchType) : MeasureType(enable)
    class All(enable: SwitchType) : MeasureType(enable)

    companion object {
        fun of(value: Int): MeasureType {
            return when (value) {
                0x00 -> HeartRate(SwitchType.OFF)
                0x01 -> BloodPressure(SwitchType.OFF)
                0x02 -> BloodOxygen(SwitchType.OFF)
                else -> All(SwitchType.OFF)
            }
        }
    }
}


/**
 * 实时心率血压血氧
 */
@Keep
data class ThreeElementsRealTime(
    var heartRateValue: Int,
    var heartRateAverage: Int, //心率平均
    var maxHeartRateValue: Int,  //最大心率
    var minHeartRateValue: Int,
    var maxBloodPressure: Int,  //最大血压
    var minBloodPressure: Int,
    var bloodOxygen: Int //血氧
)

/**
 * 当前的心率血压血氧
 */
@Keep
data class ThreeElementsCurrent(
    var heartRateValue: Int,
    var maxBloodPressure: Int,  //最大血压
    var minBloodPressure: Int,
    var bloodOxygen: Int, //血氧
    var bloodSugar: String //血糖
)

sealed class Aggregate{
    /**
     * 读取到的通知开关
     */
    @Keep
    data class NoticeAggregate(
        val skype: NoticeType.Skype,
        val line: NoticeType.Line,
        val inCall: NoticeType.InCall,
        val sms: NoticeType.Sms,
        val wx: NoticeType.WeiXin,
        val qq: NoticeType.QQ,
        val kakaoTalk: NoticeType.KakaoTalk,
        val facebook: NoticeType.Facebook,
        val twitter: NoticeType.Twitter,
        val whatsApp: NoticeType.WhatsApp,
        val linkedin: NoticeType.Linkedin,
        val viber: NoticeType.Viber,
        val instagram: NoticeType.Instagram,
        val messenger: NoticeType.Messenger,
        val other: NoticeType.Other,
        val lightUpTheScreen: SwitchType,  //抬腕亮屏
        val whileHeartRates: SwitchType,  //心率循环检测
        val whileHeartRatesInterval: Int  //心率循环检测间隔 单位分钟
    ): Aggregate()


    /**
     * 读取到的闹钟列表
     */
    data class AlarmAggregate(
        val alarmClocks: List<AlarmClock>
    ): Aggregate()


    data class SddAggregate(
        val sdds: List<SDD>
    ): Aggregate()


    /**
     * 设置表盘
     */
    data class ClockDialUnitAggregate(
        val clockDialOrder: Int,
        val sum: Int,
        val temperatureUnit: TemperatureUnit,
        val distanceUnit: DistanceUnit
    ): Aggregate()





}


sealed class SDD(var enable: SwitchType,
                 var startHour: Byte,   //开始结束时分
                 var startMinute: Byte,
                 var endHour: Byte,
                 var endMinute: Byte){
    /**
     * 久坐提醒
     */
    @Keep
    class Sedentary(
        enable: SwitchType,
        var interval: Byte,  //提醒间隔 单位分钟 勿扰模式没有间隔需要给0
        startHour: Byte,   //开始结束时分
        startMinute: Byte,
        endHour: Byte,
        endMinute: Byte,
    ): SDD(enable, startHour, startMinute, endHour, endMinute)

    /**
     * 勿扰模式
     */
    @Keep
   class DonTDisturb(
        enable: SwitchType,
        startHour: Byte,   //开始结束时分
        startMinute: Byte,
        endHour: Byte,
        endMinute: Byte,
    ): SDD(enable, startHour, startMinute, endHour, endMinute)

    /**
     * 喝水提醒
     */
    @Keep
    class DrinkingWater(
        enable: SwitchType,
        var interval: Byte,  //提醒间隔 单位分钟
        startHour: Byte,   //开始结束时分
        startMinute: Byte,
        endHour: Byte,
        endMinute: Byte,
    ): SDD(enable, startHour, startMinute, endHour, endMinute)
}












