package com.artillery.rwutils

import android.widget.Button
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.byte2Int
import com.artillery.rwutils.exts.short2Int
import com.artillery.rwutils.exts.toBuffer
import com.artillery.rwutils.exts.toBytesLowerThree
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
import com.artillery.rwutils.model.ReplyBgItem
import com.artillery.rwutils.model.ReplyBinItem
import com.artillery.rwutils.model.ReplyClockDialItem
import com.artillery.rwutils.model.ReplyContactsItem
import com.artillery.rwutils.model.ReplySportItem
import com.artillery.rwutils.model.ReplyTemperatureItem
import com.artillery.rwutils.model.SDD
import com.artillery.rwutils.model.SleepInfoData
import com.artillery.rwutils.model.SoftVersionModel
import com.artillery.rwutils.model.SportType
import com.artillery.rwutils.model.TemperatureUnit
import com.artillery.rwutils.model.ThreeElements
import com.artillery.rwutils.model.ThreeElementsCurrent
import com.artillery.rwutils.model.ThreeElementsRealTime
import com.artillery.rwutils.type.SwitchType
import com.blankj.utilcode.util.LogUtils
import java.math.BigDecimal
import java.nio.ByteBuffer
import kotlin.experimental.and


/**
 * 解析数据
 * 函数的命名规则
 * e.g ： analyze0xD1For0x51  表示 接收命令是0xD1 发送码是0x51 即理解为发送51后回复D1
 * e.g : analyze0x53 这种表示只是手环发送过来的命令
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
            val sequence = buffer.get().byte2Int()
            val code = buffer.get().byte2Int()
            BleResult(cmd.byte2Int(), code, sequence)
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
    fun analyze0xB3(bytes: ByteArray): BleResult<RealTimeData?> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_B3) {
            //0x01：实时步数数据 有效
            val number1 = buffer.get().byte2Int()
            //总步数
            val step = buffer.int
            //卡路里
            val calorie = buffer.short.short2Int()
            //总里程 单位米
            val totalMileage = buffer.int
            //活动时长 单位秒
            val activeTime = buffer.int

            BleResult(
                cmd.byte2Int(),
                if (number1 == 0x01) BleConstantData.SUCCESS_BLE_CODE else BleConstantData.FAIL_BLE_CODE,
                RealTimeData(
                    step,
                    calorie,
                    totalMileage,
                    activeTime
                )
            )
        } else {
            BleResult()
        }
    }


    /**
     * 解析电量
     */
    fun analyze0x94For0x14(bytes: ByteArray): BleResult<Int?> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_94) {
            //电量 0到100
            val batteryLevel = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                BleConstantData.SUCCESS_BLE_CODE,
                batteryLevel
            )
        } else {
            BleResult(
                cmd.byte2Int(),
                -1
            )
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
                BleConstantData.SUCCESS_BLE_CODE,
                MeasureType.of(state).apply {
                    enable = SwitchType.of(value)
                }
            )
        } else {
            BleResult(
                cmd.byte2Int(),
                BleConstantData.FAIL_BLE_CODE
            )
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

            val heartRateAverage = if (buffer.hasRemaining()) {
                buffer.get().byte2Int()
            } else {
                0
            }
            val maxHeartRateValue = if (buffer.hasRemaining()) {
                buffer.get().byte2Int()
            } else {
                0
            }
            val minHeartRateValue = if (buffer.hasRemaining()) {
                buffer.get().byte2Int()
            } else {
                0
            }
            BleResult(
                cmd.byte2Int(),
                BleConstantData.SUCCESS_BLE_CODE,
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
            BleResult(
                cmd.byte2Int(),
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 获取软件版本的回复
     */
    fun analyze0x9fFor0x1f(bytes: ByteArray): BleResult<SoftVersionModel?> {
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
                BleConstantData.SUCCESS_BLE_CODE,
                SoftVersionModel(
                    versionDes,
                    versionCodeStr
                )
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE
            )
        }
    }

    /**
     * 解析当日的运动步数
     */
    fun analyze0x93For0x13(bytes: ByteArray): BleResult<CurrentDayStepsData?> {
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
            val calorie = buffer.short.short2Int()
            //里程单位米
            val mileage = buffer.int
            //运动总时长 分钟
            val sportTime = buffer.short.short2Int()

            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
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
                BleConstantData.FAIL_BLE_CODE,
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
        if (cmd == BleConstantData.REPLY_CMD_95) {
            val order = buffer.short.short2Int()
            if (order == 0xffff) {
                return BleResult(cmd, BleConstantData.SUCCESS_BLE_CODE, null)
            }

            while (buffer.remaining() >= 6) {
                list.add(
                    SleepInfoData(
                        order,  //序列号
                        buffer.int,  //时间戳 单位秒
                        buffer.get().byte2Int() * 5, //时长一般为 5 的倍数，单位分钟
                        buffer.get().byte2Int() // 睡眠质量 1：深睡 2：浅睡 3： 醒着
                    )
                )
            }
        }
        return BleResult(cmd, BleConstantData.SUCCESS_BLE_CODE, list)
    }


    /**
     * 解析血压血压心率
     */
    fun analyze0x96For0x16(bytes: ByteArray): BleResult<List<ThreeElements>> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        val list = mutableListOf<ThreeElements>()
        if (cmd == BleConstantData.REPLY_CMD_96) {
            val short = buffer.short.short2Int()   //序列号
            if (short == 0xffff) {
                val timeZeroStart = buffer.int
                list.add(
                    ThreeElements(
                        timeZeroStart,
                        short
                    )
                )
                return BleResult(
                    cmd,
                    BleConstantData.SUCCESS_BLE_CODE,
                    list
                )
            }

            while (buffer.limit() >= 8) {
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
        }
        return BleResult(cmd, BleConstantData.SUCCESS_BLE_CODE, list)
    }


    /**
     * 获取当前血氧血氧血糖心率数据
     */
    fun analyze0x97For0x17(bytes: ByteArray): BleResult<ThreeElementsCurrent> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_97) {
            val heartRate = buffer.get().byte2Int()
            val maxBloodPressure = buffer.get().byte2Int()
            val minBloodPressure = buffer.get().byte2Int()
            val bloodOxygen = buffer.get().byte2Int()

            val bloodSugar = if (buffer.hasRemaining()) {
                buffer.get().byte2Int()
            } else {
                0
            }
            val bloodSugarStr = bloodSugar.toString().toMutableList().joinToString(".")

            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                ThreeElementsCurrent(
                    heartRate,
                    maxBloodPressure,
                    minBloodPressure,
                    bloodOxygen,
                    bloodSugarStr
                )
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 天气同步的回复数据解析
     */
    fun analyze0x85For05(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_85) {
            val state = buffer.get().byte2Int()
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                state
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 紫外线数据回复
     */
    fun analyze0x84For04(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_84) {
            val state = buffer.get().byte2Int()
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                state
            )
        } else {
            BleResult(cmd, BleConstantData.SUCCESS_BLE_CODE)
        }
    }


    fun analyze0xE2For0x62(bytes: ByteArray): BleResult<Aggregate> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_E2) {
            val order = buffer.get().byte2Int()
            when (order) {
                0x02 -> {
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
                        cmd,
                        BleConstantData.SUCCESS_BLE_CODE,
                        Aggregate.NoticeAggregate(
                            skype, line, inCall,
                            sms, wx, qq, kakaoTalk,
                            facebook, twitter, whatsApp,
                            linkedin, viber, instagram,
                            messenger, other, lightUpTheScreen,
                            whileHeartRates, whileHeartRatesInterval
                        )
                    )
                }

                0x03,0x06 -> {
                    //读取到的是闹钟信息
                    val list = mutableListOf<AlarmClock>()
                    while (buffer.remaining() >= 4 && buffer.position() < 14) {
                        val enable = SwitchType.of(buffer.get().byte2Int())
                        val startTime = buffer.short
                        val choiceDay = buffer.get().byte2Int()
                        list.add(
                            AlarmClock(
                                enable,
                                startTime,
                                mutableListOf<AlarmChoiceDay>().apply {
                                    if (choiceDay and AlarmChoiceDay.Monday().byte > 0) {
                                        add(AlarmChoiceDay.Monday())
                                    }
                                    if (choiceDay and AlarmChoiceDay.Tuesday().byte > 0) {
                                        add(AlarmChoiceDay.Tuesday())
                                    }
                                    if (choiceDay and AlarmChoiceDay.Wednesday().byte > 0) {
                                        add(AlarmChoiceDay.Wednesday())
                                    }
                                    if (choiceDay and AlarmChoiceDay.Thursday().byte > 0) {
                                        add(AlarmChoiceDay.Thursday())
                                    }
                                    if (choiceDay and AlarmChoiceDay.Friday().byte > 0) {
                                        add(AlarmChoiceDay.Friday())
                                    }
                                    if (choiceDay and AlarmChoiceDay.Saturday().byte > 0) {
                                        add(AlarmChoiceDay.Saturday())
                                    }
                                    if (choiceDay and AlarmChoiceDay.Sunday().byte > 0) {
                                        add(AlarmChoiceDay.Sunday())
                                    }
                                }
                            )
                        )
                    }


                    BleResult(
                        cmd,
                        BleConstantData.SUCCESS_BLE_CODE,
                        Aggregate.AlarmAggregate(list)
                    )
                }

                0x04 -> {
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
                        cmd,
                        BleConstantData.SUCCESS_BLE_CODE,
                        Aggregate.SddAggregate(
                            list
                        )
                    )
                }

                0x05 -> {
                    val clockDial = buffer.get().byte2Int()
                    val sum = buffer.get().byte2Int()
                    val temperValue = buffer.get().byte2Int()
                    val temperatureUnit = TemperatureUnit.of(temperValue)
                    val distanceValue = buffer.get().byte2Int()
                    val distanceUnit = DistanceUnit.of(distanceValue)
                    BleResult(
                        cmd,
                        BleConstantData.SUCCESS_BLE_CODE,
                        Aggregate.ClockDialUnitAggregate(
                            clockDial,
                            sum,
                            temperatureUnit,
                            distanceUnit
                        )
                    )
                }

                else -> {
                    BleResult(
                        cmd,
                        BleConstantData.FAIL_BLE_CODE
                    )
                }
            }
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }


    /**
     * 同步联系人后的回复信息
     */
    fun analyze0xE6For0x66(bytes: ByteArray): BleResult<ReplyContactsItem> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_E6) {
            //序号
            val order = buffer.short.short2Int()
            val flag = buffer.get().byte2Int()
            val state = buffer.get().byte2Int()
            val number = buffer.short.short2Int()

            BleResult(
                cmd,
                state,
                ReplyContactsItem(
                    order,
                    flag,
                    state,
                    number
                )
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 获取手环屏幕规格
     */
    fun analyze0xE7For0x67(bytes: ByteArray): BleResult<ReplyClockDialItem> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_E7) {
            val width = buffer.short.short2Int()
            val height = buffer.short.short2Int()
            val supportDigitalMeter = buffer.get().byte2Int()  //是否支持数字表盘
            val supportAnalogDial = buffer.get().byte2Int()  //是否支持模拟表盘
            val pictureFormat = buffer.get().byte2Int() //背景图片数据格式 0 预留 (默认 16bit RGB 数据)
            //跳两个字节
            val skipValue = buffer.short

            val dialProtocolType = buffer.short.short2Int() //支持的表盘协议类型
            val binFileMaxSize = buffer.int  //Bin 文件字节数最大值,单位 byte
            val watchShape = buffer.get().byte2Int() //手表形状: 0 方,1 圆,2 其他
            val customWallpaperLocation = buffer.get().byte2Int() //1 支持自定义壁纸位置等，0 不支持
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                ReplyClockDialItem(
                    width, height, supportDigitalMeter, supportAnalogDial,
                    pictureFormat, dialProtocolType, binFileMaxSize,
                    watchShape, customWallpaperLocation
                )
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 高速传输背景图片回复 包括预备动作的回复 以及发送完成的动作
     */
    fun analyze0xB8For0x38(bytes: ByteArray): BleResult<ReplyBgItem> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_B8) {
            //序号
            val order = buffer.short.short2Int()
            val state = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                state,
                ReplyBgItem(
                    order,
                    state
                )
            )
        } else {
            BleResult()
        }
    }

    /**
     * 自定义背景图片回复
     */
    fun analyze0xB9For0x39(bytes: ByteArray): BleResult<ReplyBinItem> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_B9) {
            //序号
            val orderBuffer = ByteBuffer.allocate(4)
            orderBuffer.put(0)
            orderBuffer.put(buffer.get())
            orderBuffer.put(buffer.get())
            orderBuffer.put(buffer.get())
            orderBuffer.flip()
            val order = orderBuffer.int
            val state = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                state,
                ReplyBinItem(
                    order,
                    state
                )
            )
        } else {
            BleResult()
        }
    }

    /**
     * 恢复出厂设置 回复
     */
    fun analyze0xF1For0x71(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get()
        return if (cmd.byte2Int() == BleConstantData.REPLY_CMD_F1) {
            //固定返回 0x01
            val state = buffer.get().byte2Int()
            BleResult(
                cmd.byte2Int(),
                BleConstantData.SUCCESS_BLE_CODE,
                state
            )
        } else {
            BleResult(
                cmd.byte2Int(),
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 低电量 通知
     */
    fun analyze0x72(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_72) {
            //固定返回 0x01
            //val state = buffer.get().byte2Int()
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 收到错误信息
     */
    fun analyze0xEE(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_EE) {
            //固定返回 0x01
            //val state = buffer.get().byte2Int()
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }


    /**
     *  启动停止实时体温检测 回复
     */
    fun analyze0xEAFor0x6A(bytes: ByteArray): BleResult<Int> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_EA) {
            //0x01：开启实时测量 0x00：停止实时测量
            val enable = buffer.get().byte2Int()
            //Succes 1, fail 0 垃圾文档 定义的跟之前相反的值
            val state = buffer.get().byte2Int()
            BleResult(
                cmd,
                if (state == 1) BleConstantData.SUCCESS_BLE_CODE else BleConstantData.FAIL_BLE_CODE,
                enable
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }

    /**
     * 实时上报体温检测数据
     */
    fun analyze0xEB(bytes: ByteArray): BleResult<String> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_EB) {
            val temperature = buffer.short.short2Int()

            val temperatureStr = if (temperature == 0) "" else {
                BigDecimal(temperature).divide(BigDecimal(10)).toString()
            }
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                temperatureStr
            )
        } else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }


    /**
     * 获取设备体温检测历史数据 回复
     */
    fun analyze0x9AFor0x1A(bytes: ByteArray): BleResult<List<ReplyTemperatureItem>> {
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        if (cmd == BleConstantData.REPLY_CMD_9A) {
            val list = mutableListOf<ReplyTemperatureItem>()
            while (buffer.limit() >= 6) {
                val timeStamp = buffer.int
                if (timeStamp.toLong() == 0xffffffff) {
                    val year = buffer.get().byte2Int() + 2000
                    val month = buffer.get().byte2Int()
                    val day = buffer.get().byte2Int()
                    list.add(
                        ReplyTemperatureItem(
                            timeStamp,
                            "",
                            year, month, day
                        )
                    )
                    return BleResult(
                        cmd,
                        BleConstantData.SUCCESS_BLE_CODE,
                        list
                    )
                } else {
                    val temperature = buffer.short.short2Int()
                    val temperatureStr = if (temperature == 0) "" else {
                        BigDecimal(temperature).divide(BigDecimal(10)).toString()
                    }
                    list.add(
                        ReplyTemperatureItem(
                            timeStamp,
                            temperatureStr
                        )
                    )
                }
            }

            return BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                list
            )
        } else {
            return BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }


    /**
     * 实时更新当前正启动的运动模式数据
     */
    fun analyze0xA0(bytes: ByteArray): BleResult<ReplySportItem>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_A0) {
            val skipIndex0 = buffer.get()  //跳过一位
            val sportValue = buffer.get().byte2Int()
            val sportType = SportType.of(sportValue)
            val startSportTime = buffer.int
            val sportDuration = buffer.short.short2Int()
            val calorie = buffer.short.short2Int()
            val steps = buffer.int
            val heartRate = buffer.get().byte2Int()
            val sportStatus = buffer.get().byte2Int()
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                        ReplySportItem(
                            sportType,
                            startSportTime,
                            sportDuration,
                            calorie, steps,
                            heartRate,
                            sportStatus
                        )
            )
        }else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }


    /**
     *  同步手环运动记录 回复
     */
    fun analyze0xA1For21(bytes: ByteArray): BleResult<ReplySportItem>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_A1) {
            val order = buffer.get().byte2Int() //0-14(最多保存 15 条记录)
            val sportValue = buffer.get().byte2Int()
            val sportType = SportType.of(sportValue)
            val startSportTime = buffer.int
            val sportDuration = buffer.short.short2Int()
            val calorie = buffer.short.short2Int()
            val steps = buffer.int
            val heartRate = buffer.get().byte2Int()
            BleResult(
                cmd,
                BleConstantData.SUCCESS_BLE_CODE,
                ReplySportItem(
                    sportType,
                    startSportTime,
                    sportDuration,
                    calorie,
                    steps,
                    heartRate,
                    -1,
                    order
                )
            )
        }else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }


    /**
     * 收款码 预备动作回复
     * index 3  如果0 表示失败  1 表示成功
     */
    fun analyze0xBAFor0x3A(bytes: ByteArray): BleResult<Int>{
        val buffer = bytes.toBuffer()
        val cmd = buffer.get().byte2Int()
        return if (cmd == BleConstantData.REPLY_CMD_BA) {
            //如果等于0xffff 表示是发送完成的后的回复
            val value = buffer.short.short2Int()
            val state = buffer.get().byte2Int()
            BleResult(
                cmd,
                if (state == 1) BleConstantData.SUCCESS_BLE_CODE else BleConstantData.FAIL_BLE_CODE,
                value
            )
        }else {
            BleResult(
                cmd,
                BleConstantData.FAIL_BLE_CODE
            )
        }
    }








}