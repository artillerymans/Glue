package com.artillery.glue

import android.bluetooth.BluetoothDevice
import android.os.Bundle
import android.os.ParcelUuid
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.artillery.compose.rememberMutableSaveStateOf
import com.artillery.compose.rememberMutableStateOf
import com.artillery.glue.ble.BleConnectCompose
import com.artillery.glue.ble.BleMainCompose
import com.artillery.glue.ble.BleScantCompose
import com.artillery.glue.ble.viewModels.BleConnectViewModel
import com.artillery.glue.ui.NavConstant
import com.artillery.glue.ui.theme.GlueTheme

class MainActivity : ComponentActivity() {

    val mConnectViewModel by viewModels<BleConnectViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GlueTheme {
                val navController = rememberNavController()
                var selectDevice = rememberMutableStateOf<BluetoothDevice?>(value = null)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = NavConstant.Ble.Tools
                    ){

                        composable(NavConstant.Ble.Tools){
                            BleMainCompose(navController, mConnectViewModel)
                        }

                        composable(NavConstant.Ble.Scant){
                            BleScantCompose(nav = navController){
                                selectDevice.value = it
                                navController.navigate(NavConstant.Ble.ConnectBle)
                            }
                        }

                        composable(NavConstant.Ble.ConnectBle){
                            BleConnectCompose(nav = navController, selectDevice.value, mConnectViewModel)
                        }
                    }
                }
            }
        }
    }
}


