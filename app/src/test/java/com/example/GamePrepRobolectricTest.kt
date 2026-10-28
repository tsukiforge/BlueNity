package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AppItem
import com.example.data.model.GameProfile
import com.example.data.model.MemoryStats
import com.example.data.repository.AppManagerRepository
import com.example.data.repository.SystemInfoRepository
import com.example.ui.viewmodel.AppFilter
import com.example.ui.viewmodel.GamePrepUiState
import com.example.ui.viewmodel.GuidedPrepState
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class GamePrepRobolectricTest {

    private val context: Context = ApplicationProvider.getApplicationContext()

    @Test
    fun testAppNameStringResource() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Game Prep", appName)
    }

    @Test
    fun testAppInfoModel() {
        val appInfo = com.example.data.model.AppInfo(
            packageName = "com.mobile.legends",
            label = "Mobile Legends: Bang Bang",
            icon = null,
            iconResId = null,
            isProtected = true,
            isSelected = false,
            isGame = true
        )

        assertEquals("com.mobile.legends", appInfo.packageName)
        assertEquals("Mobile Legends: Bang Bang", appInfo.label)
        assertEquals("Mobile Legends: Bang Bang", appInfo.appName)
        assertTrue(appInfo.isProtected)
        assertFalse(appInfo.isSelected)
        assertTrue(appInfo.isGame)

        // Conversion test
        val item = appInfo.toAppItem()
        assertEquals(appInfo.packageName, item.packageName)
        assertEquals(appInfo.label, item.appName)
        assertEquals(appInfo.isProtected, item.isProtected)
        assertEquals(appInfo.isSelected, item.isSelected)

        val convertedBack = item.toAppInfo()
        assertEquals(appInfo.packageName, convertedBack.packageName)
        assertEquals(appInfo.label, convertedBack.label)
    }

    @Test
    fun testProfileSerializationAndRestoration() {
        // Test 1 & 2: Penyimpanan dan pemulihan profil
        val originalProfile = GameProfile(
            id = "test_profile_1",
            name = "Mobile Legends Low RAM",
            gamePackageName = "com.mobile.legends",
            selectedPackages = setOf("com.facebook.katana", "com.instagram.android"),
            protectedPackages = setOf("com.whatsapp"),
            createdAt = 123456789L
        )

        val json: JSONObject = originalProfile.toJson()
        val restoredProfile = GameProfile.fromJson(json)

        assertEquals(originalProfile.id, restoredProfile.id)
        assertEquals(originalProfile.name, restoredProfile.name)
        assertEquals(originalProfile.gamePackageName, restoredProfile.gamePackageName)
        assertEquals(originalProfile.selectedPackages, restoredProfile.selectedPackages)
        assertEquals(originalProfile.protectedPackages, restoredProfile.protectedPackages)
        assertEquals(originalProfile.createdAt, restoredProfile.createdAt)
    }

    @Test
    fun testMemoryStatsCalculations() {
        // Test 10: Pembacaan dan kalkulasi memori aktual
        val total = 2_000_000_000L // ~2GB
        val avail = 600_000_000L   // 600MB
        val threshold = 200_000_000L // 200MB

        val stats = MemoryStats(
            totalMemBytes = total,
            availMemBytes = avail,
            thresholdBytes = threshold,
            isLowMemory = false,
            appPssBytes = 25_000_000L,
            appHeapBytes = 15_000_000L
        )

        assertEquals(1_400_000_000L, stats.usedMemBytes)
        assertEquals(70, stats.usedMemPercentage)
        assertEquals(30, stats.availMemPercentage)
        assertFalse(stats.isLowMemory)
    }

    @Test
    fun testProtectedPackagesDefaults() {
        // Test 5 & 6: Perlindungan aplikasi & pengecualian sistem
        val repo = AppManagerRepository(context)
        val defaultProtected = repo.getDefaultProtectedPackages()

        // Game Prep itself must always be protected
        assertTrue(defaultProtected.contains(context.packageName))
        // Critical system packages must be protected
        assertTrue(defaultProtected.contains("android"))
        assertTrue(defaultProtected.contains("com.android.systemui"))
    }

    @Test
    fun testAppInfoIntentCreation() {
        // Test 7: Pembuatan intent App Info Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val repo = SystemInfoRepository(context)
        val targetPackage = "com.facebook.katana"
        val result = repo.openAppInfo(targetPackage)

        // Robolectric records the intent launch in context
        assertTrue(result.isSuccess)
    }

    @Test
    fun testPackageFilteringAndSearching() {
        // Test 3 & 4: Daftar aplikasi dan pencarian
        val apps = listOf(
            AppItem("com.mobile.legends", "Mobile Legends: Bang Bang", isGame = true, isProtected = false, isSelected = false),
            AppItem("com.whatsapp", "WhatsApp", isGame = false, isProtected = true, isSelected = false),
            AppItem("com.instagram.android", "Instagram", isGame = false, isProtected = false, isSelected = true),
            AppItem("com.android.phone", "Phone", isGame = false, isProtected = true, isSelected = false)
        )

        val state = GamePrepUiState(
            apps = apps,
            searchQuery = "insta",
            activeFilter = AppFilter.ALL
        )

        assertEquals(1, state.filteredApps.size)
        assertEquals("Instagram", state.filteredApps.first().appName)

        // Filter: Selected only
        val selectedState = GamePrepUiState(
            apps = apps,
            searchQuery = "",
            activeFilter = AppFilter.SELECTED
        )
        assertEquals(1, selectedState.filteredApps.size)
        assertEquals("com.instagram.android", selectedState.filteredApps.first().packageName)

        // Filter: Protected only
        val protectedState = GamePrepUiState(
            apps = apps,
            searchQuery = "",
            activeFilter = AppFilter.PROTECTED
        )
        assertEquals(2, protectedState.filteredApps.size)
    }

    @Test
    fun testGuidedPrepStepProgressionAndCancellation() {
        // Test 12: Penanganan alur persiapan terpandu dan pembatalan
        val targetApps = listOf(
            AppItem("com.app.one", "App One", isProtected = false, isSelected = true),
            AppItem("com.app.two", "App Two", isProtected = false, isSelected = true)
        )

        var prepState = GuidedPrepState(
            currentIndex = 0,
            targetApps = targetApps,
            isFinished = false
        )

        assertEquals("App One", prepState.currentApp?.appName)
        assertEquals(0.5f, prepState.progressFraction, 0.01f)
        assertFalse(prepState.isFinished)

        // Next step
        prepState = prepState.copy(currentIndex = 1)
        assertEquals("App Two", prepState.currentApp?.appName)
        assertEquals(1.0f, prepState.progressFraction, 0.01f)

        // Finish
        prepState = prepState.copy(isFinished = true)
        assertTrue(prepState.isFinished)
    }

    @Test
    fun testUninstalledPackageHandling() {
        // Test 8: Penanganan paket yang tidak terpasang
        val repo = SystemInfoRepository(context)
        val isInstalled = repo.isPackageInstalled("com.nonexistent.fake.package")
        assertFalse(isInstalled)

        val launchResult = repo.launchApp("com.nonexistent.fake.package")
        assertTrue(launchResult.isFailure)
    }

    @Test
    fun testTrimBackgroundProcesses() {
        val repo = SystemInfoRepository(context)
        val trimmed = repo.trimBackgroundProcesses(listOf("com.android.chrome", "com.example.otherapp"))
        assertTrue(trimmed >= 0)
    }

    @Test
    fun testClearAppInternalCache() {
        val repo = SystemInfoRepository(context)
        val freed = repo.clearAppInternalCache()
        assertTrue(freed >= 0L)
    }

    @Test
    fun testOpenAppStorageSettings() {
        val repo = SystemInfoRepository(context)
        val result = repo.openAppStorageSettings(context.packageName)
        assertTrue(result.isSuccess)
    }

    @Test
    fun testPerformSafeClean() {
        val repo = SystemInfoRepository(context)
        val cleanResult = repo.performSafeClean(listOf("com.android.chrome"))
        assertTrue(cleanResult.processedAppsCount >= 0)
        assertTrue(cleanResult.internalCacheFreedBytes >= 0L)
        assertTrue(cleanResult.ramFreedBytes >= 0L)
        assertTrue(cleanResult.afterAvailMemBytes >= 0L)
    }

    @Test
    fun testQuickCleanNotificationHelper() {
        com.example.notification.QuickCleanNotificationHelper.createNotificationChannel(context)
        com.example.notification.QuickCleanNotificationHelper.showNotification(
            context = context,
            statusTitle = "Game Prep Test",
            statusSubtitle = "RAM Tersedia: 1000 MB"
        )
        com.example.notification.QuickCleanNotificationHelper.cancelNotification(context)
    }
}
