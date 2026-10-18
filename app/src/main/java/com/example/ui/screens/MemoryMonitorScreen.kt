package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.MemoryStats
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentGreen
import com.example.ui.theme.AccentRed
import com.example.ui.viewmodel.GamePrepUiState

@Composable
fun MemoryMonitorScreen(
    uiState: GamePrepUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val stats = uiState.memoryStats

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with Refresh
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = "Monitor RAM Aktual",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Data hardware ActivityManager",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Button(
                onClick = onRefresh,
                modifier = Modifier.testTag("refresh_memory_screen_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Segarkan")
            }
        }

        if (stats != null) {
            val usedPct = stats.usedMemPercentage
            val statusColor: Color = when {
                stats.isLowMemory || usedPct > 85 -> AccentRed
                usedPct > 70 -> AccentAmber
                else -> AccentGreen
            }

            // Main RAM Gauge Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Penggunaan RAM Keseluruhan",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = statusColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (stats.isLowMemory) "STATUS: LOW RAM" else "STATUS: NORMAL",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "${MemoryStats.formatBytes(stats.usedMemBytes)} terpakai",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "dari total kapasitas hardware ${MemoryStats.formatBytes(stats.totalMemBytes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    LinearProgressIndicator(
                        progress = { (usedPct / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(12.dp)
                            .clip(RoundedCornerShape(6.dp)),
                        color = statusColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tersedia: ${MemoryStats.formatBytes(stats.availMemBytes)} (${stats.availMemPercentage}%)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Terpakai: $usedPct%",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor
                        )
                    }
                }
            }

            // Metrics Detail Breakdown
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Rincian Metrik Resmi Android",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    MetricRow(
                        label = "Total RAM Sistem",
                        value = MemoryStats.formatBytes(stats.totalMemBytes),
                        explanation = "Kapasitas RAM fisik yang dapat dialokasikan kernel."
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    MetricRow(
                        label = "Available Memory (availMem)",
                        value = MemoryStats.formatBytes(stats.availMemBytes),
                        explanation = "Memori bebas saat ini ditambah cache yang dapat langsung direklamasi."
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    MetricRow(
                        label = "Low-Memory Threshold",
                        value = MemoryStats.formatBytes(stats.thresholdBytes),
                        explanation = "Batas ambang sistem di mana Android LMK mulai menghentikan background processes."
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    MetricRow(
                        label = "Low-Memory Flag",
                        value = if (stats.isLowMemory) "AKTIF (Kritis)" else "TIDAK AKTIF (Aman)",
                        explanation = "Status sinyal low-memory resmi dari ActivityManager."
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    MetricRow(
                        label = "Penggunaan RAM Game Prep Sendiri",
                        value = "Heap: ${MemoryStats.formatBytes(stats.appHeapBytes)} | PSS: ${MemoryStats.formatBytes(stats.appPssBytes)}",
                        explanation = "Game Prep dirancang sangat ringan agar tidak memakan RAM game."
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)

                    MetricRow(
                        label = "Waktu Pengambilan Terakhir",
                        value = stats.formattedTime,
                        explanation = "Waktu pengukuran waktu-nyata di perangkat."
                    )
                }
            }
        }

        // Technical Limitations & Honesty Explanation Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Transparansi Pengukuran RAM Android",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "1. Tidak Ada 'Pembersihan Ajaib': Di Android modern, memori bebas yang tidak digunakan adalah memori yang terbuang. Kernel Linux sengaja menggunakan RAM untuk cache buffer disk agar membuka aplikasi lebih cepat.\n\n" +
                            "2. Force Stop Resmi: Menutup aplikasi melalui halaman Info Aplikasi sistem secara resmi menghentikan proses latar belakang dan mencegahnya langsung hidup kembali, memberikan ruang fisik lebih luas bagi game berat seperti Mobile Legends atau Roblox.\n\n" +
                            "3. Tidak Menjanjikan Peningkatan FPS: Stabilitas game pada perangkat RAM 2 GB (seperti Redmi 9A) dipengaruhi oleh GPU PowerVR GE8320 dan chipset Helio G25. Menutup aplikasi latar belakang mencegah game ditutup paksa (crash OOM) oleh Android LMK, bukan mengubah kemampuan grafis perangkat.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun MetricRow(
    label: String,
    value: String,
    explanation: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = explanation,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
