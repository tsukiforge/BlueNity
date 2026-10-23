package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.model.AppItem
import com.example.data.model.GameProfile
import com.example.data.model.MemoryStats
import com.example.data.repository.AppManagerRepository
import com.example.data.repository.PreferencesRepository
import com.example.data.repository.SystemInfoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppFilter {
    ALL,
    SELECTED,
    PROTECTED,
    GAMES
}

data class GuidedPrepState(
    val currentIndex: Int = 0,
    val targetApps: List<AppItem> = emptyList(),
    val isFinished: Boolean = false
) {
    val currentApp: AppItem?
        get() = targetApps.getOrNull(currentIndex)

    val progressText: String
        get() = if (targetApps.isNotEmpty()) {
            "Aplikasi ${currentIndex + 1} dari ${targetApps.size}"
        } else ""

    val progressFraction: Float
        get() = if (targetApps.isNotEmpty()) {
            (currentIndex + 1).toFloat() / targetApps.size.toFloat()
        } else 0f
}

data class GamePrepUiState(
    val apps: List<AppItem> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: AppFilter = AppFilter.ALL,
    val memoryStats: MemoryStats? = null,
    val profiles: List<GameProfile> = emptyList(),
    val activeProfileId: String? = null,
    val guidedPrepState: GuidedPrepState? = null,
    val isLoading: Boolean = true,
    val message: String? = null,
    val themeMode: String = "SYSTEM",
    val pendingProtectedAppToSelect: AppItem? = null,
    val showConfirmPrepDialog: Boolean = false,
    val profileToDelete: GameProfile? = null
) {
    val activeProfile: GameProfile?
        get() = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull()

    val selectedCount: Int
        get() = apps.count { it.isSelected }

    val protectedCount: Int
        get() = apps.count { it.isProtected }

    val gamesCount: Int
        get() = apps.count { it.isGame }

    val filteredApps: List<AppItem>
        get() {
            var list = when (activeFilter) {
                AppFilter.ALL -> apps
                AppFilter.SELECTED -> apps.filter { it.isSelected }
                AppFilter.PROTECTED -> apps.filter { it.isProtected }
                AppFilter.GAMES -> apps.filter { it.isGame }
            }

            if (searchQuery.isNotBlank()) {
                val q = searchQuery.trim().lowercase()
                list = list.filter {
                    it.appName.lowercase().contains(q) || it.packageName.lowercase().contains(q)
                }
            }

            return list
        }
}

