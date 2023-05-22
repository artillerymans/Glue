package com.artillery.rwutils.exts

import com.artillery.rwutils.type.SwitchType


fun SwitchType.toByte(): Byte{
    return if (this == SwitchType.OFF) 0x00 else 0x01
}