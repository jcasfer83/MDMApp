package com.joaquin.mdmapp.viewmodel

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.joaquin.mdmapp.model.AppInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import android.content.BroadcastReceiver

class AppsViewModel : ViewModel() {

    private val _installedApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val installedApps: StateFlow<List<AppInfo>> = _installedApps

    fun loadInstalledApps(context: Context) {
        val pm: PackageManager = context.packageManager
        val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

        val apps = packages.mapNotNull {
            val name = it.loadLabel(pm)?.toString() ?: return@mapNotNull null
            val packageName = it.packageName
            val icon = it.loadIcon(pm)
            val version = try {
                pm.getPackageInfo(packageName, 0).versionName ?: "Unknown"
            } catch (e: Exception) {
                "Unknown"
            }

            AppInfo(name, packageName, version, icon)
        }.sortedBy { it.name }

        viewModelScope.launch {
            _installedApps.emit(apps)
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