package com.artillery.connect

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午5:02
 * 保存蓝牙管理器 一个管理器对应一个设备
 */
class BleHelper private constructor(){

    companion object{
        fun getInstance() = Helper.instance
    }

    private object Helper{
        val instance = BleHelper()
    }

    private val mBleManagerMap: LinkedHashMap<String, BleDeviceManager> by lazy(LazyThreadSafetyMode.NONE) {
        LinkedHashMap()
    }

    /**
     * 可以通过任意保存的字符获取管理器 不给的话直接是最后一个
     */
    fun getBleDeviceManager(key: String? = null): BleDeviceManager?{
        return if (key.isNullOrEmpty()){
            if (mBleManagerMap.values.isEmpty()){
                null
            }else {
                mBleManagerMap.values.last()
            }
        }else {
            mBleManagerMap[key]
        }
    }

    fun saveBleDeviceManagerByKey(key: String, manager: BleDeviceManager): Boolean{
        if (!mBleManagerMap.containsKey(key)){
            mBleManagerMap[key] = manager
        }
        return true
    }

    /**
     * 这个有可能保存失败 原因是没有连接上之前你的绑定的设备都是空的
     */
    fun saveBleDeviceManagerByMac(manager: BleDeviceManager): Boolean{
        return manager.bluetoothDevice?.let {
            if (!mBleManagerMap.containsKey(it.address)){
                mBleManagerMap[it.address] = manager
            }
            true
        } ?: false
    }

    fun remove(key: String): BleDeviceManager?{
        return mBleManagerMap.remove(key)
    }

    fun isContains(key: String): Boolean{
        return mBleManagerMap.containsKey(key)
    }

    fun closeAll(){
        mBleManagerMap.values.forEach { manager ->
            manager.close()
        }
        mBleManagerMap.clear()
    }

}