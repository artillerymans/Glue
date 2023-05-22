package com.artillery.compose

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.artillery.compose.heightSpacer
import com.artillery.compose.utils.debouncedAction
import com.artillery.compose.widthSpacer


fun Modifier.paddingHorizontal(horizontal: Dp): Modifier {
    return padding(horizontal = horizontal)
}

fun Modifier.paddingVertical(vertical: Dp): Modifier {
    return padding(vertical = vertical)
}

fun Modifier.paddingStart(value: Dp): Modifier {
    return padding(start = value)
}

fun Modifier.paddingEnd(value: Dp): Modifier {
    return padding(end = value)
}

fun Modifier.paddingTop(value: Dp): Modifier {
    return padding(top = value)
}

fun Modifier.paddingBottom(value: Dp): Modifier {
    return padding(bottom = value)
}


@Composable
fun Int.SpacerHorizontal(){
    widthSpacer(this.dp)
}

@Composable
fun Int.SpacerVertical(){
    heightSpacer(this.dp)
}

/**
 * Create by zhiweizhu on 2022/5/14
 */

/*防抖*/
@Composable
fun Modifier.click(
    delay: Long = 500L,
    enabled: Boolean = true,
    indication: Indication? = LocalIndication.current,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    block: () -> Unit
) = composed {
    clickable(
        onClick = debouncedAction(waitMillis = delay, action = block),
        indication = indication,
        interactionSource = interactionSource,
        enabled = enabled
    )
}

/*防抖 没有点击效果*/
fun Modifier.clickNoEffect(
    indication: Indication? = null,
    interactionSource: MutableInteractionSource = MutableInteractionSource(),
    enabled: Boolean = true,
    waitMillis: Long = 500L,
    onClick: () -> Unit
) = composed {
    clickable(
        onClick = debouncedAction(waitMillis = waitMillis, action = onClick),
        indication = indication,
        interactionSource = interactionSource,
        enabled = enabled
    )
}

