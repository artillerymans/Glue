package com.artillery.rwutils.cmd

/**
 * @author : zhiweizhu
 * create on: 2023/5/22 下午4:18
 */
object BleConstantData {

    /**
     * 设置日期时间
     */
    const val CMD_SET_DATE_TIME: Int = 0x01
    const val CMD_SET_DATE_TIME_REPLY: Int = 0x81

    /**
     * 设备设置信息
     */
    const val CMD_SET_USER_INFO: Int = 0x02


    const val HEAD_0X03: Byte = 0x03
    const val RESERVED_DEF_0X00: Byte = 0x00



}