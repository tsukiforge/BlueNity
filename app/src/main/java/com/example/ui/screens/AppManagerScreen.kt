package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppItem
import com.example.ui.components.AppItemCard
import com.example.ui.viewmodel.AppFilter
import com.example.ui.viewmodel.GamePrepUiState

@Composable
fun AppManagerScreen(
    uiState: GamePrepUiState,
    onSearchQueryChanged: (String) -> Unit,
    onFilterChanged: (AppFilter) -> Unit,
    onSelectionClicked: (AppItem) -> Unit,
    onOpenAppInfo: (String) -> Unit,
    onToggleProtection: (AppItem) -> Unit,
    onSelectAllNonProtected: () -> Unit,
    onClearAllSelection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Search Input
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("app_search_field"),
            placeholder = { Text("Cari nama aplikasi atau paket...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Cari",
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "Hapus pencarian"
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter Chips Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = uiState.activeFilter == AppFilter.ALL,
                onClick = { onFilterChanged(AppFilter.ALL) },
                label = { Text("Semua (${uiState.apps.size})") },
                modifier = Modifier.testTag("filter_chip_all")
            )

            FilterChip(
                selected = uiState.activeFilter == AppFilter.SELECTED,
                onClick = { onFilterChanged(AppFilter.SELECTED) },
                label = { Text("Terpilih (${uiState.selectedCount})") },
                modifier = Modifier.testTag("filter_chip_selected")
            )

            FilterChip(
                selected = uiState.activeFilter == AppFilter.PROTECTED,
                onClick = { onFilterChanged(AppFilter.PROTECTED) },
                label = { Text("Dilindungi (${uiState.protectedCount})") },
                modifier = Modifier.testTag("filter_chip_protected")
            )

            FilterChip(
                selected = uiState.activeFilter == AppFilter.GAMES,
                onClick = { onFilterChanged(AppFilter.GAMES) },
                label = { Text("Game (${uiState.gamesCount})") },
                modifier = Modifier.testTag("filter_chip_games")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Batch Action Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onSelectAllNonProtected,
                modifier = Modifier.testTag("select_all_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SelectAll,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Pilih Semua (Aman)", style = MaterialTheme.typography.labelMedium)
            }

            OutlinedButton(
                onClick = onClearAllSelection,
                modifier = Modifier.testTag("clear_all_btn"),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Batal Pilih", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // App List or Loading / Empty State
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.filteredApps.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.FilterList,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Tidak ada aplikasi yang cocok",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Coba ubah kata kunci pencarian atau filter yang dipilih.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("apps_lazy_column"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = uiState.filteredApps,
                    key = { it.packageName }
                ) { app ->
                    AppItemCard(
                        app = app,
                        onSelectionClicked = onSelectionClicked,
                        onOpenAppInfo = onOpenAppInfo,
                        onToggleProtection = onToggleProtection
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
