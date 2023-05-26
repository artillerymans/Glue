package com.artillery.glue.ble

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.artillery.glue.ble.viewModels.BleConnectViewModel
import com.blankj.utilcode.util.LogUtils

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午4:09
 */
@Composable
fun BleConnectCompose(nav: NavController, device: BluetoothDevice? = null, viewModel: BleConnectViewModel) {
    LogUtils.d("BleConnectCompose: viewModel.hasCode = ${viewModel.hashCode()}")
    val state by viewModel.connectStatusFlow.collectAsStateWithLifecycle()


    device?.let { value ->
        LaunchedEffect(nav.hashCode()){
            viewModel.connect(device)
        }

        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
            Text(
                text = if (state?.isConnected == true) "已连接" else "未连接",
                style = TextStyle(fontSize = 16.sp)
            )
        }

    }
}