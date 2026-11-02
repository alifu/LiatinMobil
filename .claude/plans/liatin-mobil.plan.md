# Plan: LiatinMobil — Simulasi Animasi Mobil di Peta (Offline)

**Sumber**: permintaan langsung (mode percakapan)
**Kompleksitas**: Medium
**Status**: MENUNGGU KONFIRMASI

## Ringkasan
Aplikasi Android (Kotlin + Jetpack Compose) untuk anak-anak yang meniru pengalaman "melihat mobil bergerak di peta" seperti aplikasi ride-hailing. Ada 3 halaman: Dashboard → Pilihan Mobil → Animasi Mobil di Peta. Semua data (10 rute dummy, peta, gambar mobil) dibundel di dalam APK, sehingga **tidak butuh internet sama sekali**. Animasi pelan, mulus, dan berdurasi ≥ 5 menit.

## Restatement Kebutuhan
| # | Kebutuhan | Interpretasi |
|---|---|---|
| 1 | 3 halaman | `DashboardScreen`, `CarSelectionScreen`, `RideMapScreen` |
| 2 | 10 rute dummy berbeda | 10 rute fiktif hardcoded (data lokal, bukan web/API) — tiap rute punya nama asal/tujuan, bentuk jalan, dan landmark berbeda |
| 3 | Animasi seperti Grab, pelan | Mobil bergerak mengikuti garis rute, berbelok (rotasi sesuai arah), garis rute "terlewati" berubah warna, kamera mengikuti mobil, label ETA menghitung mundur |
| 4 | Durasi ≥ 5 menit | Durasi default 6 menit, bisa dipilih 5 / 8 / 10 menit |
| 5 | Warna hijau & putih dominan | Tema Material3 kustom: hijau primer `#00B14F`, putih latar, abu-abu muda untuk jalan |
| 6 | Tanpa internet | Tidak ada permission `INTERNET`; peta digambar sendiri dengan Compose `Canvas` (bukan Google Maps / tile online) |
| 7 | Compose + Kotlin | Single-activity, Compose Navigation |
| 8 | README | Jelaskan fungsi app, cara build, struktur kode |

> **Catatan merek**: Gaya visual *terinspirasi* (hijau-putih, ikon mobil top-down), tapi **tidak memakai logo, nama, atau aset "Grab"**. Nama app tetap "LiatinMobil".

## Kondisi Codebase Saat Ini
Proyek baru dari template Android Studio (commit `e3c3e0f chore: init project`).

## Patterns to Mirror
| Kategori | Sumber | Pola |
|---|---|---|
| Naming / package | `app/src/main/java/com/alifu/liatinmobil/MainActivity.kt:1` | Package `com.alifu.liatinmobil`, PascalCase untuk Composable |
| Theme | `app/src/main/java/com/alifu/liatinmobil/ui/theme/Theme.kt` | `LiatinMobilTheme` membungkus `MaterialTheme` — diganti palet hijau |
| Dependensi | `gradle/libs.versions.toml` | Version catalog; semua lib baru ditambahkan di sini |
| Tests | `app/src/test/java/com/alifu/liatinmobil/ExampleUnitTest.kt:1` | JUnit4 + `org.junit.Assert` |
| Error handling / logging / data access | — | Belum ada pola di codebase; akan dibuat baru (lihat Fase 1) |

## Arsitektur

```
ui/
  theme/            Color.kt, Theme.kt, Type.kt  (palet hijau-putih)
  navigation/       AppNavHost.kt, Destinations.kt
  dashboard/        DashboardScreen.kt, RouteCard.kt
  carselection/     CarSelectionScreen.kt, CarOptionCard.kt
  ride/             RideMapScreen.kt, RideViewModel.kt, RideUiState.kt
    map/            CityMapCanvas.kt (jalan, blok, taman, sungai)
                    RoutePolyline.kt (garis rute + titik asal/tujuan)
                    CarMarker.kt (mobil top-down + rotasi)
                    RideBottomSheet.kt (status, ETA, info "driver")
domain/
  model/            Route.kt, MapPoint.kt, CarType.kt, RidePhase.kt
  geometry/         PolylineMath.kt (panjang kumulatif, interpolasi, bearing)
  simulation/       RideSimulator.kt (progress → posisi, sudut, fase, ETA)
data/
  RouteRepository.kt          (interface)
  LocalRouteRepository.kt     (10 rute hardcoded)
  CarCatalog.kt               (daftar jenis mobil)
```

