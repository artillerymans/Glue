package com.artillery.glue.ble

import android.content.Intent
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.artillery.compose.click
import com.artillery.compose.paddingVertical
import com.artillery.glue.R
import com.artillery.glue.ble.viewModels.BleConnectViewModel
import com.artillery.glue.model.DebugBaseItem
import com.artillery.glue.model.DebugDataType
import com.artillery.glue.ui.NavConstant
import com.artillery.rwutils.CreateDataFactory
import com.artillery.rwutils.exts.rgb888toRgb555
import com.artillery.rwutils.exts.scaleBitmap
import com.artillery.rwutils.model.AlarmChoiceDay
import com.artillery.rwutils.model.AlarmClock
import com.artillery.rwutils.model.BWeather
import com.artillery.rwutils.model.ContactsItem
import com.artillery.rwutils.model.DistanceUnit
import com.artillery.rwutils.model.NoticeType
import com.artillery.rwutils.model.ProcessDataRequest
import com.artillery.rwutils.model.SDD
import com.artillery.rwutils.model.TemperatureType
import com.artillery.rwutils.model.TemperatureUnit
import com.artillery.rwutils.model.WeatherType
import com.artillery.rwutils.type.Gender
import com.artillery.rwutils.type.SwitchType
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ResourceUtils
import com.blankj.utilcode.util.TimeUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.time.LocalDateTime


/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午4:39
 */

