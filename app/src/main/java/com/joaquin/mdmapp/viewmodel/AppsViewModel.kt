package com.joaquin.mdmapp.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaquin.mdmapp.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppsViewModel : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadInstalledApps(context: Context) {
        viewModelScope.launch {
            _isLoading.emit(true)

            val pm: PackageManager = context.packageManager

            val packages = withContext(Dispatchers.IO) {
                pm.getInstalledApplications(PackageManager.GET_META_DATA)
            }

            val loadedApps = mutableListOf<AppInfo>()
            val batchSize = 20
            var processedCount = 0

            for (it in packages) {
                val name = it.loadLabel(pm).toString()
                val packageName = it.packageName
                val icon = it.loadIcon(pm)
                val version = try {
                    pm.getPackageInfo(packageName, 0).versionName ?: "Unknown"
                } catch (e: Exception) {
                    "Unknown"
                }

                loadedApps.add(AppInfo(name, packageName, version, icon))
                processedCount++

                if (processedCount % batchSize == 0 || processedCount == packages.size) {
                    _installedApps.emit(loadedApps.sortedBy { app -> app.name }.toList())
                    delay(50)
                }
            }
            _isLoading.emit(false)
        }
    }
    fun registerAppChangeReceiver(context: Context) {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addDataScheme("package")
        }

        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                loadInstalledApps(context)
            }
        }, filter)
    }
}
