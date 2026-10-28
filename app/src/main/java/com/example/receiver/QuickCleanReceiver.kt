package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SystemInfoRepository
import com.example.notification.QuickCleanNotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Locale

class QuickCleanReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_QUICK_CLEAN = "com.gameprep.optimizer.ACTION_QUICK_CLEAN"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_QUICK_CLEAN) return

        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefsRepo = PreferencesRepository(appContext)
                val sysRepo = SystemInfoRepository(appContext)

                val selectedPkgs = prefsRepo.selectedPackagesFlow.first()
                val userProtected = prefsRepo.userProtectedPackagesFlow.first()

                // Filter out protected packages
                val targets = selectedPkgs.filter { !userProtected.contains(it) }

                // Execute safe real cleanup
                val result = sysRepo.performSafeClean(targets)

                val ramFreedMb = result.ramFreedBytes / (1024.0 * 1024.0)
                val cacheFreedKb = result.internalCacheFreedBytes / 1024L
                val afterAvailMb = result.afterAvailMemBytes / (1024 * 1024)

                val formattedMsg = if (ramFreedMb > 0.05) {
                    "Berhasil membebaskan ${String.format(Locale.US, "%.1f", ramFreedMb)} MB RAM & ${cacheFreedKb} KB cache!"
                } else {
                    "${cacheFreedKb} KB cache dibersihkan (RAM Tersedia: ${afterAvailMb} MB)"
                }

                // Update Notification in Notification Center
                QuickCleanNotificationHelper.showNotification(
                    context = appContext,
                    statusTitle = "Game Prep: Selesai Dibersihkan",
                    statusSubtitle = "$formattedMsg (Tersedia: ${afterAvailMb} MB)"
                )

                // Show toast to user on Main thread
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(appContext, "Game Prep: $formattedMsg", Toast.LENGTH_SHORT).show()
                }
            } catch (_: Exception) {
                // Graceful fallback
            } finally {
                pendingResult.finish()
            }
        }
    }
}
