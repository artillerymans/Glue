package com.artillery.compose

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable

/**
 * @author : zhiweizhu
 * create on: 2023/5/4 下午5:18
 */

@Composable
inline fun <reified T> rememberMutableStateOf(value: T) = remember { mutableStateOf(value) }

@Composable
inline fun <reified T> rememberMutableStateListOf() = remember { mutableStateListOf<T>() }

@Composable
fun <T> rememberMutableSaveStateOf(value: T) = rememberSaveable() {
    mutableStateOf(value)
}