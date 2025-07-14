package com.joaquin.mdmapp.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.util.Log
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

    private val _networkStatus = MutableStateFlow("Desconocido")
    val networkStatus: StateFlow<String> = _networkStatus

    private var batteryReceiver: BroadcastReceiver? = null
    private var networkCallback: ConnectivityManager.NetworkCallback? = null

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL ?: "Desconocido",
            manufacturer = Build.MANUFACTURER ?: "Desconocido",
            osVersion = Build.VERSION.RELEASE ?: "Desconocido"
        )
    }

    fun getStorageInfo(): String {
        val usable = Environment.getDataDirectory().usableSpace
        return "${usable / (1024 * 1024)} MB"
    }

    private fun updateBatteryStatusInternal(intent: Intent?) {
        intent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL

            viewModelScope.launch {
                Log.d("DashboardViewModel", "Nivel de batería: $level%, Cargando: $isCharging")
                _batteryStatus.emit(BatteryStatus(level, isCharging))
            }
        }
    }

    private fun updateNetworkStatusInternal(context: Context) {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork
        val caps = cm.getNetworkCapabilities(network)

        val status = when {
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
            caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Datos Móviles"
            else -> "Sin Conexión"
        }
        viewModelScope.launch {
            Log.d("DashboardViewModel", "Estado de la red: $status")
            _networkStatus.emit(status)
        }
    }

    fun initMonitoring(context: Context) {
        batteryReceiver?.let { context.unregisterReceiver(it) }

        batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                updateBatteryStatusInternal(intent)
            }
        }
        context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

        val initialBatteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        updateBatteryStatusInternal(initialBatteryIntent)

        networkCallback?.let {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            cm.unregisterNetworkCallback(it)
        }

        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: android.net.Network) {
                super.onAvailable(network)
                updateNetworkStatusInternal(context)
            }

            override fun onLost(network: android.net.Network) {
                super.onLost(network)
                updateNetworkStatusInternal(context)
            }

            override fun onCapabilitiesChanged(network: android.net.Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                updateNetworkStatusInternal(context)
            }
        }

        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            cm.registerDefaultNetworkCallback(networkCallback as ConnectivityManager.NetworkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            cm.registerNetworkCallback(request, networkCallback as ConnectivityManager.NetworkCallback)
        }
        updateNetworkStatusInternal(context)
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("DashboardViewModel", "ViewModel onCleared.")
    }

    fun stopMonitoring(context: Context) {
        batteryReceiver?.let {
            try {
                context.unregisterReceiver(it)
                Log.d("DashboardViewModel", "BroadcastReceiver de batería desregistrado.")
            } catch (e: IllegalArgumentException) {
                Log.e("DashboardViewModel", "Error al desregistrar BroadcastReceiver de batería: ${e.message}")
            }
            batteryReceiver = null
        }
        networkCallback?.let {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            try {
                cm.unregisterNetworkCallback(it)
                Log.d("DashboardViewModel", "NetworkCallback desregistrado.")
            } catch (e: IllegalArgumentException) {
                Log.e("DashboardViewModel", "Error al desregistrar NetworkCallback: ${e.message}")
            }
            networkCallback = null
        }
    }
}
