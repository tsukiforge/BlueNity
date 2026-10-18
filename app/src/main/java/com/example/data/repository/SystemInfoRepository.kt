package com.example.data.repository

import android.app.ActivityManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Debug
import android.os.Process
import android.provider.Settings
import com.example.data.model.MemoryStats

class SystemInfoRepository(private val context: Context) {

    private val activityManager by lazy {
        context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
    }

    /**
     * Reads actual hardware RAM stats using official Android ActivityManager.MemoryInfo.
     */
    fun getMemoryStats(): MemoryStats {
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        // Game Prep's own memory usage
        val runtime = Runtime.getRuntime()
        val appHeapBytes = runtime.totalMemory() - runtime.freeMemory()

        var appPssBytes = 0L
        try {
            val pids = intArrayOf(Process.myPid())
            val memInfos = activityManager.getProcessMemoryInfo(pids)
            if (memInfos.isNotEmpty()) {
                // PSS in KB -> convert to bytes
                appPssBytes = memInfos[0].totalPss.toLong() * 1024L
            }
        } catch (_: Exception) {
            appPssBytes = Debug.getPss() * 1024L
        }

        return MemoryStats(
            totalMemBytes = memInfo.totalMem,
            availMemBytes = memInfo.availMem,
            thresholdBytes = memInfo.threshold,
            isLowMemory = memInfo.lowMemory,
            appPssBytes = appPssBytes,
            appHeapBytes = appHeapBytes,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Opens Android System App Info page using official Settings.ACTION_APPLICATION_DETAILS_SETTINGS API.
     * Non-root users can manually press Force Stop here.
     */
    fun openAppInfo(packageName: String): Result<Boolean> {
        return try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(true)
        } catch (e: ActivityNotFoundException) {
            Result.failure(Exception("Halaman Info Aplikasi untuk $packageName tidak dapat dibuka (ActivityNotFound)."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Launches the target game via official launcher intent.
     */
    fun launchApp(packageName: String): Result<Boolean> {
        return try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                Result.success(true)
            } else {
                Result.failure(Exception("Aplikasi tidak memiliki launcher intent atau tidak terpasang."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Opens Battery Optimization or Application details for battery settings.
     */
    fun openBatterySettings(): Result<Boolean> {
        return try {
            val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Result.success(true)
        } catch (_: Exception) {
            // Fallback to app info of Game Prep
            openAppInfo(context.packageName)
        }
    }

    /**
     * Verifies if package is currently installed and queryable.
     */
    fun isPackageInstalled(packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, 0)
            true
        } catch (_: Exception) {
            false
        }
    }
}
