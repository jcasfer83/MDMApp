package com.joaquin.mdmapp.data

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.util.Log
import com.joaquin.mdmapp.model.BatteryStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

interface BatteryDataSource {
    fun getBatteryStatusFlow(context: Context): Flow<BatteryStatus>
}

class AndroidBatteryDataSource : BatteryDataSource {

    override fun getBatteryStatusFlow(context: Context): Flow<BatteryStatus> = callbackFlow {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                    val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                            status == BatteryManager.BATTERY_STATUS_FULL
                    trySend(BatteryStatus(level, isCharging))
                    Log.d("BatteryDataSource", "Batería actualizada: Nivel=$level%, Cargando=$isCharging")
                }
            }
        }

        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, filter)

        val initialIntent = context.registerReceiver(null, filter)
        initialIntent?.let {
            val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            val status = it.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
            val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                    status == BatteryManager.BATTERY_STATUS_FULL
            trySend(BatteryStatus(level, isCharging))
            Log.d("BatteryDataSource", "Estado inicial de batería: Nivel=$level%, Cargando=$isCharging")
        } ?: run {
            trySend(BatteryStatus(0, false))
            Log.w("BatteryDataSource", "No se pudo obtener el intent inicial de la batería.")
        }

        awaitClose {
            try {
                context.unregisterReceiver(batteryReceiver)
                Log.d("BatteryDataSource", "BroadcastReceiver de batería desregistrado.")
            } catch (e: IllegalArgumentException) {
                Log.e("BatteryDataSource", "Error al desregistrar BroadcastReceiver de batería: ${e.message}")
            }
        }
    }.distinctUntilChanged()
}