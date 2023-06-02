package com.artillery.rwutils.model

import androidx.annotation.Keep
import com.artillery.rwutils.type.SwitchType


@Keep
open class AReplyData

/**
 * 实时数据
 */
@Keep
data class RealTimeData(
    var step: Int,   //总步数
    var calorie: Int,  //卡路里
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
    var steps: Int,  //步数 单位步
    var calorie: Int, //卡路里 单位大卡
    var mileage: Int,  //里程,单位米
    var sportTime: Int  //运动时长,单位分钟
): AReplyData()


@Keep
data class SleepInfoData(
    var order: Int,  //序号
    var timeStamp: Int,  //时间戳 单位秒
    var sleepDuration: Int, //单位分钟
    var sleepType: Int //1深睡 2浅睡 3醒着
): AReplyData()

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
    var timeStamp: Int, //时间戳 单位秒 最后一包表示当天的0点的时间戳
    var heartRateValue: Int = 0,  //心率值 如果当前的值为0xffff 表示当前获取的是最后一包
    var maxBloodPressure: Int = 0,  //最大血压
    var minBloodPressure: Int = 0,
    var bloodOxygen: Int = 0 , //血氧
): AReplyData()

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
    var heartRateValue: Int,  //心率数据
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


/**
 * 同步联系人结果数据返回
 */
@Keep
data class ReplyContactsItem(
    var order: Int,
    var flag: Int,
    var state: Int, //0 为失败 1 成功
    var number: Int, //已经传输多少条
)

/**
 * 手环屏幕规格
 */
@Keep
data class ReplyClockDialItem(
    var width: Int,  //宽高
    var height: Int,
    var supportDigitalMeter: Int,  //是否支持数字表盘
    var supportAnalogDial: Int,  //是否支持模拟表盘
    var pictureFormat: Int,  //背景图片数据格式 0 预留 (默认 16bit RGB 数据)
    var dialProtocolType: Int, //支持的表盘协议类型
    var binFileMaxSize: Int,  //Bin 文件字节数最大值,单位 byte
    var watchShape: Int,  //手表形状: 0 方,1 圆,2 其他
    var customWallpaperLocation: Int //1 支持自定义壁纸位置等，0 不支持
)

/**
 * 背景图片发送回复
 */
@Keep
data class ReplyBgItem(
    var order: Int,  //如果等于0xfffe 表示是发送预备动作回复的
    var state: Int //成功或者失败 0失败 1成功
)

@Keep
data class ReplyBinItem(
    var order: Int,  //如果等于0xfffffe 表示是发送预备动作回复的 否则就是序号
    var state: Int //成功或者失败 0失败 1成功
)

/**
 * 读取历史体温返回
 */
@Keep
data class ReplyTemperatureItem(
    var timeStamp: Int,   // 时间戳
    var temperature: String,  //体温  36.5
    var year: Int = 0,  //年月日
    var month: Int = 0,
    var day: Int = 0
): AReplyData()


/**
 * 实时更新当前正启动的运动模式数据
 */
@Keep
data class ReplySportItem(
    var sportType:SportType,  //运动类型
    var startSportTime: Int, //运动开始时间
    var sportDuration: Int,//运动已持续时间 单位秒
    var calorie: Int,  //卡路里
    var steps: Int,   //步数
    var heartRate: Int,  //心率
    var sportStatus: Int,  //0 没有运动，1 正在运动 ，
    var order: Int = 0   //同步历史记录的数据有序号
    // 2 开始运动，3 结束 运动且需要保存轨迹 ，
    // 4 结束运动不许保存轨 迹, 有可能等于-1 表示是获取运动历史记录过来的数据
)

sealed class SportType(val value: Int){
    class 健走(value: Int = 1): SportType(value)
    class 跑步(value: Int = 2): SportType(value)
    class 游泳(value: Int = 3): SportType(value)
    class 骑车(value: Int = 4): SportType(value)
    class 瑜伽(value: Int = 5): SportType(value)
    class 登山(value: Int = 6): SportType(value)
    class 篮球(value: Int = 7): SportType(value)
    class 足球(value: Int = 8): SportType(value)
    class 羽毛球(value: Int = 9): SportType(value)
    class 网球(value: Int = 10): SportType(value)
    class 跳绳(value: Int = 11): SportType(value)
    class 跑步机(value: Int = 12): SportType(value)
    class 椭圆机(value: Int = 13): SportType(value)
    class 乒乓球(value: Int = 14): SportType(value)

    companion object{
        fun of(value: Int): SportType{
            return when(value){
                1 -> 健走()
                2 -> 跑步()
                3 -> 游泳()
                4 -> 骑车()
                5 -> 瑜伽()
                6 -> 登山()
                7 -> 篮球()
                8 -> 足球()
                9 -> 羽毛球()
                10 -> 网球()
                11 -> 跳绳()
                12 -> 跑步机()
                13 -> 椭圆机()
                14 -> 乒乓球()
                else -> 跑步()
            }
        }
    }
}












