package com.artillery.rwutils

import android.graphics.Bitmap
import android.text.format.DateFormat
import com.artillery.rwutils.cmd.BleConstantData
import com.artillery.rwutils.exts.fillZeros
import com.artillery.rwutils.exts.toByte
import com.artillery.rwutils.exts.toByteArrays
import com.artillery.rwutils.exts.toBytes
import com.artillery.rwutils.exts.toBytesLowerThree
import com.artillery.rwutils.model.AlarmClock
import com.artillery.rwutils.model.BWeather
import com.artillery.rwutils.model.BloodTimeType
import com.artillery.rwutils.model.ContactsItem
import com.artillery.rwutils.model.DistanceUnit
import com.artillery.rwutils.model.NoticeType
import com.artillery.rwutils.model.ProcessDataRequest
import com.artillery.rwutils.model.SDD
import com.artillery.rwutils.model.TemperatureUnit
import com.artillery.rwutils.type.DayFormatType
import com.artillery.rwutils.type.Gender
import com.artillery.rwutils.type.SwitchType
import com.artillery.rwutils.type.SystemType
import com.artillery.rwutils.type.ZhType
import com.artillery.rwutils.type.getLanguageType
import com.blankj.utilcode.util.FileIOUtils
import com.blankj.utilcode.util.LanguageUtils
import com.blankj.utilcode.util.Utils
import java.nio.ByteBuffer
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import kotlin.experimental.or

/**
 * @author : zhiweizhu
 * create on: 2023/5/22 下午3:55
 * 当前创建的都是发送的数据
 */
object CreateDataFactory {
    /**
     * 设置日期时间
     */
    fun createDateTime(): ByteArray {
        val calendar = Calendar.getInstance()
        //UTC时间戳
        val timeInSecond = calendar.timeInMillis / 1000
        //时区
        val timeZone: TimeZone = calendar.timeZone
        val offset: Int = timeZone.rawOffset / 1000 / 60 / 60
        val isHour24 = DateFormat.is24HourFormat(Utils.getApp())
        val language = LanguageUtils.getAppContextLanguage()
        return createDateTime(
            timeInSecond.toInt(),
            offset,
            (if (isHour24) DayFormatType.HOUR_24.ordinal else DayFormatType.HOUR_12.ordinal).toByte(),
            language.getLanguageType().ordinal.toByte(),
            (if (language.language.equals(Locale.SIMPLIFIED_CHINESE.language)) ZhType.YES_ZH.ordinal else ZhType.NO_ZH.ordinal).toByte(),
            SystemType.ANDROID.ordinal.toByte()
        )
    }

    /**
     * 设置日期时间
     */
    fun createDateTime(
        timeInSecond: Int,
        timeZone: Int,
        hour12: Byte,
        otherLanguage: Byte,
        isChinese: Byte,
        os: Byte,
        cmd: Byte = BleConstantData.CMD_0x01.toByte(),
    ): ByteArray {
        return ByteArray(20).apply {
            ByteBuffer.wrap(this).apply {
                //命令
                put(cmd)
                //UTC时间戳到秒
                putInt(timeInSecond)
                //时区
                putInt(timeZone * 60 * 60)
                //时间格式
                put(hour12)
                //其他语言
                put(otherLanguage)
                //是否是中文
                put(isChinese)
                //操作系统
                put(os)
            }
        }

    }


