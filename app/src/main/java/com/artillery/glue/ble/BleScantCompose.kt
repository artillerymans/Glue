package com.artillery.glue.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.artillery.compose.click
import com.artillery.compose.heightSpacerLine
import com.artillery.compose.paddingHorizontal
import com.artillery.glue.ble.viewModels.BleScantViewModel
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.ToastUtils

/**
 * @author : zhiweizhu
 * create on: 2023/4/23 下午3:54
 */


@SuppressLint("MissingPermission")
@Composable
fun BleScantCompose(nav: NavController, onSelectDevice: (BluetoothDevice) -> Unit) {
    val scantViewModel: BleScantViewModel = viewModel()
    LogUtils.d("BleScantCompose: viewMode.hasCode = ${scantViewModel.hashCode()}")
    Column(
        Modifier.fillMaxSize()
    ) {

        val listDevices by scantViewModel.listDevicesFlow.collectAsStateWithLifecycle()
        DisposableEffect(nav.hashCode()){

            onDispose {
                scantViewModel.stopScant()
            }
        }


        val requestPermissionLauncher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { result ->
            result.map { value ->
                LogUtils.d("BleScantCompose: ${value.key}, ${value.value}")
                if (Manifest.permission.ACCESS_COARSE_LOCATION == value.key){
                    LogUtils.d("BleScantCompose: 存在匹配关键权限")
                }
            }

            val tempList = result.filter { it.value }

            //说明权限全部被授予
            LogUtils.d("BleScantCompose: tempList.size = ${tempList.size}, result.values.size = ${result.values.size}")
            if (tempList.size == result.values.size) {
                scantViewModel.startScant()
            } else {
                val q1 = result.firstNotNullOfOrNull { value -> if (value.key == Manifest.permission.ACCESS_COARSE_LOCATION) value.value else false } ?: false
                LogUtils.d("BleScantCompose: q1 = $q1")
                if (q1) {
                    scantViewModel.startScant()
                } else {
                    ToastUtils.showShort("权限被拒绝")
                    scantViewModel.startScant()
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            LazyColumn(
                Modifier
                    .fillMaxWidth()
                    .height(maxHeight)
            ) {
                items(listDevices) {
                    Column(
                        modifier = Modifier
                            .click {
                                onSelectDevice.invoke(it)
                            }
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .paddingHorizontal(10.dp),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {

                        Text(
                            text = it.name.orEmpty().ifEmpty { "未知名称" },
                            style = TextStyle(fontSize = 18.sp)
                        )

                        Text(
                            text = it.address,
                            style = TextStyle(fontSize = 14.sp)
                        )

                    }

                    heightSpacerLine(2.dp)

                }
            }
        }

        Text(
            text = "开始扫描",
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Green)
                .click {
                    requestPermissionLauncher.launch(
                        arrayListOf<String>()
                            .apply {
                                add(Manifest.permission.BLUETOOTH)
                                add(Manifest.permission.ACCESS_COARSE_LOCATION)

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                    add(Manifest.permission.ACCESS_FINE_LOCATION)
                                }

                                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                                    add(Manifest.permission.BLUETOOTH_ADMIN)
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    add(Manifest.permission.BLUETOOTH_SCAN)
                                    add(Manifest.permission.BLUETOOTH_CONNECT)
                                }
                            }
                            .toTypedArray()
                    )

                }
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
                .padding(vertical = 10.dp),
            style = TextStyle(
                color = Color.White, fontSize = 16.sp,
                fontWeight = FontWeight.Bold, textAlign = TextAlign.Center
            )
        )
    }

}

