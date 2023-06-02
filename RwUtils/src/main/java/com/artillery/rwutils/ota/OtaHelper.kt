package com.artillery.rwutils.ota

import com.blankj.utilcode.util.Utils
import npble.nopointer.ota.NpFirmType
import npble.nopointer.ota.NpOtaHelper
import npble.nopointer.ota.callback.NpOtaCallback

/**
 * @author : zhiweizhu
 * create on: 2023/5/29 上午11:19
 */
class OtaHelper {


    fun startOta(
        path: String,
        mac: String,
    ){

        NpOtaHelper.getInstance().startOTA(Utils.getApp(), path, mac, NpFirmType.XR, object : NpOtaCallback(){
            override fun onFailure(code: Int, message: String?) {
                TODO("Not yet implemented")
            }

            override fun onSuccess() {
                TODO("Not yet implemented")
            }

            override fun onProgress(progress: Int) {
                TODO("Not yet implemented")
            }
        })
    }
}