package com.artillery.rwutils.model

import androidx.annotation.Keep

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

@Keep
data class SoftVersionModel(
    var versionDes: String,  //编译日期(Sep 12,2017)
    var versionCode: String  //版本号,例如 1.0 (data[12] = 1,data[13] = 0)
)


