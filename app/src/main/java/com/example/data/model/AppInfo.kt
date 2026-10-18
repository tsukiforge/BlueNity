package com.example.data.model

import android.graphics.drawable.Drawable

/**
 * Merepresentasikan data aplikasi di perangkat Android untuk Game Prep.
 *
 * @property packageName Nama paket unik aplikasi (contoh: com.mobile.legends)
 * @property label Label atau nama tampilan aplikasi yang dibaca dari PackageManager
 * @property icon Objek Drawable ikon aplikasi yang dimuat dari sistem
 * @property iconResId Referensi ID resource jika menggunakan ikon bawaan/statis
 * @property isProtected Status apakah aplikasi dilindungi agar tidak dihentikan
 * @property isSelected Status apakah aplikasi ditandai oleh pengguna untuk disiapkan
 * @property isSystemApp Menandai apakah aplikasi merupakan bawaan sistem Android/MIUI
 * @property isGame Menandai apakah aplikasi terdeteksi sebagai kategori game
 * @property category Kategori aplikasi ("Game", "Sistem", atau "Aplikasi")
 * @property versionName Versi aplikasi yang terpasang
 */
data class AppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable? = null,
    val iconResId: Int? = null,
    val isProtected: Boolean = false,
    val isSelected: Boolean = false,
    val isSystemApp: Boolean = false,
    val isGame: Boolean = false,
    val category: String = "App",
    val versionName: String? = null
) {
    /**
     * Alias `appName` agar kompatibel dan mudah digunakan bergantian dengan properti label.
     */
    val appName: String
        get() = label

    /**
     * Mengubah objek AppInfo menjadi AppItem.
     */
    fun toAppItem(): AppItem = AppItem(
        packageName = packageName,
        appName = label,
        icon = icon,
        isSystemApp = isSystemApp,
        isGame = isGame,
        isProtected = isProtected,
        isSelected = isSelected,
        category = category,
        versionName = versionName
    )

    companion object {
        /**
         * Membuat objek AppInfo dari AppItem.
         */
        fun fromAppItem(item: AppItem, iconResId: Int? = null): AppInfo = AppInfo(
            packageName = item.packageName,
            label = item.appName,
            icon = item.icon,
            iconResId = iconResId,
            isProtected = item.isProtected,
            isSelected = item.isSelected,
            isSystemApp = item.isSystemApp,
            isGame = item.isGame,
            category = item.category,
            versionName = item.versionName
        )
    }
}
