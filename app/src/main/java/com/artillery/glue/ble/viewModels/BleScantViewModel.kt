package com.artillery.glue.ble.viewModels

import android.bluetooth.BluetoothDevice
import androidx.lifecycle.ViewModel
import com.artillery.glue.scant.createScanSettings
import com.blankj.utilcode.util.LogUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings

/**
 * @author : zhiweizhu
 * create on: 2023/4/23 下午3:56
 */
class BleScantViewModel: ViewModel() {

    private val _listDevicesFlow = MutableStateFlow(emptyList<BluetoothDevice>())
    val listDevicesFlow: StateFlow<List<BluetoothDevice>> = _listDevicesFlow


    private val mScanner by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothLeScannerCompat.getScanner()
    }

    /**
     * 扫描回调函数
     */
    private val mScanCallback by lazy(LazyThreadSafetyMode.NONE) {
        object : ScanCallback(){
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                addDeviceToList(result.device)

            }

            override fun onBatchScanResults(results: MutableList<ScanResult>) {
                super.onBatchScanResults(results)
                LogUtils.d("onBatchScanResults: -->")
                addDeviceToList(results.map { value -> value.device }.toMutableList())
            }

            override fun onScanFailed(errorCode: Int) {
                super.onScanFailed(errorCode)
                LogUtils.d("onScanFailed: errorCode = $errorCode")
            }
        }
    }

    private fun addDeviceToList(device: BluetoothDevice?){
        device?.let {
            val currentList = _listDevicesFlow.value.toMutableList()
            val tempList = currentList.filter { value -> value.address == it.address }
            if (tempList.isEmpty()){
                currentList.add(it)
                _listDevicesFlow.value = currentList
            }
        }
    }

    private fun addDeviceToList(list: MutableList<BluetoothDevice>){
        val currentList = _listDevicesFlow.value.toMutableList()
        _listDevicesFlow.value = currentList.subtract(list.toSet()).toMutableList()
    }


    private val mScantFilterList by lazy(LazyThreadSafetyMode.NONE) {
        mutableListOf<ScanFilter>()
    }

    private var mScanSetting: ScanSettings = createScanSettings()

    fun addScantFilter(filter: List<ScanFilter>,
                       clear: Boolean = true,
                       scanSettings: ScanSettings = createScanSettings()
    ){
        mScantFilterList.apply {
            if (clear){
                clear()
            }
            addAll(filter)
        }
        mScanSetting = scanSettings
    }


    private var mScanting = false

    fun startScant(){
        if (mScanting){
            return
        }
        mScanting = true
        mScanner.startScan(mScantFilterList.toList(), mScanSetting, mScanCallback)
    }


    fun stopScant(){
        mScanting = false
        mScanner.stopScan(mScanCallback)
    }


    override fun onCleared() {
        super.onCleared()
        stopScant()
    }


}