package com.joaquin.mdmapp.view

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.joaquin.mdmapp.viewmodel.DashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current.applicationContext // Usar applicationContext para evitar leaks

    LaunchedEffect(Unit) {
        viewModel.initMonitoring(context)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopMonitoring(context)
        }
    }

    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()

    val deviceInfo = viewModel.getDeviceInfo()
    val storageInfo = viewModel.getStorageInfo()

    Column(modifier = Modifier.padding(16.dp)) {

        Spacer(modifier = Modifier.height(40.dp))

        Text("Device Info:", style = MaterialTheme.typography.titleLarge)
        Text("Model: ${deviceInfo.model}")
        Text("Manufacturer: ${deviceInfo.manufacturer}")
        Text("OS Version: ${deviceInfo.osVersion}")
        Text("Available Storage: $storageInfo")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Battery Status:", style = MaterialTheme.typography.titleLarge)
        Text("Level: ${batteryStatus.level}%")
        Text("Charging: ${if (batteryStatus.isCharging) "Sí" else "No"}")

        Spacer(modifier = Modifier.height(16.dp))

        Text("Network Status:", style = MaterialTheme.typography.titleLarge)
        Text("Connectivity: $networkStatus")

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { navController.navigate("apps") }) {
            Text("View Installed Apps")
        }
    }
}