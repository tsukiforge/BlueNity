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
import java.io.File

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

    /**
     * Best-effort manual cleanup for user-selected packages. This is never called
     * automatically when launching a game because OEMs may restart killed processes.
     */
    fun trimBackgroundProcesses(packageNames: List<String>): Int {
        var count = 0
        for (pkg in packageNames) {
            try {
                if (pkg != context.packageName && pkg.isNotBlank()) {
                    activityManager.killBackgroundProcesses(pkg)
                    count++
                }
            } catch (_: Exception) {
                // Ignore restricted system packages gracefully
            }
        }
        return count
    }

    fun openDoNotDisturbSettings(): Result<Boolean> {
        return try {
            context.startActivity(
                Intent("android.settings.ZEN_MODE_SETTINGS")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
            Result.success(true)
        } catch (_: Exception) {
            try {
                context.startActivity(
                    Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                Result.success(true)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Safely clears Game Prep's own internal cache and code cache.
     * 100% compliant with Google Play Store policies (does not violate app sandbox or request prohibited permissions).
     * Returns the total number of bytes freed.
     */
    fun clearAppInternalCache(): Long {
        var bytesFreed = 0L
        try {
            bytesFreed += deleteDir(context.cacheDir)
            bytesFreed += deleteDir(context.codeCacheDir)
            context.externalCacheDir?.let {
                bytesFreed += deleteDir(it)
            }
        } catch (_: Exception) {
            // Ignore minor file access exceptions
        }
        return bytesFreed
    }

    private fun deleteDir(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var totalBytes = 0L
        val children = dir.listFiles() ?: return 0L
        for (child in children) {
            if (child.isDirectory) {
                totalBytes += deleteDir(child)
            } else {
                val len = child.length()
                if (child.delete()) {
                    totalBytes += len
                }
            }
        }
        return totalBytes
    }

    /**
     * Safely opens the Storage & Cache screen of any target application using standard Android Settings Intent.
     * This is the official, Google Play approved way for users to clear an application's external/internal cache.
     */
    fun openAppStorageSettings(packageName: String): Result<Boolean> {
        return openAppInfo(packageName)
    }

    /**
     * Executes safe, real cleanup:
     * 1. Measures genuine hardware available RAM before cleanup.
     * 2. Clears Game Prep's internal file cache.
     * 3. Halts background processes of selected apps.
     * 4. Measures genuine available RAM after cleanup to return real, non-fake stats.
     */
    fun performSafeClean(packageNames: List<String>): CleanResult {
        val beforeAvail = getMemoryStats().availMemBytes
        val cacheFreed = clearAppInternalCache()
        val count = trimBackgroundProcesses(packageNames)

        try {
            Thread.sleep(150)
        } catch (_: Exception) {}

        val afterAvail = getMemoryStats().availMemBytes
        val ramFreed = (afterAvail - beforeAvail).coerceAtLeast(0L)

        return CleanResult(
            processedAppsCount = count,
            internalCacheFreedBytes = cacheFreed,
            ramFreedBytes = ramFreed,
            beforeAvailMemBytes = beforeAvail,
            afterAvailMemBytes = afterAvail
        )
    }

}

data class CleanResult(
    val processedAppsCount: Int,
    val internalCacheFreedBytes: Long,
    val ramFreedBytes: Long,
    val beforeAvailMemBytes: Long,
    val afterAvailMemBytes: Long
)
