package com.joaquin.mdmapp.viewmodel

import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.storage.StorageManager
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaquin.mdmapp.data.AndroidBatteryDataSource
import com.joaquin.mdmapp.data.AndroidNetworkDataSource
import com.joaquin.mdmapp.data.BatteryDataSource
import com.joaquin.mdmapp.data.NetworkDataSource
import com.joaquin.mdmapp.model.BatteryStatus
import com.joaquin.mdmapp.model.DeviceInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val batteryDataSource: BatteryDataSource = AndroidBatteryDataSource(),
    private val networkDataSource: NetworkDataSource = AndroidNetworkDataSource()
) : ViewModel() {

    private val _batteryStatus = MutableStateFlow(BatteryStatus(0, false))
    val batteryStatus: StateFlow<BatteryStatus> = _batteryStatus

    private val _networkStatus = MutableStateFlow("Desconocido")
    val networkStatus: StateFlow<String> = _networkStatus

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun getDeviceInfo(): DeviceInfo {
        return DeviceInfo(
            model = Build.MODEL ?: "Desconocido",
            manufacturer = Build.MANUFACTURER ?: "Desconocido",
            osVersion = Build.VERSION.RELEASE ?: "Desconocido"
        )
    }

    fun getStorageInfo(context: Context): String {
        val bytes: Long = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val storageManager = context.getSystemService(Context.STORAGE_SERVICE) as StorageManager
            try {
                val uuid = storageManager.getUuidForPath(context.filesDir)
                storageManager.getAllocatableBytes(uuid)
            } catch (e: Exception) {
                Log.e("DashboardViewModel", "Error al obtener espacio de almacenamiento: ${e.message}", e)
                Environment.getDataDirectory().usableSpace
            }
        } else {
            Environment.getDataDirectory().usableSpace
        }
        return "${bytes / (1024 * 1024)} MB"
    }

    fun initMonitoring(context: Context) {
        viewModelScope.launch {
            batteryDataSource.getBatteryStatusFlow(context)
                .catch { e ->
                    Log.e("DashboardViewModel", "Error al observar la batería: ${e.message}", e)
                    _error.emit("Error al obtener estado de batería: ${e.localizedMessage ?: "desconocido"}")
                }
                .collect { status ->
                    _batteryStatus.emit(status)
                }
        }

        viewModelScope.launch {
            networkDataSource.getNetworkStatusFlow(context)
                .catch { e ->
                    Log.e("DashboardViewModel", "Error al observar la red: ${e.message}", e)
                    _error.emit("Error al obtener estado de red: ${e.localizedMessage ?: "desconocido"}")
                }
                .collect { status ->
                    _networkStatus.emit(status)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d("DashboardViewModel", "ViewModel onCleared.")
    }
}
