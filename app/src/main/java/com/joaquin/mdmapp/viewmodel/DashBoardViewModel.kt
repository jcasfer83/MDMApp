package com.joaquin.mdmapp.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaquin.mdmapp.model.BatteryStatus
import com.joaquin.mdmapp.model.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _batteryStatus = MutableStateFlow(BatteryStatus(0, false))
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus

    private val _networkStatus = MutableStateFlow("Unknown")
    val networkStatus: StateFlow<String> = _networkStatus

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL ?: "Unknown",
            manufacturer = Build.MANUFACTURER ?: "Unknown",
            osVersion = Build.VERSION.RELEASE ?: "Unknown"
        )
    }

    fun getStorageInfo(): String {
        val usable = Environment.getDataDirectory().usableSpace
        return "${usable / (1024 * 1024)} MB"
    }

    //TODO: review this feature
    fun updateBatteryStatus(context: Context) {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            viewModelScope.launch {
                _batteryStatus.emit(BatteryStatus(level, isCharging))
            }
        }
    }

    //TODO: review this feature
    fun updateNetworkStatus(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return
        val caps = cm.getNetworkCapabilities(network) ?: return
        val status = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WiFi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Mobile"
            else -> "None"
        }
        viewModelScope.launch {
            _networkStatus.emit(status)
        }
    }
}
