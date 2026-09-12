package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.AppScreen
import com.example.ui.screens.CastScreen
import com.example.ui.screens.DeviceDiscoveryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CastViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: CastViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                when (currentScreen) {
                    AppScreen.HOME -> {
                        HomeScreen(viewModel = viewModel)
                    }
                    AppScreen.CAST -> {
                        BackHandler {
                            viewModel.navigateBack()
                        }
                        CastScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateBack() }
                        )
                    }
                    AppScreen.DEVICE_DISCOVERY -> {
                        BackHandler {
                            viewModel.navigateBack()
                        }
                        DeviceDiscoveryScreen(
                            viewModel = viewModel,
                            onNavigateBack = { viewModel.navigateBack() }
                        )
                    }
                }
            }
        }
    }
}
