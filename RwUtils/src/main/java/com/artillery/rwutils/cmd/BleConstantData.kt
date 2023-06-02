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
     * 传输自定背景图片
     */
    const val CMD_0x68: Byte = 0x68

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

    /**
     * 手机下发启动停止实时体温检测
     */
    const val CMD_0x6A: Byte = 0x6a

    /**
     * 获取设备体温检测历史数据
     */
    const val CMD_0x1A: Byte = 0x1a

    /**
     * 体温定时检测开关
     */
    const val CMD_0x6E: Byte = 0x6e

    /**
     * 同步手环运动记录
     */
    const val CMD_0x21: Byte = 0x21

    /**
     * 同步经纬度给手表
     */
    const val CMD_0x59: Byte = 0x59


    const val CMD_0x3a: Int = 0x3a


    /**
     * 获取手环功能
     */
    const val CMD_0X03: Byte = 0x03
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

    /**
     * 设置各类信息回复
     * 第1位 0x01 表示设置身高体重
     *
     */
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


    /**
     * 设置日期时间回复
     */
    const val REPLY_CMD_81: Int = 0x81

    /**
     * 通知紫外线数据回复
     */
    const val REPLY_CMD_84: Int = 0x84

    /**
     * 天气同步回复
     */
    const val REPLY_CMD_85: Int = 0x85

    /**
     * 根据日期获取的运动数据 index == 4 如果是0xff表示是最后一包
     */
    const val REPLY_CMD_93: Int = 0x93

    /**
     * 睡眠数据 index 1-2 Short  == 0xffff 表示是最后一包
     */
    const val REPLY_CMD_95: Int = 0x95

    /**
     * 血压心率血氧 index 1-2 Short  == 0xffff 表示是最后一包
     */
    const val REPLY_CMD_96: Int = 0x96

    /**
     * 血压心率血氧 启动或者停止
     */
    const val REPLY_CMD_E0: Int = 0xE0

    /**
     * 实时上报心率测量结果
     */
    const val REPLY_CMD_E1: Int = 0xE1

    /**
     * 手表接收到恢复出厂设置回复的
     */
    const val REPLY_CMD_F1: Int = 0xF1

    /**
     * 低电量通知
     */
    const val REPLY_CMD_72: Int = 0x72

    /**
     * 收到错误信息
     */
    const val REPLY_CMD_EE: Int = 0xEE

    /**
     * 获取当前心率血压血氧
     */
    const val REPLY_CMD_97: Int = 0x97

    /**
     * 第1位 == 0x02 获取App通知开关、抬腕亮屏、心率循环检测开关 检测间隔 分钟单位
     */
    const val REPLY_CMD_E2: Int = 0xE2

    /**
     * 同步到通讯录回复
     */
    const val REPLY_CMD_E6: Int = 0xE6

    /**
     * 获取手环屏幕规格 回复
     */
    const val REPLY_CMD_E7: Int = 0xE7

    /**
     * 自定义背景图 预备发送回复以及发送完成后的回复
     * 预备回复  1,2字段如果 == 0xfffe 表示预备发送的回复
     * 如果1，2字段是其他的则是发送完成的后的序列号
     *
     */
    const val REPLY_CMD_B8: Int = 0xB8

    /**
     * 高速传输表盘Bin 的回复 包括 回复预备动作
     * 1-3 如果 == 0xfffffe 表示回复的预备动作
     * 4 表示 0 表示失败 1 表示成功
     */
    const val REPLY_CMD_B9: Int = 0xB9

    /**
     * 手机下发启动停止实时体温检测 回复
     * 第1位 0x01：开启实时测量 0x00：停止实时测量
     */
    const val REPLY_CMD_EA: Int = 0xea

    /**
     * 实时上报体温检测数据
     */
    const val REPLY_CMD_EB: Int = 0xEB

    /**
     * 获取设备体温检测历史数据 回复
     */
    const val REPLY_CMD_9A: Int = 0x9a

    /**
     * 实时更新当前正启动的运动模式数据
     */
    const val REPLY_CMD_A0: Int = 0xA0

    /**
     * 同步手环运动记录 回复
     */
    const val REPLY_CMD_A1: Int = 0xA1

    /**
     * 手环请求经纬度 跟发送的命令码是一样的
     */
    const val REPLY_CMD_0x59: Int = 0x59

    /**
     * 蓝牙拍照
     */
    const val REPLY_CMD_0xA2: Int = 0xA2

    /**
     * 收款码 预备动作回复
     * index 3  如果0 表示失败  1 表示成功
     */
    const val REPLY_CMD_BA: Int = 0xBA






}