class GamePrepViewModel(
    application: Application,
    private val appManagerRepository: AppManagerRepository = AppManagerRepository(application),
    private val preferencesRepository: PreferencesRepository = PreferencesRepository(application),
    private val systemInfoRepository: SystemInfoRepository = SystemInfoRepository(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(GamePrepUiState())
    val uiState: StateFlow<GamePrepUiState> = _uiState.asStateFlow()

    init {
        loadData()
        refreshMemory()
    }

    private fun loadData() {
        viewModelScope.launch {
            val packagePrefsFlow = combine(
                preferencesRepository.selectedPackagesFlow,
                preferencesRepository.userProtectedPackagesFlow,
                preferencesRepository.unprotectedOverridesFlow
            ) { selected, userProt, overrides ->
                Triple(selected, userProt, overrides)
            }

            val profilePrefsFlow = combine(
                preferencesRepository.profilesFlow,
                preferencesRepository.activeProfileIdFlow,
                preferencesRepository.themeModeFlow
            ) { profiles, activeId, theme ->
                Triple(profiles, activeId, theme)
            }

            combine(packagePrefsFlow, profilePrefsFlow) { (selectedPkgs, userProtected, overrides), (profiles, activeId, theme) ->
                val currentActiveId = activeId ?: profiles.firstOrNull()?.id
                val activeProf = profiles.find { it.id == currentActiveId }
                val gamePkg = activeProf?.gamePackageName

                val appList = appManagerRepository.getInstalledApps(
                    selectedPackages = selectedPkgs,
                    userProtectedPackages = userProtected,
                    unprotectedOverrides = overrides,
                    currentGamePackage = gamePkg
                )

                _uiState.update { current ->
                    current.copy(
                        apps = appList,
                        profiles = profiles,
                        activeProfileId = currentActiveId,
                        themeMode = theme,
                        isLoading = false
                    )
                }
            }.collect {}
        }
    }

    fun refreshMemory() {
        viewModelScope.launch {
            val stats = systemInfoRepository.getMemoryStats()
            _uiState.update { it.copy(memoryStats = stats) }
        }
    }

    fun setSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun setFilter(filter: AppFilter) {
        _uiState.update { it.copy(activeFilter = filter) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }

    fun onAppSelectionClicked(app: AppItem) {
        if (!app.isSelected && app.isProtected) {
            // Cannot select protected app without user warning
            _uiState.update { it.copy(pendingProtectedAppToSelect = app) }
        } else {
            viewModelScope.launch {
                preferencesRepository.togglePackageSelection(app.packageName)
            }
        }
    }

    fun dismissProtectionWarning() {
        _uiState.update { it.copy(pendingProtectedAppToSelect = null) }
    }

    fun confirmUnprotectAndSelect(app: AppItem) {
        viewModelScope.launch {
            // First remove protection
            preferencesRepository.toggleUserProtection(app.packageName, isCurrentlyProtected = true)
            // Then select
            preferencesRepository.togglePackageSelection(app.packageName)
            _uiState.update {
                it.copy(
                    pendingProtectedAppToSelect = null,
                    message = "${app.appName} tidak lagi dilindungi dan ditandai untuk persiapan."
                )
            }
        }
    }

    fun toggleAppProtection(app: AppItem) {
        viewModelScope.launch {
            preferencesRepository.toggleUserProtection(app.packageName, isCurrentlyProtected = app.isProtected)
            // If now protected and was selected, unselect it to prevent accidental stopping
            if (!app.isProtected && app.isSelected) {
                preferencesRepository.togglePackageSelection(app.packageName)
            }
            val status = if (!app.isProtected) "sekarang dilindungi" else "tidak lagi dilindungi"
            _uiState.update { it.copy(message = "${app.appName} $status.") }
        }
    }

    fun selectAllNonProtected() {
        viewModelScope.launch {
            val currentSelected = _uiState.value.apps.filter { it.isSelected }.map { it.packageName }.toMutableSet()
            val candidateApps = _uiState.value.filteredApps.filter { !it.isProtected }

            candidateApps.forEach { currentSelected.add(it.packageName) }
            preferencesRepository.saveSelectedPackages(currentSelected)
            _uiState.update {
                it.copy(message = "${candidateApps.size} aplikasi non-protected ditambahkan ke pilihan.")
            }
        }
    }

    fun clearAllSelection() {
        viewModelScope.launch {
            preferencesRepository.clearSelectedPackages()
            _uiState.update { it.copy(message = "Semua pilihan dibatalkan.") }
        }
    }

    fun openAppInfo(packageName: String) {
        val result = systemInfoRepository.openAppInfo(packageName)
        result.onFailure { error ->
            _uiState.update { it.copy(message = error.localizedMessage ?: "Gagal membuka App Info") }
        }
    }

    /**
     * Stops selected, non-protected background processes and safely clears internal app cache
     * in full compliance with Google Play Store policies (no root, no prohibited hidden APIs).
     */
    fun quickTrimBackgroundApps() {
        val targets = _uiState.value.apps.filter { it.isSelected && !it.isProtected }.map { it.packageName }
        if (targets.isEmpty()) {
            _uiState.update { it.copy(message = "Tidak ada aplikasi non-protected yang dipilih. Centang aplikasi di tab Kelola Aplikasi terlebih dahulu.") }
            return
        }
        _uiState.update { it.copy(message = "Menghentikan proses & membersihkan cache aman...") }
        viewModelScope.launch(Dispatchers.IO) {
            val count = systemInfoRepository.trimBackgroundProcesses(targets)
            val cacheFreedBytes = systemInfoRepository.clearAppInternalCache()
            refreshMemory()

            val cacheFreedKb = cacheFreedBytes / 1024L
            val cacheInfo = if (cacheFreedKb > 0) " dan $cacheFreedKb KB cache internal dibersihkan" else " dan cache internal dibersihkan"
            _uiState.update {
                it.copy(
                    message = "Selesai. $count proses latar belakang dihentikan$cacheInfo secara aman sesuai kebijakan resmi Google Play Store."
                )
            }
        }
    }

    /**
     * Safely opens Storage & Cache screen for a package so the user can clear app cache directly.
     */
    fun openAppStorage(packageName: String) {
        val result = systemInfoRepository.openAppStorageSettings(packageName)
        result.onFailure { error ->
            _uiState.update { it.copy(message = error.localizedMessage ?: "Gagal membuka pengaturan penyimpanan") }
        }
    }

    /**
     * Automatically cleans selected background apps from memory, then launches the target game.
     */
    fun launchGame(packageName: String?, onLaunched: (() -> Unit)? = null) {
        if (packageName.isNullOrBlank()) {
            _uiState.update { it.copy(message = "Belum ada paket game yang diatur untuk profil ini.") }
            return
        }

        val result = systemInfoRepository.launchApp(packageName)
        result.onSuccess {
            onLaunched?.invoke()
        }.onFailure { error ->
            _uiState.update { it.copy(message = error.localizedMessage ?: "Gagal meluncurkan game") }
        }
    }

    fun openBatterySettings() {
        val result = systemInfoRepository.openBatterySettings()
        result.onFailure { error ->
            _uiState.update { it.copy(message = error.localizedMessage ?: "Gagal membuka pengaturan baterai") }
        }
    }

    fun openDoNotDisturbSettings() {
        systemInfoRepository.openDoNotDisturbSettings().onFailure { error ->
            _uiState.update { it.copy(message = error.localizedMessage ?: "Pengaturan Jangan Ganggu tidak tersedia") }
        }
    }

    // Profile Management
    fun selectProfile(profileId: String) {
        viewModelScope.launch {
            preferencesRepository.setActiveProfileId(profileId)
            val profile = _uiState.value.profiles.find { it.id == profileId }
            if (profile != null) {
                // If profile has saved selected packages, load them
                if (profile.selectedPackages.isNotEmpty()) {
                    preferencesRepository.saveSelectedPackages(profile.selectedPackages)
                } else {
                    preferencesRepository.clearSelectedPackages()
                }
                _uiState.update { it.copy(message = "Profil ${profile.name} diaktifkan.") }
            }
        }
    }

    fun createProfile(name: String, gamePackage: String?) {
        if (name.isBlank()) {
            _uiState.update { it.copy(message = "Nama profil tidak boleh kosong.") }
            return
        }
        viewModelScope.launch {
            val currentProfiles = _uiState.value.profiles.toMutableList()
            val newProfile = GameProfile(
                id = "prof_${UUID.randomUUID()}",
                name = name.trim(),
                gamePackageName = gamePackage?.takeIf { it.isNotBlank() },
                selectedPackages = _uiState.value.apps.filter { it.isSelected }.map { it.packageName }.toSet(),
                protectedPackages = emptySet()
            )
            currentProfiles.add(newProfile)
            preferencesRepository.saveProfiles(currentProfiles)
            preferencesRepository.setActiveProfileId(newProfile.id)
            _uiState.update { it.copy(message = "Profil '${newProfile.name}' berhasil dibuat.") }
        }
    }

    fun saveCurrentSelectionToActiveProfile() {
        val active = _uiState.value.activeProfile ?: return
        viewModelScope.launch {
            val currentSelected = _uiState.value.apps.filter { it.isSelected }.map { it.packageName }.toSet()
            val updatedProfiles = _uiState.value.profiles.map { prof ->
                if (prof.id == active.id) {
                    prof.copy(selectedPackages = currentSelected)
                } else {
                    prof
                }
            }
            preferencesRepository.saveProfiles(updatedProfiles)
            _uiState.update { it.copy(message = "Pilihan aplikasi disimpan ke profil '${active.name}'.") }
        }
    }

    fun requestDeleteProfile(profile: GameProfile) {
        _uiState.update { it.copy(profileToDelete = profile) }
    }

    fun dismissDeleteProfile() {
        _uiState.update { it.copy(profileToDelete = null) }
    }

    fun confirmDeleteProfile() {
        val target = _uiState.value.profileToDelete ?: return
        viewModelScope.launch {
            val updated = _uiState.value.profiles.filter { it.id != target.id }
            preferencesRepository.saveProfiles(updated)
            if (_uiState.value.activeProfileId == target.id) {
                val nextActive = updated.firstOrNull()?.id ?: ""
                preferencesRepository.setActiveProfileId(nextActive)
            }
            _uiState.update {
                it.copy(
                    profileToDelete = null,
                    message = "Profil '${target.name}' dihapus."
                )
            }
        }
    }

    // Guided Preparation Flow
    fun showPrepareConfirmation() {
        val targets = _uiState.value.apps.filter { it.isSelected && !it.isProtected }
        if (targets.isEmpty()) {
            _uiState.update {
                it.copy(message = "Tidak ada aplikasi terpilih untuk disiapkan. Pilih aplikasi non-terlindungi terlebih dahulu.")
            }
            return
        }
        _uiState.update { it.copy(showConfirmPrepDialog = true) }
    }

    fun dismissPrepareConfirmation() {
        _uiState.update { it.copy(showConfirmPrepDialog = false) }
    }

    fun startGuidedPrep() {
        val targets = _uiState.value.apps.filter { it.isSelected && !it.isProtected }
        _uiState.update {
            it.copy(
                showConfirmPrepDialog = false,
                guidedPrepState = GuidedPrepState(
                    currentIndex = 0,
                    targetApps = targets,
                    isFinished = false
                )
            )
        }
    }

    fun nextGuidedPrepStep() {
        val state = _uiState.value.guidedPrepState ?: return
        val nextIdx = state.currentIndex + 1
        if (nextIdx < state.targetApps.size) {
            _uiState.update {
                it.copy(guidedPrepState = state.copy(currentIndex = nextIdx))
            }
        } else {
            // Completed all steps
            _uiState.update {
                it.copy(
                    guidedPrepState = state.copy(isFinished = true),
                    message = "Persiapan terpandu selesai! Buka game untuk mulai bermain."
                )
            }
            refreshMemory()
        }
    }

    fun cancelGuidedPrep() {
        _uiState.update {
            it.copy(
                guidedPrepState = null,
                message = "Persiapan terpandu dibatalkan. Status sistem tidak diubah."
            )
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setThemeMode(mode)
        }
    }

    companion object {
        fun provideFactory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return GamePrepViewModel(application) as T
                }
            }
    }
}
