package com.artillery.scanner

import com.artillery.scanner.interfaces.IScanCall
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat
import no.nordicsemi.android.support.v18.scanner.ScanCallback
import no.nordicsemi.android.support.v18.scanner.ScanFilter
import no.nordicsemi.android.support.v18.scanner.ScanResult
import no.nordicsemi.android.support.v18.scanner.ScanSettings

/**
 * @author : zhiweizhu
 * create on: 2023/4/23 下午1:43
 * 蓝牙扫描管理
 */
class BTManagerScanner private constructor() {

    private val mScanner by lazy(LazyThreadSafetyMode.NONE) {
        BluetoothLeScannerCompat.getScanner()
    }

    private val mScanCallback by lazy(LazyThreadSafetyMode.NONE) {
        BleScantCallBack()
    }

    fun startScan(
        filter: List<ScanFilter> = emptyList(),
        settings: ScanSettings = ScanSettings.Builder().build(),
        call: IScanCall
    ) {
        mScanCallback.addICall(call)
        call.onScantStart()
        mScanner.startScan(filter, settings, mScanCallback)
    }

    fun stopScan(call: IScanCall? = null) {
        call?.let {
            mScanCallback.removeICall(it)
        }
        mScanner.stopScan(mScanCallback)
    }


    internal class BleScantCallBack: ScanCallback(){

        private val mList by lazy(LazyThreadSafetyMode.NONE){
            mutableListOf<IScanCall>()
        }

        fun addICall(call: IScanCall){
            if (mList.isEmpty()){
                mList.add(call)
            }else {
                if (!mList.contains(call)){
                    mList.add(call)
                }
            }
        }

        fun removeICall(call: IScanCall){
            if (mList.isNotEmpty()){
                mList.remove(call)
            }
        }


        override fun onBatchScanResults(results: MutableList<ScanResult>) {
            super.onBatchScanResults(results)
            results.forEach { result ->
                mList.forEach { item ->
                    item.onScantResult(-1, result.device)
                }
            }

        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            mList.forEach { item ->
                item.onScantFail(errorCode)
            }
        }

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            mList.forEach { item ->
                item.onScantResult(callbackType, result.device)
            }
        }
    }

}