- **State**: `RideViewModel` (lifecycle-viewmodel-compose) memegang `StateFlow<RideUiState>` yang immutable (`data class` + `copy`).
- **Navigasi**: `navigation-compose` dengan route type-safe: `Dashboard` → `CarSelection(routeId)` → `RideMap(routeId, carId, durationMin)`.
- **Peta offline**: koordinat rute memakai sistem koordinat "dunia" lokal (mis. 0..4000 unit). `CityMapCanvas` menggambar grid jalan + blok bangunan + taman/sungai yang sama untuk semua rute; tiap rute adalah polyline di atas grid jalan itu. Kamera = `translate` + `scale` pada Canvas agar mobil tetap di tengah.

## Desain Animasi (inti "rasa Grab")
1. **Gerak berbasis jarak, bukan titik**: hitung panjang kumulatif polyline → `progress (0..1)` dipetakan ke jarak → posisi diinterpolasi linier di segmen. Kecepatan konstan & mulus walau panjang segmen beda.
2. **Rotasi halus**: bearing segmen diinterpolasi dengan *shortest-angle lerp* saat mendekati tikungan (± 20 unit sebelum belokan), sehingga mobil "membelok", bukan langsung patah.
3. **Fase perjalanan** (ditampilkan di bottom sheet):
   - `MENCARI_DRIVER` (±10 dtk, animasi radar hijau berdenyut)
   - `DRIVER_MENUJU` (mobil datang dari titik awal ke penjemputan, ~20% durasi)
   - `MENJEMPUT` (berhenti 5 dtk, ikon penumpang)
   - `DALAM_PERJALANAN` (sisa durasi)
   - `TIBA` (konfeti/bintang sederhana, tombol "Main Lagi")
4. **Garis rute**: bagian yang sudah dilewati abu-abu muda, sisa rute hijau tebal — seperti app asli.
5. **Kamera follow**: kamera mengikuti mobil dengan smoothing (lerp 0.1 per frame) + tombol "lihat seluruh rute".
6. **"Lampu merah" kecil**: 2–3 titik berhenti acak-deterministik per rute (3–8 dtk) agar terasa hidup & menambah durasi alami.
7. **Clock**: loop `withFrameNanos` di ViewModel/LaunchedEffect; waktu disimpan sebagai `elapsedMs` sehingga bisa **pause/resume** dan bertahan saat rotasi layar (`SavedStateHandle`).
8. Layar tetap menyala selama perjalanan (`FLAG_KEEP_SCREEN_ON` via `DisposableEffect`).

## 10 Rute Dummy
| ID | Nama | Ciri bentuk |
|---|---|---|
| 1 | Rumah → Sekolah | Pendek, 3 belokan |
| 2 | Rumah → Taman Bermain | Melewati taman |
| 3 | Sekolah → Kebun Binatang | Panjang, menyusuri sungai |
| 4 | Rumah → Rumah Nenek | Zig-zag di perumahan |
| 5 | Mall → Bandara | Jalan raya lurus panjang |
| 6 | Stasiun → Pantai | Melengkung (bundaran) |
| 7 | Rumah → Rumah Sakit | Melewati jembatan |
| 8 | Pasar → Masjid Raya | Banyak belokan pendek |
| 9 | Perpustakaan → Kolam Renang | Memutar blok |
| 10 | Rumah → Toko Es Krim | Lewat jalan tol/flyover |

Setiap rute memiliki: `id`, `name`, `origin`/`destination` (label + emoji ikon), `points: List<MapPoint>`, `stops: List<StopPoint>`.

## Pilihan Mobil (Halaman 2)
Kartu bergaya pilihan layanan: **Mobil Hemat (hatchback)**, **Mobil Nyaman (sedan)**, **Mobil Keluarga (SUV/MPV)**, **Taksi**, **Mobil Listrik**. Masing-masing punya warna & gambar vector top-down sendiri (`res/drawable/car_*.xml`), harga "pura-pura" (mis. "⭐ 3 bintang" daripada rupiah — lebih ramah anak), dan pilihan durasi 5/6/8/10 menit.