    /**
     * 获取手环功能列表
     */
    fun createWatchFunctionList(): ByteArray {
        return ByteArray(2).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0X03)
                put(BleConstantData.RESERVED_DEF_0X00)
            }
        }
    }

    /**
     * 设备设置信息
     */
    fun createSettingUserInfo(
        weight: Short,
        age: Byte,
        height: Byte,
        stepLength: Byte,
        gender: Gender,  //0x00:女 0x01:男
        targetStep: Int,
    ): ByteArray {
        return ByteArray(20).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(0x01)
                putShort(weight)
                put(age)
                put(height)
                put(stepLength)
                put(if (gender == Gender.Woman) 0x00 else 0x01)
                putInt(targetStep)
            }
        }
    }

    /**
     * 设置通知开关
     * @param heartRateInterval 单位分钟
     */
    fun createSettingAppNoticeEnables(
        skypeEnable: SwitchType = SwitchType.OFF,
        lineEnable: SwitchType = SwitchType.OFF,
        incomingCallEnable: SwitchType = SwitchType.OFF,
        smsNoticeEnable: SwitchType = SwitchType.OFF,
        wxNoticeEnable: SwitchType = SwitchType.OFF,
        qqNoticeEnable: SwitchType = SwitchType.OFF,
        kakaoTalkEnable: SwitchType = SwitchType.OFF,
        facebookEnable: SwitchType = SwitchType.OFF,
        twitterEnable: SwitchType = SwitchType.OFF,
        whatsappEnable: SwitchType = SwitchType.OFF,
        linkedinEnable: SwitchType = SwitchType.OFF,
        viberEnable: SwitchType = SwitchType.OFF,
        instagramEnable: SwitchType = SwitchType.OFF,
        messengerEnable: SwitchType = SwitchType.OFF,
        otherAppEnable: SwitchType = SwitchType.OFF,
        liftScreenLightEnable: SwitchType = SwitchType.OFF,
        heartRateWhileEnable: SwitchType = SwitchType.OFF,
        heartRateInterval: Byte = 5,
    ): ByteArray {
        return ByteArray(20).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(0x02)
                put(skypeEnable.toByte())
                put(lineEnable.toByte())
                put(incomingCallEnable.toByte())
                put(smsNoticeEnable.toByte())
                put(wxNoticeEnable.toByte())
                put(qqNoticeEnable.toByte())
                put(kakaoTalkEnable.toByte())
                put(facebookEnable.toByte())
                put(twitterEnable.toByte())
                put(whatsappEnable.toByte())
                put(linkedinEnable.toByte())
                put(viberEnable.toByte())
                put(instagramEnable.toByte())
                put(messengerEnable.toByte())
                put(otherAppEnable.toByte())
                put(liftScreenLightEnable.toByte())
                put(heartRateWhileEnable.toByte())
                put(heartRateInterval)
            }
        }
    }


    /**
     * 设置闹钟 最大设置数量3个
     */
    fun createSettingsAlarmClock(
        list: MutableList<AlarmClock>,
    ): ByteArray {
        return ByteArray(20).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(0x03)
                list.forEach { item ->
                    put(item.enable.toByte())
                    putShort(item.startTime)
                    put(item.choiceDays.fold(0.toByte()) { acc, day -> acc or day.byte.toByte() })
                }
            }
        }
    }

    /**
     * 设置表盘 以及单位
     */
    fun createSettingsClockDialUnit(
        clockDialOrder: Int,
        temperatureUnit: TemperatureUnit,
        distanceUnit: DistanceUnit
    ): ByteArray {
        return ByteArray(5).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(0x05)
                put(clockDialOrder.toByte())
                put(temperatureUnit.value.toByte())
                put(distanceUnit.value.toByte())
            }
        }
    }

    /**
     * 设置久坐提醒,勿扰模式,喝水提醒
     * 按照久坐、勿扰、喝水等顺序依次设置
     */
    fun createSettingRemind(
        list: List<SDD>,
    ): ByteArray {
        return ByteArray(19).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(0x04)
                list.forEach { item ->
                    when(item){
                        is SDD.Sedentary -> {
                            put(item.enable.toByte())
                            //间隔时间
                            if (item.interval > 0.toByte()){
                                put(item.interval)
                            }
                            put(item.startHour)
                            put(item.startMinute)
                            put(item.endHour)
                            put(item.endMinute)
                        }
                        is SDD.DrinkingWater -> {
                            put(item.enable.toByte())
                            //间隔时间
                            if (item.interval > 0.toByte()){
                                put(item.interval)
                            }
                            put(item.startHour)
                            put(item.startMinute)
                            put(item.endHour)
                            put(item.endMinute)
                        }
                        is SDD.DonTDisturb -> {
                            put(item.enable.toByte())
                            put(item.startHour)
                            put(item.startMinute)
                            put(item.endHour)
                            put(item.endMinute)
                        }
                        else -> {

                        }
                    }

                }
            }
        }
    }

    /**
     * 设置血糖相关
     */
    fun createSettingBlood(
        bloodCorrect: Byte,  //血糖校准值 40 代表 4.0
        bloodTimeType: BloodTimeType
    ): ByteArray {
       return ByteArray(8).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(0x09)
                putInt((System.currentTimeMillis() / 1000).toInt())
                put(bloodCorrect)
                put(bloodTimeType.value.toByte())
            }
        }
    }


    /**
     * 查找手环
     */
    fun createFindWatchDevice(
        enable: SwitchType,
    ): ByteArray {
        return ByteArray(2).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x51)
                put(enable.toByte())
            }
        }
    }


    /**
     * 蓝牙拍照 手机通知蓝牙进入、退出拍照
     */
    fun createTakePhoto(enable: SwitchType): ByteArray {
        return ByteArray(2).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x52)
                put(enable.toByte())
            }
        }
    }

    /**
     * 蓝牙开始拍照
     */
    fun createStartTakePhoto(): ByteArray {
        return ByteArray(2).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0xA2.toByte())
                put(0x01)
            }
        }
    }


    /**
     * 获取电池电量
     */
    fun createBatteryLevel(): ByteArray {
        return ByteArray(1).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x14)
            }
        }
    }

    /**
     * 获取软件版本
     */
    fun createSoftVersion(): ByteArray {
        return ByteArray(1).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x1f)
            }
        }
    }

    /**
     * 获取设备运动数据
     * 步数里程运动时长
     */
    fun createStepsByTime(
        data: ProcessDataRequest.Steps,
    ): ByteArray {
        return createDataByTime(data)
    }

    /**
     * 睡眠数据
     */
    fun createSleepsByTime(
        data: ProcessDataRequest.Sleeps,
    ): ByteArray {
        return createDataByTime(data)
    }

    /**
     * 获取心率 血氧 血压
     */
    fun createHeartRateByTime(
        data: ProcessDataRequest.HeartRates,
    ): ByteArray {
        return createDataByTime(data)
    }

    /**
     * 血糖
     */
    fun createBloodByTime(
        data: ProcessDataRequest.Blood
    ): ByteArray {
        return createDataByTime(data)
    }

    /**
     * 运动数据获取
     */
    private fun createDataByTime(
        data: ProcessDataRequest,
    ): ByteArray {
        return ByteArray(4).apply {
            ByteBuffer.wrap(this).apply {
                put(data.cmd)
                put(data.toYearByte())
                put(data.month.toByte())
                put(data.day.toByte())
            }
        }
    }

    /**
     * 心率实时测试和测试时间
     *
     * @param byte 0x00 心率测量
     * @param byte 0x01 血压测量
     * @param byte 0x02 血氧测量
     * @param byte 0x03 All
     */
    fun createSwitchListen(
        enable: SwitchType,
        byte: Byte,
    ): ByteArray {
        return ByteArray(3).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x60)
                put(byte)
                put(enable.toByte())
            }
        }
    }


    /**
     * 恢复出厂设置
     */
    fun createResetFactorySetting(): ByteArray {
        return ByteArray(4).apply {
            ByteBuffer.wrap(this).apply {
                put(0x71)
                put(0x01)
                put(0x02)
                put(0x03)
            }
        }
    }

    /**
     * 创建推送消息
     */
    fun createMessagePush(text: String, type: NoticeType): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        val content = text.toByteArray()
        var offset = 0
        while (offset < content.size) {
            val remaining = content.size - offset
            val length = if (remaining > 17) 17 else remaining
            list.add(ByteArray(length + 3).apply {
                ByteBuffer.wrap(this).apply {
                    put(BleConstantData.CMD_0x73)  //命令码
                    put(list.size.toByte())
                    put(type.value.toByte())  //通知的类型
                    put(content, offset, length)
                }
            })
            offset += length
        }
        return list
    }


    /**
     * 获取当前手环心率
     */
    fun createCurrentHeartRate(): ByteArray {
        return ByteArray(4).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x17)
                put(0)
                put(0)
                put(0)
            }
        }
    }


    /**
     * 天气数据
     */
    fun createWeather(
        list: List<BWeather>   //当天天气在第0位
    ): ByteArray {
        return ByteArray(20).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x05)
                list.forEach { bWeather ->
                    //天气类型
                    put(bWeather.type.value.toByte())
                    if (bWeather is BWeather.TodayWeather) {
                        //当前温度正负
                        put(bWeather.currentTemperatureType.value.toByte())
                        //当前温度
                        put(bWeather.currentTemperature.toByte())
                    }
                    //最低温度
                    put(bWeather.minType.value.toByte())
                    put(bWeather.minTemperatureValue.toByte())
                    //最高温度
                    put(bWeather.maxType.value.toByte())
                    put(bWeather.maxTemperatureValue.toByte())
                }
            }
        }
    }


    /**
     * 紫外线 大气压
     */
    fun createUltravioletRaysAndPressure(
        ultravioletRays: Byte,   //75代表7.5  紫外线
        pressure: Short, //大气压  不带小数点
        humidity: Byte, //湿度
    ): ByteArray {
        return ByteArray(5).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x04)
                put(ultravioletRays)
                putShort(pressure)
                put(humidity)
            }
        }
    }


    /**
     * 读取各种开关 提醒 抬腕亮屏 心率检测开关
     */
    fun createReadNoticeEnables(): ByteArray {
        return createRead(0x02)
    }

    /**
     * 读取闹钟
     */
    fun createReadAlarms(): ByteArray {
        return createRead(0x03)
    }

    /**
     * 读取久坐提醒 勿扰模式 喝水提醒
     */
    fun createReadNoticeSettings(): ByteArray {
        return createRead(0x04)
    }


    /**
     * 读取表盘 温度单位 距离单位
     * 华摄氏 英制 公制
     */
    fun createReadClockDialUnit(): ByteArray {
        return createRead(0x05)
    }

    private fun createRead(
        serialNumber: Int
    ): ByteArray {
        return ByteArray(2).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x62)
                put(serialNumber.toByte())
            }
        }

    }

    /**
     * 设置表盘 摄氏度 华摄氏 英制公制等
     */
    fun createSetClockCompany(
        clockNumber: Int = 0,  //表盘序号0-15
        temperatureUnit: TemperatureUnit,
        distanceUnit: DistanceUnit,
        serialNumber: Int = 2
    ): ByteArray {
        return ByteArray(5).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x02)
                put(serialNumber.toByte())
                put(clockNumber.toByte())
                put(temperatureUnit.value.toByte())
                put(distanceUnit.value.toByte())
            }
        }
    }


    /**
     * 同步联系人
     */
    fun createSyncContacts(list: List<ContactsItem>): List<ByteArray> {
        return mutableListOf<ByteArray>().apply {
            list.forEachIndexed { index, item ->
                //最大值为1000
                val tempIndex = index.toShort()
                add(createByteBuffer(tempIndex, 0, item.name, BleConstantData.CMD_0x66))
                add(createByteBuffer(tempIndex, 1, item.mobile, BleConstantData.CMD_0x66))
            }
        }
    }

    private fun createByteBuffer(
        serialNumber: Short,
        type: Int,
        text: String,
        cmd: Byte
    ): ByteArray {
        return ByteArray(20).apply {
            ByteBuffer.wrap(this).apply {
                put(cmd)
                putShort(serialNumber)
                put(type.toByte())  //0 表示写入的是姓名 1 表示写入的是电话号码
                put(0)  //预留的 默认0
                val dataBytes = text.toByteArray()
                if (limit() >= dataBytes.size) {
                    put(dataBytes)
                    //如果后续还有空间对进行补0
                    if (hasRemaining()) {
                        fillZeros()
                    }
                } else {
                    put(dataBytes.toMutableList().subList(0, limit()).toByteArray())
                }
            }
        }
    }


    /**
     * 获取屏幕规格
     */
    fun createScreenSpecifications(): ByteArray {
        return ByteArray(1).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x67)
            }
        }
    }





    /**
     * 开始传输图片前开始发送准备动作
     */
    fun createFastTransferBitmapPrepare(): ByteArray {
        return ByteArray(5).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x38)
                putShort((0xfffe).toShort())
            }
        }
    }

    /**
     * 创建高速传递图片
     */
    fun createFastTransferBitmap(bytes: ByteArray): List<ByteArray>{
        return mutableListOf<ByteArray>().also {
            var offset = 0
            while (offset < bytes.size) {
                val remaining = bytes.size - offset
                val length = if (remaining > 224) 224 else remaining
                it.add(ByteArray(length + 3).apply {
                    ByteBuffer.wrap(this).apply {
                        put(BleConstantData.CMD_0x38)  //命令码
                        putShort(it.size.toShort()) //序列号从0开始
                        put(bytes, offset, length)
                        //put(176.toByte())  //固定写死
                    }
                })
                offset += length
            }
            //添加结束包
            it.add(ByteArray(4).apply {
                ByteBuffer.wrap(this).apply {
                    put(BleConstantData.CMD_0x38)
                    putShort((0xffff).toShort())
                    put(bytes.size.toBytes())  //校验和
                }
            })
        }
    }

    /**
     * 创建高速传递图片
     */
    fun createFastTransferBitmap(
        bitmap: Bitmap
    ): List<ByteArray> {
        val tempBytes = bitmap.toByteArrays()
        bitmap.recycle()
        return createFastTransferBitmap(tempBytes)
    }


    /**
     * 高速传输bin 准备
     */
    fun createFastTransferBinPrepare(size: Short): ByteArray {
        return ByteArray(6).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x39)
                put(byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xfe.toByte()))
                putShort(size) //数据帧的有效数据大小
            }
        }
    }

    /**
     * 传输bin文件
     */
    fun createFastTransferBin(bytes: ByteArray): List<ByteArray>{
        val list = mutableListOf<ByteArray>()
        var offset = 0
        while (offset < bytes.size) {
            val remaining = bytes.size - offset
            val length = if (remaining > 224) 224 else remaining
            list.add(ByteArray(length + 4).apply {
                ByteBuffer.wrap(this).apply {
                    put(BleConstantData.CMD_0x39)  //命令码
                    put(list.size.toBytesLowerThree()) //序列号从0开始
                    put(bytes, offset, length)
                }
            })
            offset += length
        }
        //添加结束包
        list.add(ByteArray(8).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x39)
                put(byteArrayOf(0xff.toByte(), 0xff.toByte(), 0xff.toByte()))
                putInt(bytes.size)  //校验和
            }
        })
        return list
    }

    /**
     * 传输bin文件
     */
    fun createFastTransferBin(
        path: String
    ): List<ByteArray> {
        val bytes = FileIOUtils.readFile2BytesByStream(path)
        return createFastTransferBin(bytes)
    }


    /**
     * 名片 发送预备
     */
    fun createCardPrepare(): ByteArray {
        return ByteArray(3).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x3b)
                putShort((0xfffe).toShort())
            }
        }
    }

    fun crateCard(
        bitmap: Bitmap
    ): List<ByteArray> {

        val list = mutableListOf<ByteArray>()
        val content = bitmap.toByteArrays()
        bitmap.recycle()
        var offset = 0
        while (offset < content.size) {
            val remaining = content.size - offset
            val length = if (remaining > 16) 16 else remaining
            list.add(
                ByteArray(length + 4).apply {
                    ByteBuffer.wrap(this).apply {
                        put(BleConstantData.CMD_0x3b)  //命令码
                        put(list.size.toBytesLowerThree()) //序列号从0开始
                        put(content, offset, length)
                    }
                }
            )
            offset += length
        }
        //结束
        list.add(
            ByteArray(4).apply {
                ByteBuffer.wrap(this).apply {
                    put(BleConstantData.CMD_0x3b)
                    put((0xffffff).toBytesLowerThree())
                }
            }
        )
        return list


    }


    /**
     * 传输运动轨迹 预备
     */
    fun createFastTransferTrackPrepare(): ByteArray {
        return ByteArray(9).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x3c)
                putShort(0xffe)
                putShort(0)  //数据帧有效数据大小
                putInt((System.currentTimeMillis() / 1000).toInt())
            }
        }
    }

    /**
     * 传输运动轨迹
     */
    fun createFastTransferTrack(
        bitmap: Bitmap
    ): List<ByteArray> {
        val list = mutableListOf<ByteArray>()
        val content = bitmap.toByteArrays()
        bitmap.recycle()
        var offset = 0
        while (offset < content.size) {
            val remaining = content.size - offset
            val length = if (remaining > 224) 224 else remaining
            list.add(
                ByteArray(length + 3).apply {
                    ByteBuffer.wrap(this).apply {
                        put(BleConstantData.CMD_0x3c)  //命令码
                        putShort(list.size.toShort()) //序列号从0开始
                        put(content, offset, length)
                    }
                }
            )
            offset += length
        }
        //结束
        list.add(ByteArray(4).apply {
            ByteBuffer.wrap(this).apply {
                put(BleConstantData.CMD_0x3c)
                put((0xffff).toBytesLowerThree())
            }
        })
        return list
    }


}
