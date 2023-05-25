package com.artillery.glue.ble

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
import androidx.compose.material3.ElevatedButton
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.artillery.compose.click
import com.artillery.compose.paddingVertical
import com.artillery.glue.ble.viewModels.BleConnectViewModel
import com.artillery.glue.model.DebugDataType
import com.artillery.glue.ui.NavConstant
import com.artillery.rwutils.CreateDataFactory
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.launch

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
            "读取日期",
            onFirstClick = {
                scope.launch {
                    viewModel.writeByteArray(CreateDataFactory.createDateTime())
                }
            },
            onSecondClick = {

            }
        )

        UnitRowLayout(
            "写入用户信息",
            "读取用户信息",
            onFirstClick = {

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
        ){
            items(debugList){
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                        .fillMaxWidth()
                        .height(IntrinsicSize.Min)
                        .padding(4.dp)
                ) {
                    Text(text = "序号:${it.order}->${
                        when(it.type){
                            DebugDataType.write -> "写入指令:0x${it.hexCmd}"
                            DebugDataType.notice -> "接收指令:0x${it.hexCmd}"
                        }
                    }->${it.formatTime}",
                        style = TextStyle(fontSize = 12.sp, color = Color.LightGray)
                    )

                    Text(
                        text = "原始数据->${it.nativeData.contentToString()}",
                        style = TextStyle(fontSize = 12.sp, color = Color.LightGray))

                    Text(text = "Hex->${it.hexString}", style = TextStyle(fontSize = 15.sp, fontWeight = FontWeight.Bold))
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
    onSecondClick: () -> Unit
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



