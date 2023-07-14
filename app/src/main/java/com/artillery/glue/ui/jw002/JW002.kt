package com.artillery.glue.ui.jw002

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.artillery.glue.ble.UnitRowLayout
import com.artillery.glue.ble.viewModels.JW002ConnectViewModel
import com.artillery.glue.ui.NavConstant

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
    }
}