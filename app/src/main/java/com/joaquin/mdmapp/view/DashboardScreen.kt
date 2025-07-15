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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.joaquin.mdmapp.viewmodel.DashboardViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.joaquin.mdmapp.R

@Composable
fun DashboardScreen(navController: NavController, viewModel: DashboardViewModel = viewModel()) {
    val context = LocalContext.current.applicationContext

    LaunchedEffect(Unit) {
        viewModel.initMonitoring(context)
    }

    val batteryStatus by viewModel.batteryStatus.collectAsState()
    val networkStatus by viewModel.networkStatus.collectAsState()
    val error by viewModel.error.collectAsState()

    val deviceInfo = viewModel.getDeviceInfo()
    val storageInfo = viewModel.getStorageInfo(context)

    Column(modifier = Modifier.padding(16.dp)) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(stringResource(id = R.string.terminal_info_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(id = R.string.device_model, deviceInfo.model))
        Text(stringResource(id = R.string.device_manufacturer, deviceInfo.manufacturer))
        Text(stringResource(id = R.string.device_os_version, deviceInfo.osVersion))
        Text(stringResource(id = R.string.storage_available, storageInfo))

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(id = R.string.battery_status_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(id = R.string.battery_level, batteryStatus.level))
        Text(stringResource(id = R.string.battery_charging, if (batteryStatus.isCharging) stringResource(id = R.string.yes) else stringResource(id = R.string.no)))

        Spacer(modifier = Modifier.height(16.dp))

        Text(stringResource(id = R.string.network_status_title), style = MaterialTheme.typography.titleLarge)
        Text(stringResource(id = R.string.network_connectivity, networkStatus))

        Spacer(modifier = Modifier.height(16.dp))

        error?.let {
            Text(stringResource(id = R.string.error_message, it), color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(onClick = { navController.navigate("apps") }) {
            Text(stringResource(id = R.string.installed_applications_button))
        }
    }
}