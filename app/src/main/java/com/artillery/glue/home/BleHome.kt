package com.artillery.glue.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.artillery.compose.click
import com.artillery.glue.ui.NavConstant

/**
 * @author : zhiweizhu
 * create on: 2023/7/13 下午5:22
 */

@Composable
fun BleHome(navController: NavController) {
    Column(modifier = Modifier.padding(10.dp).fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "JW002", modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(5.dp))
                    .click {
                        navController.navigate(NavConstant.Ble.JW002)
                    }
                    .padding(vertical = 8.dp)
                    .weight(1f)
                    .height(IntrinsicSize.Min),
                textAlign = TextAlign.Center
            )
            Text(
                text = "H1",
                modifier = Modifier
                    .clip(RoundedCornerShape(5.dp))
                    .border(1.dp, Color.DarkGray, RoundedCornerShape(5.dp))
                    .click {
                        navController.navigate(NavConstant.Ble.Tools)
                    }
                    .padding(vertical = 8.dp)
                    .weight(1f)
                    .height(IntrinsicSize.Min),
                textAlign = TextAlign.Center
            )
        }

    }
}