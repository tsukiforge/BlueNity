package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import com.example.ui.components.ConfirmDeleteProfileDialog
import com.example.ui.components.ConfirmPrepStartDialog
import com.example.ui.components.GuidedPrepDialog
import com.example.ui.components.ProtectionWarningDialog
import com.example.ui.screens.AppManagerScreen
import com.example.ui.screens.GameProfilesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.MemoryMonitorScreen
import com.example.ui.screens.ProtectedAppsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.GamePrepTheme
import com.example.ui.viewmodel.GamePrepViewModel

enum class NavigationTab(val label: String, val icon: ImageVector, val tag: String) {
    HOME("Home", Icons.Default.Home, "nav_tab_home"),
    APPS("Apps", Icons.Default.Apps, "nav_tab_apps"),
    PROTECTED("Proteksi", Icons.Default.Security, "nav_tab_protected"),
    PROFILES("Profil", Icons.Default.SportsEsports, "nav_tab_profiles"),
    MEMORY("RAM", Icons.Default.Memory, "nav_tab_memory"),
    SETTINGS("Setelan", Icons.Default.Settings, "nav_tab_settings")
}

class MainActivity : ComponentActivity() {

    private val viewModel: GamePrepViewModel by viewModels {
        GamePrepViewModel.provideFactory(application)
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshNotificationAccess()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val uiState by viewModel.uiState.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(uiState.message) {
                uiState.message?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    viewModel.clearMessage()
                }
            }

            GamePrepTheme(themePreference = uiState.themeMode) {
                var currentTab by remember { mutableStateOf(NavigationTab.HOME) }

                // System BackHandler: pressing back on secondary tabs returns to Home
                if (currentTab != NavigationTab.HOME) {
                    BackHandler {
                        currentTab = NavigationTab.HOME
                    }
                }

                Scaffold(
                    contentWindowInsets = WindowInsets.safeDrawing,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier.testTag("bottom_navigation_bar"),
                            containerColor = MaterialTheme.colorScheme.surface
                        ) {
                            NavigationTab.values().forEach { tab ->
                                NavigationBarItem(
                                    selected = currentTab == tab,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = tab.icon,
                                            contentDescription = tab.label
                                        )
                                    },
                                    label = { Text(tab.label) },
                                    modifier = Modifier.testTag(tab.tag)
                                )
                            }
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentTab) {
                            NavigationTab.HOME -> {
                                HomeScreen(
                                    uiState = uiState,
                                    onRefreshMemory = { viewModel.refreshMemory() },
                                    onPrepareGameClicked = { viewModel.showPrepareConfirmation() },
                                    onQuickCleanClicked = { viewModel.quickTrimBackgroundApps() },
                                    onLaunchGameClicked = {
                                        viewModel.launchGame(uiState.activeProfile?.gamePackageName) {
                                            finishAndRemoveTask()
                                        }
                                    },
                                    onNavigateToApps = { currentTab = NavigationTab.APPS },
                                    onNavigateToProtected = { currentTab = NavigationTab.PROTECTED }
                                )
                            }
                            NavigationTab.APPS -> {
                                AppManagerScreen(
                                    uiState = uiState,
                                    onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                                    onFilterChanged = { viewModel.setFilter(it) },
                                    onSelectionClicked = { viewModel.onAppSelectionClicked(it) },
                                    onOpenAppInfo = { viewModel.openAppInfo(it) },
                                    onToggleProtection = { viewModel.toggleAppProtection(it) },
                                    onSelectAllNonProtected = { viewModel.selectAllNonProtected() },
                                    onClearAllSelection = { viewModel.clearAllSelection() }
                                )
                            }
                            NavigationTab.PROTECTED -> {
                                ProtectedAppsScreen(
                                    uiState = uiState,
                                    onToggleProtection = { viewModel.toggleAppProtection(it) },
                                    onOpenAppInfo = { viewModel.openAppInfo(it) }
                                )
                            }
                            NavigationTab.PROFILES -> {
                                GameProfilesScreen(
                                    uiState = uiState,
                                    onSelectProfile = { viewModel.selectProfile(it) },
                                    onCreateProfile = { name, pkg -> viewModel.createProfile(name, pkg) },
                                    onRequestDeleteProfile = { viewModel.requestDeleteProfile(it) },
                                    onSaveCurrentSelectionToProfile = { viewModel.saveCurrentSelectionToActiveProfile() }
                                )
                            }
                            NavigationTab.MEMORY -> {
                                MemoryMonitorScreen(
                                    uiState = uiState,
                                    onRefresh = { viewModel.refreshMemory() }
                                )
                            }
                            NavigationTab.SETTINGS -> {
                                SettingsScreen(
                                    uiState = uiState,
                                    onSetThemeMode = { viewModel.setThemeMode(it) },
                                    onOpenBatterySettings = { viewModel.openBatterySettings() },
                                    onNotificationBlockingChanged = { enabled ->
                                        viewModel.setNotificationBlockingEnabled(enabled)
                                        if (enabled && !uiState.notificationAccessEnabled) {
                                            viewModel.openNotificationListenerSettings()
                                        }
                                    },
                                    onOpenNotificationSettings = { viewModel.openNotificationListenerSettings() }
                                )
                            }
                        }

                        // Dialogs
                        if (uiState.showConfirmPrepDialog) {
                            ConfirmPrepStartDialog(
                                targetCount = uiState.apps.count { it.isSelected && !it.isProtected },
                                profileName = uiState.activeProfile?.name ?: "Game",
                                onDismiss = { viewModel.dismissPrepareConfirmation() },
                                onConfirm = { viewModel.startGuidedPrep() }
                            )
                        }

                        uiState.pendingProtectedAppToSelect?.let { protectedApp ->
                            ProtectionWarningDialog(
                                app = protectedApp,
                                onDismiss = { viewModel.dismissProtectionWarning() },
                                onUnprotectAndSelect = { viewModel.confirmUnprotectAndSelect(protectedApp) }
                            )
                        }

                        uiState.profileToDelete?.let { profToDelete ->
                            ConfirmDeleteProfileDialog(
                                profile = profToDelete,
                                onDismiss = { viewModel.dismissDeleteProfile() },
                                onConfirm = { viewModel.confirmDeleteProfile() }
                            )
                        }

                        uiState.guidedPrepState?.let { guidedState ->
                            GuidedPrepDialog(
                                state = guidedState,
                                onOpenAppInfo = { viewModel.openAppInfo(it) },
                                onNextStep = { viewModel.nextGuidedPrepStep() },
                                onCancel = { viewModel.cancelGuidedPrep() },
                                onLaunchGame = {
                                    viewModel.launchGame(uiState.activeProfile?.gamePackageName) {
                                        finishAndRemoveTask()
                                    }
                                },
                                activeGamePackage = uiState.activeProfile?.gamePackageName
                            )
                        }
                    }
                }
            }
        }
    }
}
