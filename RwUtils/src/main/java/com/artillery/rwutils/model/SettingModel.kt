package com.artillery.rwutils.model

import com.artillery.rwutils.type.SwitchType


/**
 * 闹钟提醒
 */
data class AlarmClock(
    var enable: SwitchType,
    var startTime: Short,  //高位在前
    var choiceDays: MutableList<Byte>
)


/**
 * 久坐提醒
 * 勿扰模式
 * 喝水提醒
 */
data class RemindItem(
    var enable: SwitchType,
    var interval: Byte,  //提醒间隔
    var startHour: Byte,   //开始结束时分
    var startMinute: Byte,
    var endHour: Byte,
    var endMinute: Byte
)