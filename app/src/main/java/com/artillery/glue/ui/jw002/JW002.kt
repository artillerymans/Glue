package com.artillery.glue.ui.jw002

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.artillery.connect.base.ABaseBleManager
import com.artillery.connect.manager.JW002BleManage
import com.artillery.glue.ble.UnitRowLayout
import com.artillery.glue.ble.viewModels.JW002ConnectViewModel
import com.artillery.glue.model.DebugBaseItem
import com.artillery.glue.model.DebugDataType
import com.artillery.glue.ui.NavConstant
import com.artillery.protobuf.AlarmChoiceDay
import com.artillery.protobuf.Climate
import com.artillery.protobuf.DeviceModel
import com.artillery.protobuf.HealthDataType
import com.artillery.protobuf.MessageSwitch
import com.artillery.protobuf.MsgType
import com.artillery.protobuf.ProtoBufHelper
import com.artillery.protobuf.SwitchType
import com.artillery.protobuf.model.alarm_clock_t
import com.artillery.protobuf.utils.byte2Int
import com.artillery.protobuf.utils.createAlarmRepeat
import com.artillery.protobuf.utils.timeZone
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.temporal.TemporalField
import java.util.Calendar
import kotlin.experimental.or
import kotlin.random.Random

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 下午5:38
 */
