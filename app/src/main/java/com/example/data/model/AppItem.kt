package com.example.data.model

import android.graphics.drawable.Drawable

/**
 * Represents an installed application item discovered via official PackageManager APIs.
 */
data class AppItem(
    val packageName: String,
    val appName: String,
    val icon: Drawable? = null,
    val isSystemApp: Boolean = false,
    val isGame: Boolean = false,
    val isProtected: Boolean = false,
    val isSelected: Boolean = false,
    val category: String = "App",
    val versionName: String? = null
) {
    fun toAppInfo(): AppInfo = AppInfo.fromAppItem(this)
}
