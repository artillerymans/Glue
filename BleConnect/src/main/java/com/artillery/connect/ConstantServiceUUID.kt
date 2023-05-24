package com.artillery.connect

import java.util.UUID

/**
 * UUID 常量字段
 */
object ConstantServiceUUID {

    val MAIN_SERVICE_UUID: UUID = UUID.fromString("6E40FC00-B5A3-F393-E0A9-E50E24DCCA9E")

    val READ_NOTIFY_SERVICE_UUID = UUID.fromString("6E40FC21-B5A3-F393-E0A9-E50E24DCCA9E")

    val WRITE_SERVICE_UUID = UUID.fromString("6E40FC20-B5A3-F393-E0A9-E50E24DCCA9E")
}