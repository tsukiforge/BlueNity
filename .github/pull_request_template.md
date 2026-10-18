## Ringkasan Perubahan (Summary)
Penjelasan singkat mengenai perbaikan atau penambahan fitur yang dilakukan pada Pull Request ini.

## Jenis Perubahan
- [ ] Bug fix (perbaikan masalah)
- [ ] Fitur baru (penambahan fitur tanpa merusak fungsi yang sudah ada)
- [ ] Optimasi performa / memori RAM
- [ ] Pembaruan dokumentasi

## Checklist Kualitas & Kepatuhan Android
- [ ] **Pure Non-Root:** Tidak ada penggunaan Root, Shizuku, ADB, atau Accessibility abuse.
- [ ] **Real Implementation:** Tidak ada data mockup, animasi palsu "pembersihan RAM", atau klaim Force Stop otomatis yang tidak didukung API.
- [ ] **Efisiensi Memori:** Kode tidak menimbulkan memory leak pada perangkat RAM 2 GB.
- [ ] **Tests:** Unit test atau Robolectric test telah dijalankan (`./gradlew :app:testDebugUnitTest`) dan berhasil lulus 100%.
- [ ] **Package Visibility:** Tidak menggunakan izin berbahaya yang melanggar Google Play Policy (`QUERY_ALL_PACKAGES`).

## Pengujian yang Telah Dilakukan
Jelaskan pengujian yang telah dijalankan (misal: Robolectric tests, pengujian lokal emulator, atau uji coba langsung di Xiaomi Redmi 9A).
