package com.artillery.rwutils.cmd

/**
 * @author : zhiweizhu
 * create on: 2023/5/22 下午4:18
 */
object BleConstantData {




    /**
     * 设置日期时间
     */
    const val CMD_0x01: Int = 0x01
    const val CMD_SET_DATE_TIME_REPLY: Int = 0x81


    /**
     * 天气
     */
    const val CMD_0x05: Byte = 0x05

    /**
     * 查找手环
     */
    const val CMD_0x51: Byte = 0x51

    /**
     * 蓝牙拍照
     */
    const val CMD_0x52: Byte = 0x52

    /**
     * 蓝牙开始拍照
     */
    const val CMD_0xA2: Int = 0xA2

    /**
     * 获取电池电量
     */
    const val CMD_0x14: Byte = 0x14

    /**
     * 获取软件版本
     */
    const val CMD_0x1f: Byte = 0x1f

    /**
     * 获取步数
     */
    const val CMD_0x13: Byte = 0x13

    /**
     * 获取睡眠数据
     */
    const val CMD_0x15: Byte = 0x15

    /**
     * 获取心率、血压、血氧
     */
    const val CMD_0x16: Byte = 0x16

    /**
     * 血糖
     */
    const val CMD_0x18: Byte = 0x18


    /**
     * 停止心率实时测试和测试时间
     */
    const val CMD_0x60: Byte = 0x60

    /**
     * 紫外线大气压
     */
    const val CMD_0x04: Byte = 0x04

    /**
     * 设置身高体重性别
     * 设置表盘 摄氏度 华摄氏 英制公制等
     * 设置血糖相关
     * 设置久坐提醒,勿扰模式,喝水提醒 按照久坐、勿扰、喝水等顺序依次设置
     * 设置闹钟 最大设置数量3个
     * 设置通知开关
     */
    const val CMD_0x02: Byte = 0x02

    /**
     * 获取当前手环心率
     */
    const val CMD_0x17: Byte = 0x17

    /**
     * 开始传输图片前开始发送准备动作 以及发送动作
     */
    const val CMD_0x38: Byte = 0x38

    /**
     * 高速传输bin 准备 包括发送动作
     */
    const val CMD_0x39: Byte = 0x39

    /**
     * 读取久坐提醒 勿扰模式 喝水提醒
     * 读取闹钟
     * 读取各种开关 提醒 抬腕亮屏 心率检测开关
     * 读取表盘 温度单位 距离单位 华摄氏 英制 公制
     */
    const val CMD_0x62: Byte = 0x62

    /**
     * 同步联系人
     */
    const val CMD_0x66: Byte = 0x66

    /**
     * 获取屏幕规格
     */
    const val CMD_0x67: Byte = 0x67

    /**
     * 创建推送消息
     */
    const val CMD_0x73: Byte = 0x73

    /**
     * 名片 发送预备 以及发送
     */
    const val CMD_0x3b: Byte = 0x3b

    /**
     * 传输运动轨迹 预备 发送轨迹
     */
    const val CMD_0x3c: Byte = 0x3c


    const val HEAD_0X03: Byte = 0x03
    const val RESERVED_DEF_0X00: Byte = 0x00


    /**
     * 成功状态
     */
    const val SUCCESS_BLE_CODE: Int = 0x00

    /**
     * 失败状态
     */
    const val FAIL_BLE_CODE: Int = 0xEE


    const val REPLY_CMD_83: Int = 0x83

    const val REPLY_CMD_82: Int = 0x82

    /**
     * 查找手环返回的数据
     */
    const val REPLY_CMD_D1: Int = 0xD1

    /**
     * 蓝牙拍照
     */
    const val REPLY_CMD_D2: Int = 0xD2

    /**
     * 查找手机 手环--》 手机 这条命令码需要回复
     * {0xD3，status 1byte} = 2 size
     * status： 0x01 查找手机开   0x00 查找手机关 0xee 收到错误数据
     */
    const val REPLY_CMD_53: Int = 0x53

    /**
     * 设备发送实时数据(更新步数专用,手环步数有增加时会发指令给 APP)
     */
    const val REPLY_CMD_B3: Int = 0xB3

    /**
     * 电量信息
     */
    const val REPLY_CMD_94: Int = 0x94

    /**
     * 软件版本信息获取
     */
    const val REPLY_CMD_9F: Int = 0x9f




}