package com.example.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.view.inputmethod.InputMethodManager
import com.example.data.model.AppItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppManagerRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager

    // Cached system packages to avoid redundant queries on 2GB RAM
    private var cachedDefaultProtectedPackages: Set<String>? = null

    /**
     * Resolves all system-critical communication, alarm, keyboard, and launcher packages
     * that should be protected by default to ensure phone reliability.
     */
    fun getDefaultProtectedPackages(): Set<String> {
        cachedDefaultProtectedPackages?.let { return it }

        val protectedSet = mutableSetOf<String>()

        // 1. Game Prep itself
        protectedSet.add(context.packageName)

        // 2. Critical Android OS / MIUI framework packages
        protectedSet.addAll(
            listOf(
                "android",
                "com.android.systemui",
                "com.android.settings",
                "com.android.phone",
                "com.android.server.telecom",
                "com.miui.home",
                "com.miui.securityadd",
                "com.miui.securitycenter"
            )
        )

        // 3. Dialer / Telephony apps
        try {
            val dialerIntent = Intent(Intent.ACTION_DIAL)
            val dialers = packageManager.queryIntentActivities(dialerIntent, 0)
            dialers.forEach { protectedSet.add(it.activityInfo.packageName) }
        } catch (_: Exception) {}

        // 4. SMS / Messaging apps
        try {
            val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("smsto:")
            }
            val smsApps = packageManager.queryIntentActivities(smsIntent, 0)
            smsApps.forEach { protectedSet.add(it.activityInfo.packageName) }
        } catch (_: Exception) {}

        // 5. Alarm / Clock apps
        try {
            val alarmIntent = Intent(AlarmClock.ACTION_SHOW_ALARMS)
            val alarms = packageManager.queryIntentActivities(alarmIntent, 0)
            alarms.forEach { protectedSet.add(it.activityInfo.packageName) }
        } catch (_: Exception) {}

        // 6. Active Keyboard / Input Methods
        try {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.enabledInputMethodList?.forEach { imi ->
                protectedSet.add(imi.packageName)
            }
        } catch (_: Exception) {}

        // 7. Recommended communication & 2FA packages if present
        val criticalThirdPartyPackages = listOf(
            "com.whatsapp",
            "com.discord",
            "org.telegram.messenger",
            "com.google.android.apps.authenticator2"
        )
        criticalThirdPartyPackages.forEach { pkg ->
            try {
                packageManager.getPackageInfo(pkg, 0)
                protectedSet.add(pkg)
            } catch (_: Exception) {}
        }

        cachedDefaultProtectedPackages = protectedSet
        return protectedSet
    }

    /**
     * Loads launchable applications using official PackageManager launcher intent queries.
     * Complies fully with Android 11+ Package Visibility guidelines.
     */
    suspend fun getInstalledApps(
        selectedPackages: Set<String>,
        userProtectedPackages: Set<String>,
        unprotectedOverrides: Set<String>,
        currentGamePackage: String? = null
    ): List<AppItem> = withContext(Dispatchers.IO) {
        val defaultProtected = getDefaultProtectedPackages()

        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos = try {
            packageManager.queryIntentActivities(launcherIntent, 0)
        } catch (e: Exception) {
            emptyList()
        }

        val distinctPackages = mutableMapOf<String, AppItem>()

        for (resolveInfo in resolveInfos) {
            val pkgName = resolveInfo.activityInfo.packageName
            if (distinctPackages.containsKey(pkgName)) continue

            try {
                val appInfo = packageManager.getApplicationInfo(pkgName, 0)
                val label = packageManager.getApplicationLabel(appInfo).toString()
                val icon = try {
                    packageManager.getApplicationIcon(appInfo)
                } catch (_: Exception) {
                    null
                }

                val isSystem = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

                val isGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appInfo.category == ApplicationInfo.CATEGORY_GAME ||
                            (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                } else {
                    @Suppress("DEPRECATION")
                    (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0
                }

                // Determine protection status:
                // Protected if: (is default protected OR is user protected OR is current active game) AND NOT in unprotectedOverrides
                val isByDefault = defaultProtected.contains(pkgName)
                val isByUser = userProtectedPackages.contains(pkgName)
                val isCurrentGame = (currentGamePackage != null && pkgName == currentGamePackage)
                val isOverridden = unprotectedOverrides.contains(pkgName)

                val isProtected = if (isOverridden) {
                    false
                } else {
                    isByDefault || isByUser || isCurrentGame
                }

                val isSelected = selectedPackages.contains(pkgName)

                val categoryStr = when {
                    isGame -> "Game"
                    isSystem -> "Sistem"
                    else -> "Aplikasi"
                }

                val versionName = try {
                    packageManager.getPackageInfo(pkgName, 0).versionName
                } catch (_: Exception) {
                    null
                }

                distinctPackages[pkgName] = AppItem(
                    packageName = pkgName,
                    appName = label,
                    icon = icon,
                    isSystemApp = isSystem,
                    isGame = isGame,
                    isProtected = isProtected,
                    isSelected = isSelected,
                    category = categoryStr,
                    versionName = versionName
                )
            } catch (_: Exception) {
                // Ignore packages that cannot be accessed or were removed
            }
        }

        distinctPackages.values.sortedBy { it.appName.lowercase() }
    }
}
