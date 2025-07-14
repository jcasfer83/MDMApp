package com.joaquin.mdmapp.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val name: String,
    val packageName: String,
    val versionName: String,
    val icon: Drawable
)