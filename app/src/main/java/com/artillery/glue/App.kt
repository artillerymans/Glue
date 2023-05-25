package com.artillery.glue

import android.app.Application
import com.blankj.utilcode.util.LogUtils

/**
 * @author : zhiweizhu
 * create on: 2023/5/25 下午3:21
 */
class App: Application() {

    override fun onCreate() {
        super.onCreate()

        LogUtils.getConfig().apply {
            setBorderSwitch(false)
            isLogHeadSwitch = false
        }
    }
}