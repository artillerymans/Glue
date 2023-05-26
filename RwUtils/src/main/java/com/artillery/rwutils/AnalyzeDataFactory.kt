package com.artillery.rwutils

import android.widget.Button
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.artillery.rwutils.exts.zeroByte
import com.artillery.rwutils.model.BleResult
import com.artillery.rwutils.model.CurrentDayStepsData
import com.artillery.rwutils.model.MeasureType
import com.artillery.rwutils.model.RealTimeData
import com.artillery.rwutils.model.SleepInfoData
import com.artillery.rwutils.model.SoftVersionModel
import com.artillery.rwutils.model.ThreeElements
import com.artillery.rwutils.model.ThreeElementsCurrent
import com.artillery.rwutils.model.ThreeElementsRealTime
import com.artillery.rwutils.type.SwitchType
import com.blankj.utilcode.util.LogUtils
import kotlin.experimental.and


/**
 * 解析数据
 */
object AnalyzeDataFactory {

    /**
     * 日期设置回复的数据
     */
    fun analyze0x81For0x01(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        val code = buffer.get()
        return BleResult(cmd.byte2Int(), code.toInt(), 0)
    }

    /**
     * 解析手环支持的功能列表
     */
    fun analyze0x83For0x03(bytes: ByteArray) {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        if (cmd.byte2Int() == BleConstantData.REPLY_CMD_83) {
            //项目固件名称最长 8 字节
            val versionStr = ByteArray(8).apply {
                buffer.get(this)
            }.map { byte -> if (byte == 0.toByte()) '0' else byte.toInt().toChar() }
                .joinToString("")
            //固件平台 1
            val firmwarePlatform = buffer.get()
            //固件型号 1
            val firmwareModel = buffer.get()
            val supportFunction = buffer.get()
            //是否支持心率
            val heartRatesSupport = (supportFunction and 0x01) > zeroByte()
            //是否支持血压
            val bloodPressureSupport = (supportFunction and 0x02) > zeroByte()
            //是否支持血氧
            val bloodOxygenSupport = (supportFunction and 0x04) > zeroByte()
            //是否支持ota
            val otaSupport = buffer.get() > zeroByte()
            //是否支持天气
            val weatherSupport = buffer.get() > zeroByte()
            //闹钟个数
            val alarmNumber = buffer.get().toInt()
            //是否支持提醒功能
            val remindSupport = buffer.get() > zeroByte()

            //16 17 过
            val number16to17 = buffer.short
            //是否支持计步
            val stepSupport = (number16to17 and 0x01) > zeroByte()

        }
    }