## Files to Change
| File | Aksi | Alasan |
|---|---|---|
| `gradle/libs.versions.toml` | UPDATE | Tambah navigation-compose, lifecycle-viewmodel-compose, kotlinx-serialization, kotlinx-coroutines-test |
| `app/build.gradle.kts` | UPDATE | Tambah plugin serialization + dependensi |
| `app/src/main/AndroidManifest.xml` | UPDATE | Kunci orientasi portrait; pastikan tanpa `INTERNET` |
| `ui/theme/Color.kt`, `Theme.kt` | UPDATE | Palet hijau-putih, nonaktifkan dynamic color |
| `MainActivity.kt` | UPDATE | Host `AppNavHost` |
| `domain/**`, `data/**`, `ui/**` (lihat Arsitektur) | CREATE | Fitur utama |
| `res/drawable/car_*.xml`, ikon landmark | CREATE | Aset vector lokal |
| `res/values/strings.xml` | UPDATE | Semua teks UI (Bahasa Indonesia) |
| `app/src/test/.../PolylineMathTest.kt`, `RideSimulatorTest.kt`, `LocalRouteRepositoryTest.kt` | CREATE | Unit test |
| `app/src/androidTest/.../AppFlowTest.kt` | CREATE | UI test alur 3 halaman |
| `README.md` | CREATE | Penjelasan fungsi app |
| `.gitignore` | UPDATE | Abaikan `.claude/plans/` |

## Model per Fase
Mengacu pada strategi pemilihan model (Haiku = tugas ringan/mekanis, Sonnet = pengembangan utama, Opus = penalaran mendalam).

| Fase / Task | Model | Alasan |
|---|---|---|
| Fase 1 · Task 1 — Dependensi & tema | **Haiku 5.5** (`claude-haiku-5-5`) | Mekanis: edit version catalog, build file, palet warna |
| Fase 1 · Task 2 — PolylineMath | **Sonnet 5.5** (`claude-sonnet-5-5`) | Logika geometri + tes, perlu teliti tapi lingkupnya jelas |
| Fase 1 · Task 3 — 10 rute dummy + CarCatalog | **Sonnet 5.5** | Merancang 10 polyline yang benar-benar mengikuti grid jalan |
| Fase 2 · Task 4 — RideSimulator | **Opus 5.5** (`claude-opus-5-5`) | Inti "rasa Grab": pemetaan waktu→fase→jarak, stop point, smoothing belokan; rawan bug di batas waktu |
| Fase 2 · Task 5 — RideViewModel | **Sonnet 5.5** | Frame clock, pause/resume, SavedStateHandle, tes coroutine |
| Fase 3 · Task 6 — Navigasi + Dashboard | **Sonnet 5.5** | UI Compose standar + navigasi type-safe |
| Fase 3 · Task 7 — Pilihan Mobil | **Sonnet 5.5** | UI Compose standar, aset vector mobil |
| Fase 3 · Task 8 — Peta & Animasi | **Opus 5.5** | Canvas kustom, kamera follow, performa per-frame, layer caching |
| Fase 4 · Task 9 — Polish & aksesibilitas | **Haiku 5.5** | Perubahan kecil dan terarah (keep-screen-on, ukuran tombol, contentDescription) |
| Fase 4 · Task 10 — UI test alur | **Sonnet 5.5** | Tes Compose UI, perlu memahami alur navigasi |
| Fase 4 · Task 11 — README | **Haiku 5.5** | Dokumentasi dari kode yang sudah jadi |
| Review kode (setelah tiap fase) | **Sonnet 5.5** | Review Kotlin/Compose (agent `ecc:kotlin-reviewer`) |
| Perbaikan build gagal | **Haiku 5.5**, naik ke **Sonnet 5.5** bila tidak selesai | Error build biasanya lokal & minimal |

> Ganti model dengan `/model` sebelum mengerjakan tiap fase, atau set `model` saat memanggil subagent.

## Tasks (urutan TDD)

### Fase 1 — Fondasi
_Model: Haiku 5.5 (Task 1), Sonnet 5.5 (Task 2–3)_
**Task 1: Dependensi & tema**
- Action: update version catalog + build file; ganti `Color.kt`/`Theme.kt` ke palet hijau-putih (`GreenPrimary #00B14F`, `GreenDark #00873C`, `GreenLight #E6F7EE`, `RoadGray #E0E0E0`, `White`).
- Validate: `./gradlew :app:assembleDebug`

**Task 2: Model & geometri (tes dulu)**
- Action: tulis `PolylineMathTest` (panjang total, interpolasi di 0/0.5/1, bearing 0°/90°/180°/270°, shortest-angle lerp 350°→10°), lalu implementasi `PolylineMath`.
- Validate: `./gradlew :app:testDebugUnitTest`

