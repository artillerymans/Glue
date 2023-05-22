package com.artillery.compose.utils

import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import kotlinx.coroutines.delay

/**
 * @author : zhiweizhu
 * create on: 2022/11/11 下午5:01
 * 点击防抖 可以防止任何的短时间的触发事件 不仅仅局限与点击事件
 */
@Composable
fun debouncedAction(waitMillis: Long = 250, action: () -> Unit): () -> Unit {
    var ready by rememberSaveable { mutableStateOf(true) }
    LaunchedEffect(ready, waitMillis, action) {
        delay(waitMillis)
        ready = true
    }

    return {
        if (ready) {
            action()
        }
        ready = false
    }
}