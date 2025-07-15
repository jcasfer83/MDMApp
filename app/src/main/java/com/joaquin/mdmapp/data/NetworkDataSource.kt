package com.joaquin.mdmapp.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import android.util.Log
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

interface NetworkDataSource {
    fun getNetworkStatusFlow(context: Context): Flow<String>
}

class AndroidNetworkDataSource : NetworkDataSource {
    override fun getNetworkStatusFlow(context: Context): Flow<String> = callbackFlow {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            fun updateAndSendStatus() {
                val activeNetwork = connectivityManager.activeNetwork
                val caps = connectivityManager.getNetworkCapabilities(activeNetwork)

                val status = when {
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> "WiFi"
                    caps?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> "Datos Móviles"
                    else -> "Sin Conexión"
                }
                trySend(status)
                Log.d("NetworkDataSource", "Estado de red actualizado: $status")
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                updateAndSendStatus()
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                updateAndSendStatus()
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                updateAndSendStatus()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            connectivityManager.registerDefaultNetworkCallback(networkCallback)
        } else {
            val request = NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager.registerNetworkCallback(request, networkCallback)
        }

        networkCallback.updateAndSendStatus()

        awaitClose {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
                Log.d("NetworkDataSource", "NetworkCallback retirado del registro.")
            } catch (e: IllegalArgumentException) {
                Log.e("NetworkDataSource", "Error al retirar del registro NetworkCallback: ${e.message}")
            }
        }
    }.distinctUntilChanged()
}