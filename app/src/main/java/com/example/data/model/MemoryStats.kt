package com.example.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Encapsulates real hardware memory measurements obtained from Android OS ActivityManager.MemoryInfo.
 * Transparent and measured without simulated data.
 */
data class MemoryStats(
    val totalMemBytes: Long,
    val availMemBytes: Long,
    val thresholdBytes: Long,
    val isLowMemory: Boolean,
    val appPssBytes: Long,
    val appHeapBytes: Long,
    val timestamp: Long = System.currentTimeMillis()
) {
    val usedMemBytes: Long
        get() = (totalMemBytes - availMemBytes).coerceAtLeast(0L)

    val usedMemPercentage: Int
        get() = if (totalMemBytes > 0) ((usedMemBytes.toDouble() / totalMemBytes) * 100).toInt() else 0

    val availMemPercentage: Int
        get() = if (totalMemBytes > 0) ((availMemBytes.toDouble() / totalMemBytes) * 100).toInt() else 0

    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
            return sdf.format(Date(timestamp))
        }

    companion object {
        fun formatBytes(bytes: Long): String {
            val mb = bytes.toDouble() / (1024 * 1024)
            return if (mb >= 1024) {
                val gb = mb / 1024
                String.format(Locale.US, "%.2f GB", gb)
            } else {
                String.format(Locale.US, "%.1f MB", mb)
            }
        }
    }
}
