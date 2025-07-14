package com.joaquin.mdmapp.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.joaquin.mdmapp.viewmodel.AppsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter

@Composable
fun AppsScreen(navController: NavController, viewModel: AppsViewModel = viewModel()) {

    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val apps by viewModel.installedApps.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadInstalledApps(context)
        viewModel.registerAppChangeReceiver(context)
    }

    Column(modifier = Modifier.padding(16.dp)) {

        Spacer(modifier = Modifier.height(40.dp))

        Text("Installed Applications", style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            androidx.compose.material3.CircularProgressIndicator()
            Text("Cargando apps...")
        } else {
            LazyColumn {
                items(apps) { app ->
                    Row(modifier = Modifier.padding(8.dp)) {
                        Image(
                            painter = rememberAsyncImagePainter(app.icon),
                            contentDescription = null,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(app.name, style = MaterialTheme.typography.bodyLarge)
                            Text("Package: ${app.packageName}", style = MaterialTheme.typography.bodySmall)
                            Text("Version: ${app.versionName}", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}


