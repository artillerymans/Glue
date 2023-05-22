package com.artillery.compose

fun Int?.orEmpty(def: Int = 0): Int{
    return this ?: def
}

fun Double?.orEmpty(def: Double = 0.0): Double{
    return this ?: def
}

fun Long?.orEmpty(def: Long = 0L): Long{
    return this ?: def
}

fun String?.orEmptyStr(def: String = ""): String {
    return this ?: def
}



fun Int.toUserIDStr(): String {
    return this.toUInt().toString()
}



