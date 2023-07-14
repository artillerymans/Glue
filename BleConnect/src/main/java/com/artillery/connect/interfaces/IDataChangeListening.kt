package com.artillery.connect.interfaces

/**
 * @author : zhiweizhu
 * create on: 2023/7/14 上午9:59
 */
interface IDataChangeListening {
    fun onDataChange(type: Int, bytes: ByteArray)
}