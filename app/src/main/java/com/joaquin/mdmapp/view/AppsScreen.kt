package com.joaquin.mdmapp.view

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import com.joaquin.mdmapp.viewmodel.AppsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.joaquin.mdmapp.R
import com.joaquin.mdmapp.model.AppInfo

@Composable
fun AppsScreen(navController: NavController, viewModel: AppsViewModel = viewModel()) {

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val apps by viewModel.installedApps.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
    }

    DisposableEffect(context, viewModel) {
        val appChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                viewModel.loadInstalledApps(context)
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_PACKAGE_ADDED)
            addAction(Intent.ACTION_PACKAGE_REMOVED)
            addAction(Intent.ACTION_PACKAGE_REPLACED)
            addDataScheme("package")
        }
        context.registerReceiver(appChangeReceiver, filter)
        onDispose {
            context.unregisterReceiver(appChangeReceiver)
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Spacer(modifier = Modifier.height(40.dp))

        Text(stringResource(id = R.string.installed_applications_title), style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        when {
            isLoading -> {
                CircularProgressIndicator()
                Text(stringResource(id = R.string.loading_applications))
            }
            error != null -> {
                Text(stringResource(id = R.string.error_prefix), color = MaterialTheme.colorScheme.error)
            }
            apps.isEmpty() -> {
                Text(stringResource(id = R.string.no_applications_found))
            }
            else -> {
                LazyColumn {
                    items(apps) { app ->
                        AppItem(app = app)
                    }
                }
            }
        }
    }
}

@Composable
fun AppItem(app: AppInfo) {
    Row(modifier = Modifier.padding(8.dp)) {
        Image(
            painter = rememberAsyncImagePainter(app.icon),
            contentDescription = null,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(app.name, style = MaterialTheme.typography.bodyLarge)
            Text(stringResource(id = R.string.package_) + app.packageName, style = MaterialTheme.typography.bodySmall)
            Text(stringResource(id = R.string.version) + app.versionName, style = MaterialTheme.typography.bodySmall)
        }
    }
}


