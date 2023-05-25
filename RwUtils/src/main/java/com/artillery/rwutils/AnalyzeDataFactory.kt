package com.artillery.rwutils

import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.artillery.rwutils.exts.zeroByte
import com.artillery.rwutils.model.BleResult
import com.artillery.rwutils.model.RealTimeData
import com.artillery.rwutils.model.SoftVersionModel
import java.nio.ByteBuffer
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
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_82){
            //序列号 共用0x02命令
            val sequence = buffer.get()
            val code = buffer.get()
            BleResult(cmd.byte2Int(), code.toInt(), sequence.toInt())
        }else {
            BleResult<Int>()
        }
    }

    /**
     * 查找手环返回的数据
     */
    fun analyze0xD1For0x51(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_D1){
            //0x01：查找手环图标显示并震动
            // 0x00：查找手环图标不显示，并关闭震动
            // 0xEE：收到错误数据
            val status = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                status
            )
        }else {
            BleResult<Int>()
        }
    }

    /**
     * 需要回复的命令码
     */
    fun analyze0x53(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_53){
            //命令： 0x01：寻找手机（enable） 0x00：寻找手机（disable）
            val status = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                status
            )
        }else {
            BleResult<Int>()
        }
    }

    /**
     * 蓝牙拍照
     */
    fun analyze0xD2For0x52(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_D2){
            //0x01：APP 进入拍照界面（enable）
            // 0x00：APP 退出拍照界面（disable）
            val status = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                status
            )
        }else {
            BleResult<Int>()
        }
    }



    /**
     * 设备发送实时数据(更新步数专用,手环步数有增加时会发指令给 APP)
     * 步数上报开关打开后才会上报数据，白天模式且有运动数据时一秒一次上发。
     */
    fun analyze0xB3(bytes: ByteArray): BleResult<RealTimeData>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_B3){
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
        }else {
            BleResult<RealTimeData>()
        }
    }


    /**
     * 解析电量
     */
    fun analyze0x94For0x14(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_94){
            //电量 0到100
            val batteryLevel = buffer.get().toInt()
            BleResult(
                cmd.byte2Int(),
                -1,
                batteryLevel
            )
        }else {
            BleResult()
        }
    }

    /**
     * 获取软件版本的回复
     */
    fun analyze0x9fFor0x1f(bytes: ByteArray): BleResult<SoftVersionModel>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_9F){
            //编译日期(Sep 12,2017)
            val versionDes = ByteArray(11).apply {
                buffer.get(this)
            }.map { byte -> byte.toInt().toChar() }.joinToString("")
            //版本号,例如 1.0
            val versionCodeStr = ByteArray(2).apply {
                buffer.get(this)
            }.map { byte -> byte.toInt() }.joinToString(".")

            BleResult(
                cmd.byte2Int(),
                -1,
                SoftVersionModel(
                    versionDes,
                    versionCodeStr
                )
            )
        }else {
            BleResult()
        }
    }















}