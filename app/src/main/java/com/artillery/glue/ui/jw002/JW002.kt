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
import com.artillery.protobuf.MessageSwitch
import com.artillery.protobuf.MsgType
import com.artillery.protobuf.ProtoBufHelper
import com.artillery.protobuf.SwitchType
import com.artillery.protobuf.model.alarm_clock_t
import com.artillery.protobuf.utils.byte2Int
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
        viewModel.writeByteArray(bytes,characteristicType)
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
    ){
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
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_MESSAGE_SWITCH(
                    MessageSwitch.Sms(1)
                ))
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_MESSAGE_DATA(
                    MsgType.Sms, "短信", "我是测试用的消息${Random.nextInt(0, 10000)}"
                ))
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
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_HR_CONFIG(
                    SwitchType.ON.value, 5, 180, 30
                ))
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_HR_CONFIG(
                    SwitchType.OFF.value, 5, 180, 30
                ))
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
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_SPO2_CONFIG(
                    SwitchType.ON.value, 5, 180, 30
                ))
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_SPO2_CONFIG(
                    SwitchType.OFF.value, 5, 180, 30
                ))
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
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_STRESS_CONFIG(
                    SwitchType.ON.value, 5
                ))
            },
            onSecondClick = {
                writeListBytes(ProtoBufHelper.getInstance().sendCMD_SET_STRESS_CONFIG(
                    SwitchType.OFF.value, 5
                ))
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
                        SwitchType.ON.value, 30, 9, 30, 18, 30,
                        0
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_LONG_SIT_CONFIG(SwitchType.OFF.value)
                )
            }
        )

        UnitRowLayout(
            "设置勿扰开",
            "设置勿扰关",
            onFirstClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_NOTDISTURB_CONFIG(
                        1, 1, 9, 30, 18, 30
                    )
                )
            },
            onSecondClick = {
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_NOTDISTURB_CONFIG(
                        0,1
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
                val repeate = listOf(
                    AlarmChoiceDay.Monday,
                    AlarmChoiceDay.Sunday,
                ).map { value -> value.byte }.fold(0.toByte()) { acc, day -> acc or day.toByte() }.byte2Int()
                writeListBytes(
                    ProtoBufHelper.getInstance().sendCMD_SET_CLOCK_ALARM_CONFIG(
                        SwitchType.ON.value,
                        listOf(
                            ProtoBufHelper.getInstance().createAlarm(
                                1, SwitchType.ON.value, 1,
                                repeate, 8, 30, "闹钟1"
                            )
                        )
                    )
                )
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