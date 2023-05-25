package com.artillery.connect.splitter

import no.nordicsemi.android.ble.data.DataSplitter

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 上午9:49
 */
class LongDataSplitter: DataSplitter {

    override fun chunk(message: ByteArray, index: Int, maxLength: Int): ByteArray? {
        TODO("Not yet implemented")
    }
}