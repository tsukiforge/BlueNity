package com.example.service

import android.os.Build
import android.os.Handler
import android.os.Looper
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.Toast
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SystemInfoRepository
import com.example.notification.QuickCleanNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class QuickCleanTileService : TileService() {

    override fun onStartListening() {
        super.onStartListening()
        val tile = qsTile ?: return
        tile.state = Tile.STATE_INACTIVE
        tile.label = "Hapus Cache"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            tile.subtitle = "Game Prep"
        }
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        val tile = qsTile
        tile?.state = Tile.STATE_ACTIVE
        tile?.updateTile()

        val appContext = applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = PreferencesRepository(appContext)
                val sysRepo = SystemInfoRepository(appContext)

                val selectedPkgs = prefsRepo.selectedPackagesFlow.first()
                val userProtected = prefsRepo.userProtectedPackagesFlow.first()
                val targets = selectedPkgs.filter { !userProtected.contains(it) }

                val result = sysRepo.performSafeClean(targets)

                val ramFreedMb = result.ramFreedBytes / (1024.0 * 1024.0)
                val cacheFreedKb = result.internalCacheFreedBytes / 1024L
                val afterAvailMb = result.afterAvailMemBytes / (1024 * 1024)

                val resultMsg = if (ramFreedMb > 0.05) {
                    "Berhasil membebaskan ${String.format(Locale.US, "%.1f", ramFreedMb)} MB RAM & ${cacheFreedKb} KB cache!"
                } else {
                    "${cacheFreedKb} KB cache internal dibersihkan (RAM: ${afterAvailMb} MB)"
                }

                // Show the notification showing how many MB were cleared
                QuickCleanNotificationHelper.showCleanResultNotification(
                    context = appContext,
                    title = "Hasil Pembersihan Cache",
                    message = "$resultMsg (RAM Tersedia: ${afterAvailMb} MB)"
                )

                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(appContext, "Game Prep: $resultMsg", Toast.LENGTH_SHORT).show()
                    tile?.state = Tile.STATE_INACTIVE
                    tile?.updateTile()
                }
            } catch (_: Exception) {
                Handler(Looper.getMainLooper()).post {
                    tile?.state = Tile.STATE_INACTIVE
                    tile?.updateTile()
                }
            }
        }
    }
}
