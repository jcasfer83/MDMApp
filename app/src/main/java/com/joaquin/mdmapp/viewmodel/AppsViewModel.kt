package com.joaquin.mdmapp.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaquin.mdmapp.data.AppRepository
import com.joaquin.mdmapp.data.DefaultAppRepository
import com.joaquin.mdmapp.model.AppInfo
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AppsViewModel(
    private val appRepository: AppRepository = DefaultAppRepository()
) : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _isLoading.emit(true)
            _error.emit(null)

            try {
                val allApps = appRepository.getInstalledApps(context)

                val batchSize = 20
                val loadedApps = mutableListOf<AppInfo>()
                var processedCount = 0

                for (appInfo in allApps) {
                    loadedApps.add(appInfo)
                    processedCount++

                    if (processedCount % batchSize == 0 || processedCount == allApps.size) {
                        _installedApps.emit(loadedApps.toList())
                        delay(50)
                    }
                }

                if (allApps.isNotEmpty() && processedCount < batchSize) {
                    _installedApps.emit(loadedApps.toList())
                } else if (allApps.isEmpty()) {
                    _installedApps.emit(emptyList())
                }

            } catch (e: Exception) {
                Log.e("AppsViewModel", "Error al cargar las aplicaciones instaladas: ${e.message}", e)
                _error.emit("Error al cargar las aplicaciones: ${e.localizedMessage ?: "Error desconocido"}")
                _installedApps.emit(emptyList())
            } finally {
                _isLoading.emit(false)
            }
        }
    }
}
