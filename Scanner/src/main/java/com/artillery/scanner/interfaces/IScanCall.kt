package com.artillery.scanner.interfaces

import android.bluetooth.BluetoothDevice

/**
 * @author : zhiweizhu
 * create on: 2023/4/23 下午3:04
 */
interface IScanCall {

    fun onScantStart()

    fun onScantResult(callType: Int, device: BluetoothDevice)

    fun onScantFail(error: Int)

}