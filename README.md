# Game Prep — Production-Ready Non-Root App Manager

**Game Prep** adalah aplikasi pengelola aplikasi sebelum bermain game yang dirancang khusus untuk smartphone Android dengan RAM terbatas, terutama **Xiaomi Redmi 9A (RAM 2 GB, MIUI 12, Android 10/11+)**.

Aplikasi ini dibangun dengan prinsip kejujuran sistemik (**Pure Non-Root & System Transparency**). Tidak ada klaim palsu "membersihkan 2 GB RAM", tidak ada tombol bohongan, tidak menggunakan Accessibility Service untuk klik otomatis tersembunyi, dan tidak memerlukan Shizuku maupun akses root.

---

## 🚀 Fitur Utama

1. **Package Visibility Sah & Aman (Android 10 - Android 14+):**
   - Mendeteksi aplikasi yang dapat diluncurkan (launcher apps) menggunakan kueri resmi `<queries>` tanpa meminta izin invasif `QUERY_ALL_PACKAGES`.
   - Mengelompokkan aplikasi ke dalam kategori Game, Sistem, dan Aplikasi biasa.

2. **Sistem Perlindungan Aplikasi (Protected Apps):**
   - Secara default melindungi aplikasi penting: Telepon, SMS, Jam Alarm, Keyboard sistem (InputMethodManager), dan Game Prep itu sendiri.
   - Tombol "Pilih Semua" secara ketat mengecualikan aplikasi yang dilindungi demi mencegah gangguan panggilan darurat atau alarm.
   - Perlindungan dapat diatur secara manual oleh pengguna dengan dialog konfirmasi keamanan.

3. **Persiapan Terpandu (Guided Preparation):**
   - Mengimplementasikan alur resmi `Settings.ACTION_APPLICATION_DETAILS_SETTINGS` dengan URI `package:<nama_paket>`.
   - Memandu pengguna langkah demi langkah menuju halaman Info Aplikasi sistem untuk menekan tombol **Paksa Berhenti (Force Stop)** secara sah.
   - Transparan: Tidak mengklaim aplikasi tertutup otomatis jika sistem tidak menyediakannya.

4. **Profil Game Mandiri (Game Profiles):**
   - Menyimpan preferensi pemilihan aplikasi secara terpisah untuk setiap game (misal: Mobile Legends, Roblox, Minecraft).
   - Data profil disimpan secara lokal dan persisten menggunakan **Jetpack DataStore Preferences**.

5. **Monitor RAM Aktual (Hardware Accurate):**
   - Membaca memori fisik waktu-nyata via `ActivityManager.MemoryInfo`: Total RAM, Available Memory, Low-Memory Threshold, dan Status Low-Memory.
   - Menampilkan footprint memori Game Prep sendiri (JVM Heap & PSS) yang dirancang sangat minim (< 25 MB).
   - Dilengkapi penjelasan teknis mengenai perbedaan Available RAM vs Reclaimable Cache RAM.

6. **Panduan MIUI 12 & Optimasi Baterai:**
   - Panduan pengaturan Autostart dan Penghemat Baterai MIUI.
   - Pintasan langsung ke pengaturan baterai sistem tanpa meminta izin berbahaya.

### Catatan performa

- Tombol **Mulai Game** tidak lagi menjalankan pembersihan proses otomatis dan tidak memanggil `System.gc()`, karena kedua tindakan tersebut dapat menyebabkan stutter pada perangkat RAM rendah.
- Setelah game berhasil dibuka, Activity Game Prep ditutup agar komposisi UI, ikon, dan state layar dilepas lebih cepat.
- **Bersihkan RAM** adalah tindakan manual best-effort dan diproses di background. Android/MIUI tetap menentukan proses mana yang boleh dihentikan.

---

## 🏛️ Arsitektur & Teknologi

- **Bahasa:** Kotlin 100%
- **Pola Arsitektur:** Clean Architecture + MVVM (Model-View-ViewModel)
- **UI Toolkit:** Jetpack Compose & Material Design 3 (M3)
- **State Management:** `StateFlow`, `collectAsState`, Coroutines
- **Penyimpanan Lokal:** Jetpack DataStore Preferences
- **Minimum SDK:** API 29 (Android 10)
- **Target SDK:** API 36
- **Package ID:** `com.aistudio.gameprep.optimizer`

