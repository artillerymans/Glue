package com.artillery.glue.ble.viewModels

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.artillery.connect.BleDeviceManager
import com.artillery.connect.BleHelper
import com.blankj.utilcode.util.LogUtils
import com.blankj.utilcode.util.Utils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import no.nordicsemi.android.ble.ktx.state.ConnectionState
import no.nordicsemi.android.ble.ktx.stateAsFlow

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午4:41
 */
class BleConnectViewModel: ViewModel() {



    private val bluetoothObserver by lazy(LazyThreadSafetyMode.NONE) {
        object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothAdapter.ACTION_STATE_CHANGED -> {
                        val bluetoothState = intent.getIntExtra(
                            BluetoothAdapter.EXTRA_STATE,
                            -1
                        )
                        when (bluetoothState) {
                            BluetoothAdapter.STATE_ON -> enableBleServices()
                            BluetoothAdapter.STATE_OFF -> disableBleServices()
                        }
                    }
                    BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                        val device =
                            intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                        LogUtils.d("Bond state changed for device ${device?.address}: ${device?.bondState}")
                        when (device?.bondState) {
                            BluetoothDevice.BOND_BONDED -> addDevice(device)
                            BluetoothDevice.BOND_NONE -> removeDevice(device)
                        }
                    }

                }
            }
        }
    }



    private fun enableBleServices() {
        val bluetoothManager = Utils.getApp().getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        if (bluetoothManager.adapter?.isEnabled == true) {
            LogUtils.d("Enabling BLE services")
            bluetoothManager.adapter.bondedDevices.forEach { device -> addDevice(device) }
        } else {
            LogUtils.d("Cannot enable BLE services as either there is no Bluetooth adapter or it is disabled")
        }
    }

    private fun disableBleServices() {
        BleHelper.getInstance().closeAll()
    }


    private var mCurrentDevice: BleDeviceManager? = null
    private val _connectStatusFlow = MutableStateFlow<ConnectionState?>(null)
    val connectStatusFlow: StateFlow<ConnectionState?> = _connectStatusFlow



    private fun addDevice(device: BluetoothDevice) {
        if (!BleHelper.getInstance().isContains(device.address)){
            mCurrentDevice = BleDeviceManager().also {
                it.connectDevice(device)
                viewModelScope.launch {
                    it.stateAsFlow().collect{ state ->
                        _connectStatusFlow.value = state
                    }
                }
                BleHelper.getInstance().saveBleDeviceManagerByKey(device.address, it)
            }
        }else {
            mCurrentDevice = BleHelper.getInstance().getBleDeviceManager(device.address)
            mCurrentDevice?.also {
                _connectStatusFlow.value = ConnectionState.Initializing
                viewModelScope.launch {
                    it.stateAsFlow().collect{
                        _connectStatusFlow.value = it
                    }
                }
                if (it.connectionState == BluetoothProfile.STATE_DISCONNECTED){
                    it.connectDevice(device)
                }
            }

        }
    }




    private fun removeDevice(device: BluetoothDevice) {
        BleHelper.getInstance().remove(device.address)?.close()
    }

    fun connect(device: BluetoothDevice){
        addDevice(device)
    }



    init {
        Utils.getApp().registerReceiver(bluetoothObserver, IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        })
    }


    override fun onCleared() {
        super.onCleared()
        Utils.getApp().unregisterReceiver(bluetoothObserver)
    }


}