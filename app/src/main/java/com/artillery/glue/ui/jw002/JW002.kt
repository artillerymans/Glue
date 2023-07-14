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
import com.artillery.glue.ble.UnitRowLayout
import com.artillery.glue.ble.viewModels.JW002ConnectViewModel
import com.artillery.glue.model.DebugBaseItem
import com.artillery.glue.model.DebugDataType
import com.artillery.glue.ui.NavConstant
import com.artillery.protobuf.FactoryProto

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
            "获取信息",
            "-----",
            onFirstClick = {
                viewModel.writeByteArrays(FactoryProto().createBytes())
            },
            onSecondClick = {
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