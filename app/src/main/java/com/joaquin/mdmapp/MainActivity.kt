package com.joaquin.mdmapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.joaquin.mdmapp.ui.theme.MDMAppTheme
import com.joaquin.mdmapp.viewmodel.DashboardViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: DashboardViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.updateBatteryStatus(this)
        viewModel.updateNetworkStatus(this)
        setContent {
            MDMAppTheme {
                AppNavigation()
            }
        }
    }
}





