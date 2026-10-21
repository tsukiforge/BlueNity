package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.components.MemoryOverviewCard
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGreen
import com.example.ui.viewmodel.GamePrepUiState

@Composable
fun HomeScreen(
    uiState: GamePrepUiState,
    onRefreshMemory: () -> Unit,
    onPrepareGameClicked: () -> Unit,
    onQuickCleanClicked: () -> Unit,
    onLaunchGameClicked: () -> Unit,
    onNavigateToApps: () -> Unit,
    onNavigateToProtected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeProfile = uiState.activeProfile
    val hasGamePackage = !activeProfile?.gamePackageName.isNullOrBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Active Game Banner
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("hero_game_card"),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
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
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(end = 12.dp)
                    ) {
                        Text(
                            text = "Profil Aktif",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = activeProfile?.name ?: "Game Prep",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Box(
                            modifier = Modifier.padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SportsEsports,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                val gameStatusText = if (hasGamePackage) {
                    "Paket Game: ${activeProfile?.gamePackageName}"
                } else {
                    "Belum ada aplikasi game yang dikaitkan."
                }

                Text(
                    text = gameStatusText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Primary CTA Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilledTonalButton(
                        onClick = onQuickCleanClicked,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("home_quick_clean_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Hentikan Proses",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (hasGamePackage) {
                        Button(
                            onClick = onLaunchGameClicked,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("home_launch_game_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mulai Game",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Button(
                            onClick = onPrepareGameClicked,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("home_prepare_game_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Persiapan Teliti",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                if (hasGamePackage) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onPrepareGameClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .testTag("home_prepare_game_button"),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Persiapan Teliti (Force Stop Sistem)",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        // Real RAM Monitor Card
        MemoryOverviewCard(
            memoryStats = uiState.memoryStats,
            onRefresh = onRefreshMemory
        )

        // Summary Statistics Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(
                onClick = onNavigateToApps,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_selected_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Apps,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${uiState.selectedCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aplikasi Terpilih",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            ElevatedCard(
                onClick = onNavigateToProtected,
                modifier = Modifier
                    .weight(1f)
                    .testTag("summary_protected_card"),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = AccentGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${uiState.protectedCount}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Aplikasi Terlindungi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Redmi 9A 2GB RAM Information Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Prinsip Kerja Game Prep pada Redmi 9A",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Pure Non-Root: Menghormati batasan keamanan Android resmi tanpa trik palsu.\n" +
                            "• Persiapan Terpandu: Membantu Anda membuka Info Aplikasi sistem untuk melakukan Force Stop resmi.\n" +
                            "• Perlindungan Panggilan: Telepon, SMS, dan Jam Alarm secara otomatis dikecualikan agar aktivitas penting tidak terganggu.\n" +
                            "• Zero-Bloat: Game Prep berbobot sangat ringan agar tidak membebani sisa RAM 2 GB Anda.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
