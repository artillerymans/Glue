package com.artillery.glue.ble

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.artillery.glue.ble.viewModels.BleScantViewModel

/**
 * @author : zhiweizhu
 * create on: 2023/4/23 下午3:54
 */

@Composable
fun BleScantCompose(nav: NavController) {
    val scantViewModel: BleScantViewModel = viewModel()
    Column(
        Modifier.fillMaxSize()
    ) {

        val list = listOf<String>("1122", "2222", "3333", "33333")

        LazyColumn(
            Modifier
                .fillMaxWidth()
                .weight(1f)){
            items(list){
                Text(text = it)
            }
        }
        Text(text = "开始扫描")
    }

}