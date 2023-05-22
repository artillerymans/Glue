package com.artillery.compose

import androidx.compose.foundation.background

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height

import androidx.compose.foundation.layout.width

import androidx.compose.runtime.Composable

import androidx.compose.ui.Modifier

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * @author : zhiweizhu
 * create on: 2023/5/4 下午5:20
 */
@Composable
fun widthSpacer(dp: Dp) {
    Spacer(Modifier.width(dp))
}

@Composable
fun heightSpacer(dp: Dp) {
    Spacer(Modifier.height(dp))
}

@Composable
fun widthSpacerLine(
    dp: Dp = 1.dp,
    color: Color = Color.DarkGray,
) {
    Spacer(
        modifier = Modifier
            .width(dp)
            .fillMaxHeight()
            .background(color)
    )
}

@Composable
fun heightSpacerLine(dp: Dp = 10.dp, color: Color = Color.DarkGray) {
    Spacer(
        modifier = Modifier
            .fillMaxWidth()
            .height(dp)
            .background(color)
    )
}
