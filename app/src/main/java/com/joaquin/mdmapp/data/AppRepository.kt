package com.joaquin.mdmapp.data

import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import com.joaquin.mdmapp.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface AppRepository {
    suspend fun getInstalledApps(context: Context): List<AppInfo>
}

class DefaultAppRepository : AppRepository {
    override suspend fun getInstalledApps(context: Context): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm: PackageManager = context.packageManager
        try {
            val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

            packages.mapNotNull { appInfo ->
                val name = appInfo.loadLabel(pm).toString()
                val packageName = appInfo.packageName
                val icon = appInfo.loadIcon(pm)
                val version = try {
                    pm.getPackageInfo(packageName, 0).versionName ?: "Unknown"
                } catch (e: PackageManager.NameNotFoundException) {
                    Log.w("DefaultAppRepository", "Información del paquete no encontrada para $packageName: ${e.message}")
                    "Unknown"
                } catch (e: Exception) {
                    Log.e("DefaultAppRepository", "Error al obtener la versión del paquete $packageName: ${e.message}", e)
                    "Unknown"
                }
                AppInfo(name, packageName, version, icon)
            }.sortedBy { it.name }
        } catch (e: Exception) {
            Log.e("DefaultAppRepository", "Error al obtener las aplicaciones instaladas: ${e.message}", e)
            throw e
        }
    }
}