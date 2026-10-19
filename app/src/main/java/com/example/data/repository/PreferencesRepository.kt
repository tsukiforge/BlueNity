package com.example.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.model.GameProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_prep_prefs")

class PreferencesRepository(private val context: Context) {

    private object PreferencesKeys {
        val SELECTED_PACKAGES = stringSetPreferencesKey("selected_packages")
        val USER_PROTECTED_PACKAGES = stringSetPreferencesKey("user_protected_packages")
        val UNPROTECTED_OVERRIDES = stringSetPreferencesKey("unprotected_overrides")
        val ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val PROFILES_JSON = stringPreferencesKey("profiles_json")
        val THEME_MODE = stringPreferencesKey("theme_mode")
    }

    val selectedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.SELECTED_PACKAGES] ?: emptySet()
    }

    val userProtectedPackagesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.USER_PROTECTED_PACKAGES] ?: emptySet()
    }

    val unprotectedOverridesFlow: Flow<Set<String>> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.UNPROTECTED_OVERRIDES] ?: emptySet()
    }

    val activeProfileIdFlow: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.ACTIVE_PROFILE_ID]
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[PreferencesKeys.THEME_MODE] ?: "SYSTEM"
    }

    val profilesFlow: Flow<List<GameProfile>> = context.dataStore.data.map { preferences ->
        val jsonStr = preferences[PreferencesKeys.PROFILES_JSON]
        if (jsonStr.isNullOrBlank()) {
            getDefaultInitialProfiles()
        } else {
            try {
                val array = JSONArray(jsonStr)
                val list = mutableListOf<GameProfile>()
                for (i in 0 until array.length()) {
                    list.add(GameProfile.fromJson(array.getJSONObject(i)))
                }
                list
            } catch (e: Exception) {
                getDefaultInitialProfiles()
            }
        }
    }

    suspend fun saveSelectedPackages(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PACKAGES] = packages
        }
    }

    suspend fun togglePackageSelection(packageName: String) {
        context.dataStore.edit { preferences ->
            val current = preferences[PreferencesKeys.SELECTED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            if (current.contains(packageName)) {
                current.remove(packageName)
            } else {
                current.add(packageName)
            }
            preferences[PreferencesKeys.SELECTED_PACKAGES] = current
        }
    }

    suspend fun clearSelectedPackages() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PACKAGES] = emptySet()
        }
    }

    suspend fun setUserProtectedPackages(packages: Set<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USER_PROTECTED_PACKAGES] = packages
        }
    }

    suspend fun toggleUserProtection(packageName: String, isCurrentlyProtected: Boolean) {
        context.dataStore.edit { preferences ->
            val userProtected = preferences[PreferencesKeys.USER_PROTECTED_PACKAGES]?.toMutableSet() ?: mutableSetOf()
            val overrides = preferences[PreferencesKeys.UNPROTECTED_OVERRIDES]?.toMutableSet() ?: mutableSetOf()

            if (isCurrentlyProtected) {
                // User wants to unprotect this app
                userProtected.remove(packageName)
                overrides.add(packageName)
            } else {
                // User wants to protect this app
                userProtected.add(packageName)
                overrides.remove(packageName)
            }

            preferences[PreferencesKeys.USER_PROTECTED_PACKAGES] = userProtected
            preferences[PreferencesKeys.UNPROTECTED_OVERRIDES] = overrides
        }
    }

    suspend fun setActiveProfileId(profileId: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ACTIVE_PROFILE_ID] = profileId
        }
    }

    suspend fun saveProfiles(profiles: List<GameProfile>) {
        context.dataStore.edit { preferences ->
            val jsonArray = JSONArray()
            profiles.forEach { jsonArray.put(it.toJson()) }
            preferences[PreferencesKeys.PROFILES_JSON] = jsonArray.toString()
        }
    }

    suspend fun setThemeMode(theme: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = theme
        }
    }

    companion object {
        fun getDefaultInitialProfiles(): List<GameProfile> {
            return listOf(
                GameProfile(
                    id = "profile_mlbb",
                    name = "Mobile Legends",
                    gamePackageName = "com.mobile.legends",
                    selectedPackages = emptySet(),
                    protectedPackages = emptySet()
                ),
                GameProfile(
                    id = "profile_roblox",
                    name = "Roblox",
                    gamePackageName = "com.roblox.client",
                    selectedPackages = emptySet(),
                    protectedPackages = emptySet()
                ),
                GameProfile(
                    id = "profile_mc",
                    name = "Minecraft",
                    gamePackageName = "com.mojang.minecraftpe",
                    selectedPackages = emptySet(),
                    protectedPackages = emptySet()
                )
            )
        }
    }
}
