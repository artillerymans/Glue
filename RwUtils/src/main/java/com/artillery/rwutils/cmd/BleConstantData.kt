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

    /**
     * 查找手环
     */
    const val CMD_FIND_WATCH_DEVICE: Byte = 0x51

    /**
     * 蓝牙拍照
     */
    const val CMD_TAKE_PHOTO: Byte = 0x52

    /**
     * 蓝牙开始拍照
     */
    const val CMD_START_TAKE_PHOTO: Int = 0xA2

    /**
     * 获取电池电量
     */
    const val CMD_BATTER_LEVEL: Byte = 0x14

    /**
     * 获取软件版本
     */
    const val CMD_SOFT_VERSION: Byte = 0x1f

    /**
     * 获取步数
     */
    const val CMD_GET_STEPS: Byte = 0x13

    /**
     * 获取睡眠数据
     */
    const val CMD_GET_SLEEPS: Byte = 0x15

    /**
     * 获取心率、血压、血氧
     */
    const val CMD_GET_HEART_RATES: Byte = 0x16

    /**
     * 血糖
     */
    const val CMD_GET_BLOOD: Byte = 0x18


    /**
     * 停止心率实时测试和测试时间
     */
    const val CMD_SWITCH_HEART_LISTEN: Byte = 0x60


    const val HEAD_0X03: Byte = 0x03
    const val RESERVED_DEF_0X00: Byte = 0x00



}