### Struktur Direktori:
```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/
│   ├── MainActivity.kt
│   ├── data/
│   │   ├── model/
│   │   │   ├── AppItem.kt
│   │   │   ├── GameProfile.kt
│   │   │   └── MemoryStats.kt
│   │   └── repository/
│   │       ├── AppManagerRepository.kt
│   │       ├── PreferencesRepository.kt
│   │       └── SystemInfoRepository.kt
│   └── ui/
│       ├── components/
│       │   ├── AppIconImage.kt
│       │   ├── AppItemCard.kt
│       │   ├── GuidedPrepDialog.kt
│       │   ├── MemoryOverviewCard.kt
│       │   └── ProfileDialogs.kt
│       ├── screens/
│       │   ├── AppManagerScreen.kt
│       │   ├── GameProfilesScreen.kt
│       │   ├── HomeScreen.kt
│       │   ├── MemoryMonitorScreen.kt
│       │   ├── ProtectedAppsScreen.kt
│       │   └── SettingsScreen.kt
│       └── theme/
│           ├── Color.kt
│           ├── Theme.kt
│           └── Type.kt
└── res/
    ├── drawable/
    │   ├── ic_launcher_background.xml
    │   └── ic_launcher_foreground.xml
    └── values/
        ├── strings.xml
        └── themes.xml
```

---

## 🔒 Kebijakan Izin & Privasi (Privacy Policy)

- **Zero Dangerous Permissions:** Game Prep tidak memerlukan izin SMS, Kontak, Kamera, Mikrofon, Lokasi, atau Penyimpanan file.
- **Tanpa Akses Internet:** Aplikasi beroperasi 100% secara lokal di perangkat Anda. Tidak ada data profil, daftar aplikasi, atau analitik yang dikirim ke server.
- **Kepatuhan Google Play:** Deklarasi `<queries>` spesifik untuk Launcher, Telepon, SMS, dan Alarm tanpa menyalahgunakan `QUERY_ALL_PACKAGES`.

---

## 🛠️ Langkah Membangun & Menguji APK

### 1. Membangun APK Debug:
Jalankan perintah Gradle berikut di terminal proyek:
```bash
gradle assembleDebug
```
File APK akan dihasilkan di direktori:
`app/build/outputs/apk/debug/app-debug.apk`

### 2. Membangun Full Release (APK & AAB):
Untuk menghasilkan APK dan Google Play App Bundle (AAB) versi produksi:
```bash
# Membangun Release APK
gradle assembleRelease

# Membangun Release App Bundle (AAB untuk Google Play)
gradle bundleRelease
```
Hasil build produksi:
- APK Release: `app/build/outputs/apk/release/app-release.apk`
- AAB Bundle: `app/build/outputs/bundle/release/app-release.aab`

### 3. Otomasi GitHub Actions CI/CD:
Proyek ini telah dikonfigurasi dengan alur kerja GitHub Actions otomatis:
- `.github/workflows/android-ci.yml`: Berjalan otomatis pada setiap *push* atau *pull request* ke branch `main`/`master` untuk menjalankan verifikasi kompilasi, seluruh Robolectric test suite, dan membangun debug APK sebagai artefak yang dapat diunduh.
- `.github/workflows/android-release.yml`: Berjalan otomatis ketika Anda membuat tag rilis (misal `git tag v1.0.0 && git push origin v1.0.0`) untuk menandatangani APK dan AAB, kemudian otomatis mempublikasikan rilis di tab **Releases** GitHub lengkap dengan file unduhan dan catatan rilis.

### 4. Menjalankan Unit & Robolectric Tests:
```bash
gradle :app:testDebugUnitTest
```

### 3. Memasang & Menguji di Xiaomi Redmi 9A:
1. Aktifkan **Opsi Pengembang (Developer Options)** di Redmi 9A:
   - Masuk ke **Pengaturan** -> **Tentang Ponsel**.
   - Ketuk **Versi MIUI** sebanyak 7 kali berturut-turut hingga muncul pesan pengembang.
2. Aktifkan **Debugging USB** di menu Opsi Pengembang.
3. Hubungkan Redmi 9A ke komputer menggunakan kabel data.
4. Pasang APK menggunakan ADB:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
5. Buka Game Prep, periksa daftar aplikasi, lakukan persiapan terpandu pada salah satu game (misal Mobile Legends), dan amati stabilitas RAM.
