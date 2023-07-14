package com.artillery.protobuf

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 上午11:09
 */
class ProtoBufHelper private constructor(){

    companion object{
        fun getInstance() = Helper.instance
    }


    private object Helper{
        val instance = ProtoBufHelper()
    }





}