**Task 3: Data rute (tes dulu)**
- Action: tes "ada tepat 10 rute", "ID unik", "setiap rute ≥ 2 titik", "titik di dalam batas peta"; implementasi `LocalRouteRepository` + `CarCatalog`.
- Validate: `./gradlew :app:testDebugUnitTest`

### Fase 2 — Simulasi
_Model: Opus 5.5 (Task 4), Sonnet 5.5 (Task 5)_

**Task 4: RideSimulator (tes dulu)**
- Action: fungsi murni `snapshotAt(elapsedMs, route, durationMs): RideSnapshot` → posisi, bearing, fase, sisa ETA, progress. Tes: fase benar di tiap batas waktu, berhenti di stop point, total durasi = pilihan user, `TIBA` saat elapsed ≥ durasi.
- Validate: `./gradlew :app:testDebugUnitTest`

**Task 5: RideViewModel**
- Action: frame clock + pause/resume + `SavedStateHandle`; tes dengan `kotlinx-coroutines-test`.

### Fase 3 — UI
_Model: Sonnet 5.5 (Task 6–7), Opus 5.5 (Task 8)_

**Task 6: Navigasi + Dashboard**
- Dashboard: header hijau "Mau ke mana hari ini?", grid/daftar 10 kartu rute (emoji asal→tujuan), banner ilustrasi mobil.

**Task 7: Halaman Pilihan Mobil**
- Peta mini rute di atas, daftar kartu mobil, chip durasi, tombol besar hijau "Pesan Sekarang".

**Task 8: Halaman Peta & Animasi**
- `CityMapCanvas` + `RoutePolyline` + `CarMarker` + `RideBottomSheet`; tombol pause, "lihat seluruh rute", kembali.

### Fase 4 — Polish & Dokumen
_Model: Haiku 5.5 (Task 9, 11), Sonnet 5.5 (Task 10)_

**Task 9**: keep-screen-on, tombol besar ramah anak (min 56dp), aksesibilitas (contentDescription).
**Task 10**: UI test alur Dashboard → Pilih Mobil → Peta → tampil "Driver menuju".
**Task 11**: `README.md` (fungsi app, fitur, screenshot placeholder, cara build, struktur folder, catatan offline & merek).

## Validation
```bash
./gradlew :app:testDebugUnitTest          # unit test
./gradlew :app:connectedDebugAndroidTest  # UI test (butuh emulator)
./gradlew :app:lintDebug
./gradlew :app:assembleDebug
# Uji offline: nyalakan mode pesawat di emulator lalu jalankan app
```

## Risks
| Risiko | Kemungkinan | Mitigasi |
|---|---|---|
| Peta buatan sendiri terlihat kurang "nyata" | Sedang | Detail visual: blok bangunan, taman hijau, sungai biru, nama jalan, bayangan mobil |
| Animasi patah-patah di HP lama (minSdk 24) | Rendah–Sedang | Gambar peta statis di-cache sebagai layer terpisah; hanya mobil & garis progres yang digambar ulang per frame |
| Kemiripan dengan merek Grab (hak cipta/merek) | Sedang | Tidak memakai logo/nama/aset Grab; hanya skema warna umum |
| Rotasi layar / app ke background mereset perjalanan | Sedang | `elapsedMs` di `SavedStateHandle`; auto-pause di `ON_PAUSE` |
| Versi lib lama di template (`coreKtx 1.10.1`, `lifecycle 2.6.1`) tak cocok dengan compileSdk 37 | Rendah | Naikkan ke versi stabil terbaru saat Task 1 |
| Layar mati saat anak menonton 5+ menit | Tinggi jika tidak ditangani | `FLAG_KEEP_SCREEN_ON` hanya di halaman peta |

## Estimasi
- Fase 1: 2–3 jam · Fase 2: 2–3 jam · Fase 3: 5–7 jam · Fase 4: 2 jam
- **Total: ±11–15 jam**

## Acceptance
- [ ] 3 halaman berfungsi dan tersambung
- [ ] 10 rute dummy berbeda bisa dipilih
- [ ] Animasi mulus, mobil berbelok halus, durasi ≥ 5 menit (bisa dipilih)
- [ ] Tema dominan hijau & putih
- [ ] Berjalan normal dalam mode pesawat; tidak ada permission INTERNET
- [ ] Unit test lulus, coverage domain/simulasi ≥ 80%
- [ ] README menjelaskan fungsi app