@Composable
fun BleMainCompose(navController: NavController, viewModel: BleConnectViewModel) {

    LogUtils.d("BleMainCompose: viewModel.hasCode = ${viewModel.hashCode()}")

    val scrollState = rememberScrollState()
    val connectState by viewModel.connectStatusFlow.collectAsStateWithLifecycle()

    val debugList by viewModel.readWriteListFlow.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    fun writeBytes(bytes: ByteArray) {
        viewModel.writeByteArray(bytes)
    }

    fun writeListBytes(list: List<ByteArray>) {
        viewModel.writeByteArrays(list)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "当前连接状态: ${connectState?.isConnected ?: false}",
            style = TextStyle(fontSize = 16.sp, fontWeight = FontWeight.Bold)
        )
        UnitRowLayout(
            "扫描设备",
            "断开设备",
            onFirstClick = {
                navController.navigate(NavConstant.Ble.Scant)
            },
            onSecondClick = {
                viewModel.disConnect()
            }
        )

        UnitRowLayout(
            "写入日期",
            "获取功能",
            onFirstClick = {
                scope.launch {
                    //写入日期
                    writeBytes(CreateDataFactory.createDateTime())
                }
            },
            onSecondClick = {
                //获取功能
                writeBytes(CreateDataFactory.createWatchFunctionList())
            }
        )

        UnitRowLayout(
            "写入用户信息",
            "读取用户信息",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createSettingUserInfo(
                        70,
                        33,
                        180.toByte(),
                        30,
                        Gender.man,
                        30_000
                    )
                )
            },
            onSecondClick = {

            }
        )

        UnitRowLayout(
            "常用通知打开",
            "通知全部关闭",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createSettingAppNoticeEnables(
                        wxNoticeEnable = SwitchType.ON,
                        qqNoticeEnable = SwitchType.ON,
                        otherAppEnable = SwitchType.ON,
                        heartRateWhileEnable = SwitchType.ON,
                        heartRateInterval = 3,
                        liftScreenLightEnable = SwitchType.ON,
                        smsNoticeEnable = SwitchType.ON,
                        incomingCallEnable = SwitchType.ON,
                        viberEnable = SwitchType.ON
                    )
                )
            },
            onSecondClick = {
                //默认全部关闭
                writeBytes(
                    CreateDataFactory.createSettingAppNoticeEnables()
                )
            }
        )


        UnitRowLayout(
            "闹钟5分钟后",
            "读取闹钟",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createSettingsAlarmClock(
                        mutableListOf(
                            AlarmClock(
                                SwitchType.ON,
                                ByteBuffer.wrap(ByteArray(2).apply {
                                    val localDateTime = LocalDateTime.now().plusMinutes(5)
                                    set(0, localDateTime.hour.toByte())
                                    set(1, localDateTime.minute.toByte())
                                }).short,
                                mutableListOf(
                                    AlarmChoiceDay.Monday(),
                                    AlarmChoiceDay.Tuesday(),
                                    AlarmChoiceDay.Wednesday(),
                                    AlarmChoiceDay.Thursday(),
                                    AlarmChoiceDay.Friday(),
                                    AlarmChoiceDay.Saturday(),
                                    AlarmChoiceDay.Sunday(),
                                )
                            ),
                            AlarmClock(
                                SwitchType.OFF,
                                ByteBuffer.wrap(ByteArray(2).apply {
                                    val localDateTime = LocalDateTime.now().plusMinutes(10)
                                    set(0, localDateTime.hour.toByte())
                                    set(1, localDateTime.minute.toByte())
                                }).short,
                                mutableListOf(
                                    AlarmChoiceDay.Monday(),
                                    AlarmChoiceDay.Tuesday(),
                                    AlarmChoiceDay.Wednesday(),
                                    AlarmChoiceDay.Thursday(),
                                    AlarmChoiceDay.Friday(),
                                    AlarmChoiceDay.Saturday(),
                                    AlarmChoiceDay.Sunday(),
                                )
                            ),
                            AlarmClock(
                                SwitchType.OFF,
                                ByteBuffer.wrap(ByteArray(2).apply {
                                    val localDateTime = LocalDateTime.now().plusHours(1)
                                    set(0, localDateTime.hour.toByte())
                                    set(1, localDateTime.minute.toByte())
                                }).short,
                                mutableListOf(
                                    AlarmChoiceDay.Monday(),
                                    AlarmChoiceDay.Tuesday(),
                                    AlarmChoiceDay.Wednesday(),
                                    AlarmChoiceDay.Thursday(),
                                    AlarmChoiceDay.Friday(),
                                    AlarmChoiceDay.Saturday(),
                                    AlarmChoiceDay.Sunday(),
                                )
                            )
                        )
                    )
                )
            },
            onSecondClick = {
                //默认全部关闭
                writeBytes(
                    CreateDataFactory.createReadAlarms()
                )
            }
        )


        UnitRowLayout(
            "久坐勿扰喝水",
            "读取久坐勿扰喝水",
            onFirstClick = {
                //按照久坐、勿扰、喝水等顺序依次设置
                writeBytes(
                    CreateDataFactory.createSettingRemind(
                        mutableListOf(
                            SDD.Sedentary(
                                SwitchType.ON,
                                60.toByte(),
                                9.toByte(),
                                0.toByte(),
                                18.toByte(),
                                30.toByte()
                            ),
                            SDD.DonTDisturb(
                                SwitchType.ON,
                                23.toByte(),
                                0.toByte(),
                                8.toByte(),
                                30.toByte()
                            ),
                            SDD.DrinkingWater(
                                SwitchType.ON,
                                60.toByte(),
                                9.toByte(),
                                0.toByte(),
                                18.toByte(),
                                30.toByte()
                            )
                        )
                    )
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createReadNoticeSettings()
                )
            }
        )


        UnitRowLayout(
            "查找手环开",
            "查找手环关",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createFindWatchDevice(SwitchType.ON)
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createFindWatchDevice(SwitchType.OFF)
                )
            }
        )

        UnitRowLayout(
            "当前电量",
            "获取版本",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createBatteryLevel()
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createSoftVersion()
                )
            }
        )

        UnitRowLayout(
            "昨天运动数据",
            "昨日睡眠数据",
            onFirstClick = {
                val localDateTime = LocalDateTime.now().minusDays(1L)
                writeBytes(
                    CreateDataFactory.createStepsByTime(
                        ProcessDataRequest.Steps(
                            localDateTime.year,
                            localDateTime.monthValue,
                            localDateTime.dayOfMonth
                        )
                    )
                )
            },
            onSecondClick = {
                val localDateTime = LocalDateTime.now().minusDays(1L)
                writeBytes(
                    CreateDataFactory.createSleepsByTime(
                        ProcessDataRequest.Sleeps(
                            localDateTime.year,
                            localDateTime.monthValue,
                            localDateTime.dayOfMonth
                        )
                    )
                )
            }
        )

        UnitRowLayout(
            "今日心率血压血氧",
            "读取通知开关",
            onFirstClick = {
                val localDateTime = LocalDateTime.now()
                writeBytes(
                    CreateDataFactory.createHeartRateByTime(
                        ProcessDataRequest.HeartRates(
                            localDateTime.year,
                            localDateTime.monthValue,
                            localDateTime.dayOfMonth
                        )
                    )
                )
            },
            onSecondClick = {
                //读取通知开关
                writeBytes(
                    CreateDataFactory.createReadNoticeEnables()
                )
            }
        )


        UnitRowLayout(
            "心率测量开",
            "心率测量关",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.ON,
                        0x00
                    )
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.OFF,
                        0x00
                    )
                )
            }
        )

        UnitRowLayout(
            "血压测量开",
            "血压测量关",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.ON,
                        0x01
                    )
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.OFF,
                        0x01
                    )
                )
            }
        )

        UnitRowLayout(
            "血氧测量开",
            "血氧测量关",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.ON,
                        0x02
                    )
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.OFF,
                        0x02
                    )
                )
            }
        )

        UnitRowLayout(
            "当前心率血压血氧",
            "-----",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createCurrentHeartRate()
                )
            },
            onSecondClick = {

            }
        )


        UnitRowLayout(
            "恢复出厂设置",
            "----",
            onFirstClick = {
                writeBytes(
                    CreateDataFactory.createResetFactorySetting()
                )
            },
            onSecondClick = {
                writeBytes(
                    CreateDataFactory.createSwitchListen(
                        SwitchType.OFF,
                        0x02
                    )
                )
            }
        )


        UnitRowLayout(
            "QQ推送",
            "微信推送",
            onFirstClick = {
                val whileText = (0..10).map { "QQ推送$it" }.joinToString("")
                val text = "${TimeUtils.getNowString()}->$whileText"
                writeListBytes(CreateDataFactory.createMessagePush(text, NoticeType.QQ()))
            },
            onSecondClick = {
                val whileText = (0..100).map { "微信推送$it" }.joinToString("")
                val text = "${TimeUtils.getNowString()}->$whileText"
                writeListBytes(CreateDataFactory.createMessagePush(text, NoticeType.QQ()))
            }
        )

        UnitRowLayout(
            "天气",
            "同步紫外线",
            onFirstClick = {
                val list = mutableListOf<BWeather>().apply {
                    //添加今天的天气
                    add(
                        BWeather.TodayWeather(
                            WeatherType.雷阵雨(),
                            TemperatureType.AboveZero(),  //零上
                            39,
                            TemperatureType.AboveZero(),
                            12,
                            TemperatureType.AboveZero(),
                            42
                        )
                    )
                    add(
                        BWeather.OtherDayWeather(
                            WeatherType.大雨(),
                            TemperatureType.AboveZero(),
                            19,
                            TemperatureType.AboveZero(),
                            41
                        )
                    )
                }
                writeBytes(CreateDataFactory.createWeather(list))
            },
            onSecondClick = {
                //同步紫外线
                writeBytes(CreateDataFactory.createUltravioletRaysAndPressure(
                    89.toByte(),
                    98.toShort(),
                    20.toByte()
                ))
            }
        )

        UnitRowLayout(
            "写入表盘温度单位",
            "读取表盘温度单位",
            onFirstClick = {
                writeBytes(CreateDataFactory.createSettingsClockDialUnit(
                    3,
                    TemperatureUnit.HuaCelsius(),
                    DistanceUnit.EnglishSystem()
                ))
            },
            onSecondClick = {
                //读取表盘温度单位
                writeBytes(CreateDataFactory.createReadClockDialUnit())
            }
        )


        UnitRowLayout(
            "同步联系人",
            "-----",
            onFirstClick = {

                writeListBytes(CreateDataFactory.createSyncContacts(
                    (0..80).map {
                        ContactsItem(
                            "三间隔$it",
                            "1762037$it"
                        )
                    }.toList()
                ))
            },
            onSecondClick = {
                //待定
            }
        )

        UnitRowLayout(
            "预备发送自定义背景",
            "发送自定义背景",
            onFirstClick = {
                //预备发送自定义背景图片
                writeBytes(CreateDataFactory.createFastTransferBitmapPrepare())
            },
            onSecondClick = {
                val bytes = ResourceUtils.readAssets2String("imges/test_bg.png").toByteArray()
                /*val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                val newBitmap = bitmap.scaleBitmap(240f, 240f)
                val pixels = IntArray(newBitmap.height * newBitmap.width)
                newBitmap.getPixels(pixels, 0, newBitmap.width, 0, 0, newBitmap.width, newBitmap.height)
                val buffer = ByteBuffer.allocate(pixels.size * 2).apply {
                    pixels.forEach { value ->
                        putShort(value.rgb888toRgb555().toShort())
                    }
                }*/
                //发送自定义背景图片
                writeListBytes(CreateDataFactory.createFastTransferBitmap(bytes))




            }
        )

        UnitRowLayout(
            "预备发送Bin",
            "发送Bin",
            onFirstClick = {
                //预备发送Bin
                val bytes = ResourceUtils.readAssets2String("bin/jeep_h1_1_7.bin").toByteArray()
                writeBytes(CreateDataFactory.createFastTransferBinPrepare(bytes.size.toShort()))
            },
            onSecondClick = {
                //发送Bin
                val bytes = ResourceUtils.readAssets2String("bin/jeep_h1_1_7.bin").toByteArray()
                writeListBytes(CreateDataFactory.createFastTransferBin(bytes))
            }
        )


        UnitRowLayout(
            "启动有礼",
            "启动想你",
            onFirstClick = {
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("txc_pay_store://com.txc.store/wqf")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Utils.getApp().startActivity(intent)
            },
            onSecondClick = {


                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse("txc_pay_agent://com.txc.agent/wqf")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                Utils.getApp().startActivity(intent)
            }
        )


        /*调试直观查看数据*/
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(600.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            items(debugList) {
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(4.dp)
                ) {
                    if (it is DebugBaseItem.DebugItem) {
                        Text(
                            text = "序号:${it.order}->${
                                when (it.type) {
                                    DebugDataType.write -> "写入指令:0x${it.hexCmd}"
                                    DebugDataType.notice -> "接收指令:0x${it.hexCmd}"
                                    else -> {}
                                }
                            }->${it.formatTime}",
                            style = TextStyle(fontSize = 12.sp, color = Color.LightGray)
                        )

                        Text(
                            text = "原始数据->${it.nativeData.contentToString()}",
                            style = TextStyle(fontSize = 12.sp, color = Color.LightGray)
                        )

                        Text(
                            text = "Hex->${it.hexString}",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        )
                    } else if (it is DebugBaseItem.PackItem) {

                        Text(
                            text = "Hex->${it.hexString}",
                            style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        )

                        Text(
                            text = "解包->${it.json}",
                            style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        )

                    }
                }
            }
        }
    }


}


@Composable
fun UnitRowLayout(
    firstTitle: String,
    secondTitle: String,
    onFirstClick: () -> Unit,
    onSecondClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = firstTitle,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .click {
                    onFirstClick.invoke()
                }
                .weight(1f)
                .height(IntrinsicSize.Min)
                .paddingVertical(10.dp)
        )

        Text(
            text = secondTitle,
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            ),
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(0.5.dp, Color.LightGray, RoundedCornerShape(8.dp))
                .click {
                    onSecondClick.invoke()
                }
                .weight(1f)
                .height(IntrinsicSize.Min)
                .paddingVertical(10.dp)
        )
    }
}