@Composable
fun JW002Compose(navController: NavController, viewModel: JW002ConnectViewModel) {

    val scrollState = rememberScrollState()
    val connectState by viewModel.connectStatusFlow.collectAsStateWithLifecycle()

    val debugList by viewModel.readWriteListFlow.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()

    fun writeBytes(bytes: ByteArray, characteristicType: Int = ABaseBleManager.WRITE) {
        viewModel.writeByteArray(bytes, characteristicType)
    }

    fun writeListBytes(list: List<ByteArray>, characteristicType: Int = ABaseBleManager.WRITE) {
        viewModel.writeByteArrays(list, characteristicType)
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
            "获取基本信息",
            "绑定设备",
            onFirstClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_GET_BASE_PARAM())
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_BIND_DEVICE())
            }
        )

        UnitRowLayout(
            "获取设备信息",
            "发送手机信息到手表设备",
            onFirstClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_GET_DEVICE_INFO())
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_PHONE_INFO())
            }
        )

        UnitRowLayout(
            "设置短信通知开关",
            "发送短信消息",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_MESSAGE_SWITCH(
                        MessageSwitch.Sms(1)
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_MESSAGE_DATA(
                        MsgType.Sms, "短信", "我是测试用的消息${Random.nextInt(0, 10000)}"
                    )
                )
            }
        )

        UnitRowLayout(
            "获取心率配置信息",
            "同步最新心率值",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HR_CONFIG()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_HR_DETECT_VAL()
                )
            }
        )

        UnitRowLayout(
            "心率配置开",
            "心率配置关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_HR_CONFIG(
                        SwitchType.ON, 5, 180, 30
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_HR_CONFIG(
                        SwitchType.OFF, 5, 180, 30
                    )
                )
            }
        )

        UnitRowLayout(
            "获取血氧",
            "同步血氧",
            onFirstClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_GET_SPO2_CONFIG())
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SYNC_SPO2_DETECT_VAL())
            }
        )

        UnitRowLayout(
            "血氧开",
            "血氧关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_SPO2_CONFIG(
                        SwitchType.ON, 5, 180, 30
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_SPO2_CONFIG(
                        SwitchType.OFF, 5, 180, 30
                    )
                )
            }
        )



        UnitRowLayout(
            "获取压力",
            "同步压力",
            onFirstClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_GET_STRESS_CONFIG())
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SYNC_STRESS_DETECT_VAL())
            }
        )

        UnitRowLayout(
            "压力开",
            "压力关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_STRESS_CONFIG(
                        SwitchType.ON, 5
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_STRESS_CONFIG(
                        SwitchType.OFF, 5
                    )
                )
            }
        )

        UnitRowLayout(
            "获取久坐",
            "获取勿扰",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_LONG_SIT_CONFIG()
                )

            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_NOTDISTURB_CONFIG()
                )
            }
        )

        UnitRowLayout(
            "设置久坐开",
            "设置久坐关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_LONG_SIT_CONFIG(
                        SwitchType.ON, 30, 9, 30, 18, 30,
                        0
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_LONG_SIT_CONFIG(SwitchType.OFF)
                )
            }
        )

        UnitRowLayout(
            "设置勿扰开",
            "设置勿扰关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_NOTDISTURB_CONFIG(
                        SwitchType.ON, 1, 9, 30, 18, 30
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_NOTDISTURB_CONFIG(
                        SwitchType.OFF, 1
                    )
                )
            }
        )


        UnitRowLayout(
            "同步闹钟",
            "设置闹钟",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_CLOCK_ALARM_CONFIG()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_CLOCK_ALARM_CONFIG(
                        SwitchType.ON,
                        listOf(
                            ProtoBufHelper.getInstance().createAlarm(
                                1,
                                SwitchType.ON,
                                1,
                                createAlarmRepeat(
                                    AlarmChoiceDay.Monday,
                                    AlarmChoiceDay.Sunday
                                ),
                                8,
                                30,
                                "闹钟1"
                            )
                        )
                    )
                )
            }
        )

        UnitRowLayout(
            "同步喝水闹钟",
            "设置喝水闹钟",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_DRINK_ALARM_CONFIG()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_DRINK_ALARM_CONFIG(
                        SwitchType.ON,
                        listOf(
                            ProtoBufHelper.getInstance().createAlarm(
                                1,
                                SwitchType.ON,
                                1,
                                createAlarmRepeat(
                                    AlarmChoiceDay.Monday,
                                    AlarmChoiceDay.Sunday
                                ),
                                10, 30, "闹钟1"
                            )
                        )
                    )
                )
            }
        )

        UnitRowLayout(
            "同步吃药闹钟",
            "设置吃药闹钟",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_MEDI_ALARM_CONFIG()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_MEDI_ALARM_CONFIG(
                        SwitchType.ON,
                        listOf(
                            ProtoBufHelper.getInstance().createAlarm(
                                1,
                                SwitchType.ON,
                                0,
                                createAlarmRepeat(
                                    AlarmChoiceDay.Monday,
                                    AlarmChoiceDay.Wednesday
                                ),
                                10,
                                42,
                                "吃药闹钟"
                            )
                        )
                    )
                )
            }
        )


        UnitRowLayout(
            "设置国家信息",
            "设置时间",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_COUNTRY_INFO(
                        "中国",
                        timeZone()
                    )
                )
            },
            onSecondClick = {
                //当前时间基础上 + 2个小时
                val localDateTime = LocalDateTime.now().plusHours(2)
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_TIME_INFO(
                        localDateTime.year,
                        localDateTime.monthValue,
                        localDateTime.dayOfMonth,
                        localDateTime.hour,
                        localDateTime.minute,
                        localDateTime.second,
                        timeZone()
                    )
                )
            }
        )


        UnitRowLayout(
            "时间格式24",
            "时间格式12",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_TIME_FMT(0)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_TIME_FMT(1)
                )
            }
        )

        UnitRowLayout(
            "设置公制",
            "设置英制",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_METRIC_INCH(0)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_METRIC_INCH(1)
                )
            }
        )

        UnitRowLayout(
            "设置亮屏30s",
            "设置亮屏15s",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_BRIGHT_DURATION(30)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_BRIGHT_DURATION(15)
                )
            }
        )

        UnitRowLayout(
            "菜单蜂窝风格",
            "菜单瀑布流风格",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_MENU_STYLE(0)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_MENU_STYLE(1)
                )
            }
        )


        UnitRowLayout(
            "同步每天运动目标",
            "设置每天运动目标",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_DAY_SPORT_TARGET()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_DAY_SPORT_TARGET(
                        30000,
                        9000,
                        80000,
                        1800,
                        5
                    )
                )
            }
        )

        UnitRowLayout(
            "实时步数",
            "电量信息",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_ACTUAL_STEP_INFO()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_BATTERY_INFO()
                )
            }
        )

        UnitRowLayout(
            "找手表开",
            "找手表关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_RING_WATCH_CTRL_VALUE(SwitchType.ON)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_RING_WATCH_CTRL_VALUE(SwitchType.OFF)
                )
            }
        )

        UnitRowLayout(
            "设置天气信息",
            "--",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_WEATHER_INFO(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        "深圳",
                        listOf(
                            ProtoBufHelper.getInstance().createWeatherDay(
                                Climate.Sunny,
                                42,
                                45,
                                30,
                                38,
                                24,
                                30,
                                100
                            ),
                            ProtoBufHelper.getInstance().createWeatherDay(
                                Climate.LightRain,
                                41,
                                44,
                                30,
                                38,
                                24,
                                30,
                                100
                            ),
                            ProtoBufHelper.getInstance().createWeatherDay(
                                Climate.TStorm,
                                42,
                                46,
                                30,
                                38,
                                24,
                                30,
                                100
                            )
                        )
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().()
                )
            }
        )



        UnitRowLayout(
            "恢复出厂设置",
            "调试模式",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_DEVICE_MODE(DeviceModel.FactoryReset)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_DEVICE_MODE(DeviceModel.DeBug)
                )
            }
        )


        UnitRowLayout(
            "手机前台运行",
            "手机后台运行",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_PHONE_APP_SET_STATUS(1)
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_PHONE_APP_SET_STATUS(0)
                )
            }
        )

        UnitRowLayout(
            "获取Log记录",
            "获取表盘配置",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_LOG_INFO_DATA()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SYNC_DIAL_CONFIG_DATA()
                )
            }
        )


        UnitRowLayout(
            "同步步数",
            "同步卡路里",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.Step
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.Calorie
                    )
                )
            }
        )


        UnitRowLayout(
            "同步距离",
            "同步活动时长",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.Distance
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.ActivityDuration
                    )
                )
            }
        )

        UnitRowLayout(
            "同步活动次数",
            "同步心率",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.NumberActivities
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.HeartRate
                    )
                )
            }
        )


        UnitRowLayout(
            "同步血氧",
            "同步压力",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.BloodOxygen
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.Pressure
                    )
                )
            }
        )


        UnitRowLayout(
            "同步心率血氧",
            "同步心率血氧压力",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.HeartRateAndBloodOxygen
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.HeartRateBloodOxygenPressure
                    )
                )
            }
        )

        UnitRowLayout(
            "活动时长、次数",
            "步数卡路里距离",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.ActivityNumberDuration
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.StepsCaloriesDistance
                    )
                )
            }
        )


        UnitRowLayout(
            "同步除睡眠数据",
            "同步睡眠",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.All
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_HEALTH_DATA(
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH) + 1,
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH),
                        HealthDataType.Sleep1
                    )
                )
            }
        )

        UnitRowLayout(
            "判断多运动是否正在运行",
            "--",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_MUL_SPORT_IS_RUNNING()
                )
            },
            onSecondClick = {
                /*writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_NOTDISTURB_CONFIG()
                )*/
            }
        )


        UnitRowLayout(
            "--",
            "--",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_NOTDISTURB_CONFIG()
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_GET_NOTDISTURB_CONFIG()
                )
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