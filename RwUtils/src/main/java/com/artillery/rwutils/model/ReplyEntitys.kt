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
sealed class MeasureType(var enable: SwitchType){
    class HeartRate(enable: SwitchType): MeasureType(enable)
    class BloodPressure(enable: SwitchType): MeasureType(enable)
    class BloodOxygen(enable: SwitchType): MeasureType(enable)
    class All(enable: SwitchType): MeasureType(enable)

    companion object {
        fun of(value: Int): MeasureType{
            return when(value){
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








