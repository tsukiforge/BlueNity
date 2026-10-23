# ==============================================================================
# PROGUARD & R8 PRODUCTION SHRINKING & OBFUSCATION CONFIGURATION
# Specifically tuned for Low-RAM devices (2GB - 3GB RAM)
# ==============================================================================

# ------------------------------------------------------------------------------
# 1. AGGRESSIVE CODE SHRINKING & OPTIMIZATION PASSES
# ------------------------------------------------------------------------------
# Run multiple passes of optimizations (dead code elimination, method inlining,
# constant propagation, and devirtualization)
-optimizationpasses 5

# Allow R8 to widen the access modifiers of classes and class members.
# This permits more aggressive inlining and dead code pruning.
-allowaccessmodification

# Merge single-implementation interfaces into implementing classes to minimize
# the total number of classes loaded into ART heap memory on low-RAM phones.
-mergeinterfacesaggressively

# Flatten package hierarchy: Repackage all obfuscated classes into a single root
# namespace ('') to drastically shorten class descriptor strings in classes.dex.
-repackageclasses ''

# ------------------------------------------------------------------------------
# 2. STRIP DEBUG LOGGING & RUNTIME ASSERTIONS
# ------------------------------------------------------------------------------
# Eliminate non-critical android.util.Log invocations and Kotlin runtime checks
# in production release builds to shrink DEX size and save CPU cycles.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
    public static int w(...);
}

# Remove Kotlin parameter null-safety checks in release to save method bytecode
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    public static void checkNotNullParameter(...);
    public static void checkNotNullExpressionValue(...);
    public static void checkExpressionValueIsNotNull(...);
    public static void checkParameterIsNotNull(...);
}

# ------------------------------------------------------------------------------
# 3. METADATA & ATTRIBUTE PRUNING
# ------------------------------------------------------------------------------
# Strip debugging and reflection metadata that bloats classes.dex in memory.
# Retain only what is strictly required for Coroutines, Serialization & Stacktraces.
-renamesourcefileattribute SourceFile
-keepattributes SourceFile,LineNumberTable,EnclosingMethod,InnerClasses
-keepattributes *Annotation*,Signature

# ------------------------------------------------------------------------------
# 4. PRESERVE APPLICATION ENTRY POINTS & DATA MODELS
# ------------------------------------------------------------------------------
# Protect Main Activity entry point
-keep public class com.example.MainActivity extends androidx.activity.ComponentActivity

# Preserve Game Prep data models (JSON & DataStore serialization)
-keepclassmembers class com.example.data.model.** {
    public <fields>;
    public <methods>;
}
-keep class com.example.data.model.** { *; }

# Preserve ViewModel and factory constructors for lifecycle instantiation
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * extends androidx.lifecycle.ViewModelProvider$Factory {
    <init>(...);
}

# Preserve DataStore preferences
-keepclassmembers class * extends androidx.datastore.preferences.core.Preferences {
    *;
}

# ------------------------------------------------------------------------------
# 5. JETPACK COMPOSE COMPATIBILITY
# ------------------------------------------------------------------------------
# Prevent R8 from breaking Compose slot tables and restart group functions
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Compose stable markers and snapshot observers
-keep class androidx.compose.runtime.** { *; }
-dontwarn androidx.compose.**

# ------------------------------------------------------------------------------
# 6. COROUTINES & KOTLIN RUNTIME
# ------------------------------------------------------------------------------
# Ensure Coroutines continuation dispatchers remain functional
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory { *; }
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler { *; }
-keepclassmembernames class kotlinx.coroutines.** {
    volatile <fields>;
}

# ------------------------------------------------------------------------------
# 7. SUPPRESS NON-FATAL WARNINGS
# ------------------------------------------------------------------------------
-dontwarn android.hardware.**
-dontwarn android.app.**
-dontwarn com.google.android.material.**
-dontnote **