    /**
     * 设置用户体重数据等 目标步数返回的
     */
    fun analyze0x82For0x02(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_82) {
            //序列号 共用0x02命令
            val sequence = buffer.get()
            val code = buffer.get()
            BleResult(cmd.byte2Int(), code.toInt(), sequence.toInt())
        } else {
            BleResult<Int>()
        }
    }

    /**
     * 查找手环返回的数据
     */
    fun analyze0xD1For0x51(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_D1) {
            //0x01：查找手环图标显示并震动
            // 0x00：查找手环图标不显示，并关闭震动
            // 0xEE：收到错误数据
            val status = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                status
            )
        } else {
            BleResult<Int>()
        }
    }

    /**
     * 需要回复的命令码
     */
    fun analyze0x53(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_53) {
            //命令： 0x01：寻找手机（enable） 0x00：寻找手机（disable）
            val status = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                status
            )
        } else {
            BleResult<Int>()
        }
    }

    /**
     * 蓝牙拍照
     */
    fun analyze0xD2For0x52(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_D2) {
            //0x01：APP 进入拍照界面（enable）
            // 0x00：APP 退出拍照界面（disable）
            val status = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                status
            )
        } else {
            BleResult<Int>()
        }
    }


    /**
     * 设备发送实时数据(更新步数专用,手环步数有增加时会发指令给 APP)
     * 步数上报开关打开后才会上报数据，白天模式且有运动数据时一秒一次上发。
     */
    fun analyze0xB3(bytes: ByteArray): BleResult<RealTimeData> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_B3) {
            //0x01：实时步数数据 有效
            val number1 = buffer.get()
            //总步数
            val step = buffer.int
            //卡路里
            val calorie = buffer.short
            //总里程 单位米
            val totalMileage = buffer.int
            //活动时长 单位秒
            val activeTime = buffer.int

            BleResult(
                cmd.byte2Int(),
                -1,
                RealTimeData(
                    step,
                    calorie,
                    totalMileage,
                    activeTime
                )
            )
        } else {
            BleResult<RealTimeData>()
        }
    }


    /**
     * 解析电量
     */
    fun analyze0x94For0x14(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_94) {
            //电量 0到100
            val batteryLevel = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                -1,
                batteryLevel
            )
        } else {
            BleResult()
        }
    }

    /**
     * 实时血压心率血氧 测量停止或者启动
     */
    fun analyze0xE0For0x60(bytes: ByteArray): BleResult<MeasureType> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_E0) {
            val state = buffer.get().byte2Int()
            val value = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                -1,
                MeasureType.of(state).apply {
                    enable = SwitchType.of(value)
                }
            )
        } else {
            BleResult()
        }
    }

    /**
     * 实时心率血压血氧
     */
    fun analyze0xE1(bytes: ByteArray): BleResult<ThreeElementsRealTime> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_E1) {
            val heartRate = buffer.get().byte2Int()
            val maxBloodPressure = buffer.get().byte2Int()
            val minBloodPressure = buffer.get().byte2Int()
            val bloodOxygen = buffer.get().byte2Int()
            val heartRateAverage = buffer.get().byte2Int()
            val maxHeartRateValue = buffer.get().byte2Int()
            val minHeartRateValue = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                -1,
                ThreeElementsRealTime(
                    heartRate,
                    heartRateAverage,
                    maxHeartRateValue,
                    minHeartRateValue,
                    maxBloodPressure,
                    minBloodPressure,
                    bloodOxygen
                )
            )
        } else {
            BleResult()
        }
    }

    /**
     * 获取软件版本的回复
     */
    fun analyze0x9fFor0x1f(bytes: ByteArray): BleResult<SoftVersionModel> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_9F) {
            //编译日期(Sep 12,2017)
            val versionDes = ByteArray(11).apply {
                buffer.get(this)
            }.map { byte -> byte.byte2Int().toChar() }.joinToString("")
            //版本号,例如 1.0
            val versionCodeStr = ByteArray(2).apply {
                buffer.get(this)
            }.map { byte -> byte.byte2Int() }.joinToString(".")

            BleResult(
                cmd,
                -1,
                SoftVersionModel(
                    versionDes,
                    versionCodeStr
                )
            )
        } else {
            BleResult()
        }
    }

    /**
     * 解析当日的运动步数
     */
    fun analyze0x93For0x13(bytes: ByteArray): BleResult<CurrentDayStepsData> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_93) {
            val year = buffer.get().byte2Int() + 2000
            val month = buffer.get().byte2Int()
            val day = buffer.get().byte2Int()
            //如果不等于0xff就是小时
            val flagOrHour = buffer.get().byte2Int()
            if (flagOrHour == 0xff) {
                //这是最后一包
            }
            //最后一包是当日的总步数
            val steps = buffer.int
            //卡路里 大卡
            val calorie = buffer.short
            //里程单位米
            val mileage = buffer.int
            //运动总时长 分钟
            val sportTime = buffer.short

            BleResult(
                cmd,
                -1,
                CurrentDayStepsData(
                    year,
                    month,
                    day,
                    flagOrHour,
                    steps,
                    calorie,
                    mileage,
                    sportTime
                )
            )


        } else {
            BleResult(
                cmd,
                -1,
                null
            )

        }
    }


    /**
     * 睡眠数据
     */
    fun analyze0x95For0x15(bytes: ByteArray): BleResult<List<SleepInfoData>?> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        val list = mutableListOf<SleepInfoData>()
        if (cmd == BleConstantData.REPLY_CMD_95){
            val short = buffer.short
            if (short == 0xffff.toShort()){
                return BleResult(cmd, -1, null)
            }
            list.add(
                SleepInfoData(
                    short,
                    buffer.int,
                    buffer.get().byte2Int() * 5,
                    buffer.get().byte2Int()
                )
            )
            list.add(
                SleepInfoData(
                    short,
                    buffer.int,
                    buffer.get().byte2Int() * 5,
                    buffer.get().byte2Int()
                )
            )
        }
        return BleResult(cmd, -1, list)
    }


    /**
     * 解析血压血压心率
     */
    fun analyze0x96For0x16(bytes: ByteArray): BleResult<List<ThreeElements>?> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        val list = mutableListOf<ThreeElements>()
        if (cmd == BleConstantData.REPLY_CMD_96){
            val short = buffer.short   //序列号
            if (short == 0xffff.toShort()){
                return BleResult(cmd, -1, null)
            }
            list.add(
                ThreeElements(
                    buffer.int,
                    buffer.get().byte2Int(),
                    buffer.get().byte2Int(),
                    buffer.get().byte2Int(),
                    buffer.get().byte2Int()
                )
            )
            list.add(
                ThreeElements(
                    buffer.int,
                    buffer.get().byte2Int(),
                    buffer.get().byte2Int(),
                    buffer.get().byte2Int(),
                    buffer.get().byte2Int()
                )
            )
        }
        return BleResult(cmd, -1, list)
    }


    /**
     * 获取当前血氧血氧血糖心率数据
     */
    fun analyze0x97For0x17(bytes: ByteArray): BleResult<ThreeElementsCurrent> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_97) {
            val heartRate = buffer.get().byte2Int()
            val maxBloodPressure = buffer.get().byte2Int()
            val minBloodPressure = buffer.get().byte2Int()
            val bloodOxygen = buffer.get().byte2Int()
            val bloodSugar = buffer.get().byte2Int()
            LogUtils.d("analyze0x97For0x17: $bloodSugar")
            val bloodSugarStr = bloodSugar.toString().toMutableList().joinToString(".")

            BleResult(
                cmd.byte2Int(),
                -1,
                ThreeElementsCurrent(
                    heartRate,
                    maxBloodPressure,
                    minBloodPressure,
                    bloodOxygen,
                    bloodSugarStr
                )
            )
        } else {
            BleResult()
        }
    }
}