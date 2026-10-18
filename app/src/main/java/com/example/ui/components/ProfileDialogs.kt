package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppItem
import com.example.data.model.GameProfile
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentRed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProfileDialog(
    installedGames: List<AppItem>,
    onDismiss: () -> Unit,
    onConfirm: (name: String, gamePackage: String?) -> Unit
) {
    var profileName by remember { mutableStateOf("") }
    var selectedGamePkg by remember { mutableStateOf<String?>(null) }
    var expandedDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Tambah Profil Game Baru", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = profileName,
                    onValueChange = { profileName = it },
                    label = { Text("Nama Profil") },
                    placeholder = { Text("misal: Free Fire, PUBG") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("create_profile_name_input")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Kaitkan dengan Game (Opsional):",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentSelectedName = installedGames.find { it.packageName == selectedGamePkg }?.appName
                        ?: if (selectedGamePkg == null) "Pilih Game Terpasang" else selectedGamePkg

                    OutlinedTextField(
                        value = currentSelectedName.orEmpty(),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )

                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Tanpa Game Khusus") },
                            onClick = {
                                selectedGamePkg = null
                                expandedDropdown = false
                            }
                        )
                        installedGames.forEach { game ->
                            DropdownMenuItem(
                                text = { Text("${game.appName} (${game.packageName})") },
                                onClick = {
                                    selectedGamePkg = game.packageName
                                    if (profileName.isBlank()) {
                                        profileName = game.appName
                                    }
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(profileName, selectedGamePkg) },
                enabled = profileName.isNotBlank(),
                modifier = Modifier.testTag("create_profile_confirm_btn")
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ConfirmDeleteProfileDialog(
    profile: GameProfile,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = AccentRed
            )
        },
        title = { Text("Hapus Profil '${profile.name}'?") },
        text = {
            Text("Profil ini akan dihapus dari Game Prep. Pengaturan aplikasi yang tersimpan pada profil ini akan hilang.")
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = AccentRed),
                modifier = Modifier.testTag("confirm_delete_profile_btn")
            ) {
                Text("Hapus")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}

@Composable
fun ProtectionWarningDialog(
    app: AppItem,
    onDismiss: () -> Unit,
    onUnprotectAndSelect: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Security,
                contentDescription = null,
                tint = AccentAmber,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("Aplikasi Ini Dilindungi", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "${app.appName} (${app.packageName}) masuk dalam daftar aplikasi yang dilindungi untuk menjaga kestabilan telepon, pesan darurat, atau alarm Anda.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Jika Anda benar-benar ingin menargetkan aplikasi ini untuk dihentikan sebelum bermain game, Anda harus membatalkan status perlindungannya terlebih dahulu.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onUnprotectAndSelect,
                colors = ButtonDefaults.buttonColors(containerColor = AccentAmber),
                modifier = Modifier.testTag("unprotect_and_select_btn")
            ) {
                Text("Lepas Proteksi & Pilih")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Tetap Lindungi")
            }
        }
    )
}

@Composable
fun ConfirmPrepStartDialog(
    targetCount: Int,
    profileName: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        },
        title = {
            Text("Mulai Persiapan Game?", fontWeight = FontWeight.Bold)
        },
        text = {
            Column {
                Text(
                    text = "Profil: $profileName\n" +
                            "Jumlah aplikasi target: $targetCount aplikasi non-protected.\n\n" +
                            "Game Prep akan memandu Anda membuka halaman Info Aplikasi sistem satu demi satu agar Anda dapat menekan 'Paksa Berhenti' (Force Stop) secara resmi.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Aplikasi sistem penting dan yang dilindungi secara otomatis dikecualikan demi keamanan.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier.testTag("confirm_prep_start_btn")
            ) {
                Text("Mulai Sekarang")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
