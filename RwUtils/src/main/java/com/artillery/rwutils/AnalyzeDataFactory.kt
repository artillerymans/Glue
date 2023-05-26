package com.artillery.rwutils

import android.widget.Button
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.toBuffer
import com.artillery.rwutils.exts.zeroByte
import com.artillery.rwutils.model.Aggregate
import com.artillery.rwutils.model.AlarmChoiceDay
import com.artillery.rwutils.model.AlarmClock
import com.artillery.rwutils.model.BleResult
import com.artillery.rwutils.model.CurrentDayStepsData
import com.artillery.rwutils.model.DistanceUnit
import com.artillery.rwutils.model.MeasureType
import com.artillery.rwutils.model.NoticeType
import com.artillery.rwutils.model.RealTimeData
import com.artillery.rwutils.model.SDD
import com.artillery.rwutils.model.SleepInfoData
import com.artillery.rwutils.model.SoftVersionModel
import com.artillery.rwutils.model.TemperatureUnit
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
           /* val bloodSugar = buffer.get().byte2Int()
            LogUtils.d("analyze0x97For0x17: $bloodSugar")
            val bloodSugarStr = bloodSugar.toString().toMutableList().joinToString(".")*/

            BleResult(
                cmd.byte2Int(),
                -1,
                ThreeElementsCurrent(
                    heartRate,
                    maxBloodPressure,
                    minBloodPressure,
                    bloodOxygen,
                    "0"
                )
            )
        } else {
            BleResult()
        }
    }

    /**
     * 天气同步的回复数据解析
     */
    fun analyze0x85For05(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_85) {
            val state = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                state,
                state
            )
        } else {
            BleResult()
        }
    }

    fun analyze0x84For04(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_84) {
            val state = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                state,
                state
            )
        } else {
            BleResult()
        }
    }


    fun analyze0xE2For0x62(bytes: ByteArray): BleResult<Aggregate>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_E2) {
            val order = buffer.get().byte2Int()
            if (order == 0x02){
                val skypeValue = buffer.get().byte2Int()
                val lineValue = buffer.get().byte2Int()
                val inCallValue = buffer.get().byte2Int()
                val smsValue = buffer.get().byte2Int()
                val wxValue = buffer.get().byte2Int()
                val qqValue = buffer.get().byte2Int()
                val kakaoTalkValue = buffer.get().byte2Int()
                val facebookValue = buffer.get().byte2Int()
                val twitterValue = buffer.get().byte2Int()
                val whatsAppValue = buffer.get().byte2Int()
                val linkedinValue = buffer.get().byte2Int()
                val viberValue = buffer.get().byte2Int()
                val instagramValue = buffer.get().byte2Int()
                val messengerValue = buffer.get().byte2Int()
                val otherValue = buffer.get().byte2Int()

                val skype = NoticeType.Skype(SwitchType.of(skypeValue))
                val line = NoticeType.Line(SwitchType.of(lineValue))
                val inCall = NoticeType.InCall(SwitchType.of(inCallValue))
                val sms = NoticeType.Sms(SwitchType.of(smsValue))
                val wx = NoticeType.WeiXin(SwitchType.of(wxValue))
                val qq = NoticeType.QQ(SwitchType.of(qqValue))
                val kakaoTalk = NoticeType.KakaoTalk(SwitchType.of(kakaoTalkValue))
                val facebook = NoticeType.Facebook(SwitchType.of(facebookValue))
                val twitter = NoticeType.Twitter(SwitchType.of(twitterValue))
                val whatsApp = NoticeType.WhatsApp(SwitchType.of(whatsAppValue))
                val linkedin = NoticeType.Linkedin(SwitchType.of(linkedinValue))
                val viber = NoticeType.Viber(SwitchType.of(viberValue))
                val instagram = NoticeType.Instagram(SwitchType.of(instagramValue))
                val messenger = NoticeType.Messenger(SwitchType.of(messengerValue))
                val other = NoticeType.Other(SwitchType.of(otherValue))

                val lightUpTheScreenValue = buffer.get().byte2Int()
                //抬腕亮屏
                val lightUpTheScreen = SwitchType.of(lightUpTheScreenValue)
                val whileHeartRatesValue = buffer.get().byte2Int()
                //循环心率检测
                val whileHeartRates = SwitchType.of(whileHeartRatesValue)
                //循环心率检测间隔 单位分钟
                val whileHeartRatesInterval = buffer.get().byte2Int()

                BleResult(
                    cmd.byte2Int(),
                    -1,
                    Aggregate.NoticeAggregate(
                        skype, line, inCall,
                        sms, wx, qq, kakaoTalk,
                        facebook, twitter, whatsApp,
                        linkedin, viber, instagram,
                        messenger, other, lightUpTheScreen,
                        whileHeartRates, whileHeartRatesInterval
                    )
                )
            }else if(order == 0x03){
                //读取到的是闹钟信息
                val list = mutableListOf<AlarmClock>()
                while (buffer.limit() > 4 && buffer.position() < 14){
                    val enable = SwitchType.of(buffer.get().byte2Int())
                    val startTime = buffer.short
                    val choiceDay = buffer.get().byte2Int()
                    list.add(
                        AlarmClock(
                            enable,
                            startTime,
                            mutableListOf<AlarmChoiceDay>().apply {
                                if (choiceDay and AlarmChoiceDay.Monday().byte > 0){
                                    add(AlarmChoiceDay.Monday())
                                }
                                if (choiceDay and AlarmChoiceDay.Tuesday().byte > 0){
                                    add(AlarmChoiceDay.Tuesday())
                                }
                                if (choiceDay and AlarmChoiceDay.Wednesday().byte > 0){
                                    add(AlarmChoiceDay.Wednesday())
                                }
                                if (choiceDay and AlarmChoiceDay.Thursday().byte > 0){
                                    add(AlarmChoiceDay.Thursday())
                                }
                                if (choiceDay and AlarmChoiceDay.Friday().byte > 0){
                                    add(AlarmChoiceDay.Friday())
                                }
                                if (choiceDay and AlarmChoiceDay.Saturday().byte > 0){
                                    add(AlarmChoiceDay.Saturday())
                                }
                                if (choiceDay and AlarmChoiceDay.Sunday().byte > 0){
                                    add(AlarmChoiceDay.Sunday())
                                }
                            }
                        )
                    )
                }


                BleResult(
                    cmd.byte2Int(),
                    -1,
                    Aggregate.AlarmAggregate(list)
                )
            }else if (order == 0x04){
                //久坐 勿扰 喝水
                val list = mutableListOf<SDD>()
                var enable = SwitchType.of(buffer.get().byte2Int())
                var interval = buffer.get()
                var startHour = buffer.get()
                var startMinute = buffer.get()
                var endHour = buffer.get()
                var endMinute = buffer.get()
                list.add(
                    SDD.Sedentary(
                        enable,
                        interval,
                        startHour,
                        startMinute,
                        endHour, endMinute
                    )
                )

                enable = SwitchType.of(buffer.get().byte2Int())
                startHour = buffer.get()
                startMinute = buffer.get()
                endHour = buffer.get()
                endMinute = buffer.get()
                list.add(
                    SDD.DonTDisturb(
                        enable,
                        startHour,
                        startMinute,
                        endHour,
                        endMinute
                    )
                )

                enable = SwitchType.of(buffer.get().byte2Int())
                interval = buffer.get()
                startHour = buffer.get()
                startMinute = buffer.get()
                endHour = buffer.get()
                endMinute = buffer.get()
                list.add(
                    SDD.DrinkingWater(
                        enable,
                        interval,
                        startHour,
                        startMinute,
                        endHour, endMinute
                    )
                )

                BleResult(
                    cmd.byte2Int(),
                    -1,
                    Aggregate.SddAggregate(
                        list
                    )
                )
            }else if (order == 0x05){
                val clockDial = buffer.get().byte2Int()
                val sum = buffer.get().byte2Int()
                val temperValue = buffer.get().byte2Int()
                val temperatureUnit = TemperatureUnit.of(temperValue)
                val distanceValue = buffer.get().byte2Int()
                val distanceUnit = DistanceUnit.of(distanceValue)
                BleResult(
                    cmd.byte2Int(),
                    -1,
                    Aggregate.ClockDialUnitAggregate(
                        clockDial,
                        sum,
                        temperatureUnit,
                        distanceUnit
                    )
                )
            }else {
                BleResult()
            }
        } else {
            BleResult()
        }
    }
}