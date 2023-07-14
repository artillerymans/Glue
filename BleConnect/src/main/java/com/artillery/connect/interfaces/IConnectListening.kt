package com.artillery.connect.interfaces

import no.nordicsemi.android.ble.ktx.state.ConnectionState

/**
 * @author : zhiweizhu
 * create on: 2023/7/14 上午9:24
 */
interface IConnectListening {

    fun onConnectChange(state: ConnectionState)
}