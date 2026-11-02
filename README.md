# 🚗 LiatinMobil

Aplikasi Android sederhana untuk anak-anak yang suka **melihat mobil bergerak di peta**, seperti saat menunggu mobil di aplikasi ojek/taksi online. Anak memilih tujuan, memilih mobil, lalu menonton mobil berjalan pelan-pelan di peta sampai tiba.

> Seluruh aplikasi berjalan **tanpa internet**. Tidak ada iklan, akun, pembayaran, atau data yang dikirim ke mana pun.

---

## ✨ Fungsi Aplikasi

### 1. Dashboard — "Mau ke mana hari ini?"
- Menampilkan **10 perjalanan contoh** dalam bentuk kartu besar dengan emoji (🏠 → 🏫, 🏫 → 🦁, 🛍️ → ✈️, dan lainnya).
- Setiap kartu menunjukkan asal, tujuan, dan jarak "pura-pura".

### 2. Pilih Mobil
- Pratinjau peta rute: titik jemput hijau dan pin tujuan oranye.
- 5 pilihan mobil: **Mobil Hemat, Mobil Nyaman, Mobil Keluarga, Taksi, Mobil Listrik**. Masing-masing punya warna sendiri.
- Pilihan lama perjalanan: **5, 6, 8, atau 10 menit**.
- Tombol besar **"Pesan Sekarang"**.

### 3. Animasi Mobil di Peta
Urutan perjalanannya dibuat mirip aplikasi ride-hailing:

| Tahap | Yang terlihat |
|---|---|
| 🔍 Mencari driver | Radar hijau berdenyut di sekitar mobil |
| 🚗 Driver menuju kamu | Mobil berjalan di jalur putus-putus menuju titik jemput, dengan ETA "Driver tiba dalam X menit" |
| 🚪 Menjemput | Mobil berhenti sebentar: "Ayo naik!" |
| 🛣️ Dalam perjalanan | Mobil mengikuti garis hijau. Bagian yang sudah dilewati berubah abu-abu. Kadang berhenti di **lampu merah 🚦** |
| 🎉 Tiba | "Hore! Sudah sampai", lalu tombol **Main Lagi** |

Fitur lain di halaman peta:
- Mobil **berbelok dengan halus** di tikungan dan bundaran, serta melambat atau mempercepat dengan lembut saat berhenti dan berangkat.
- Kamera **mengikuti mobil**. Tombol **🗺️ Lihat Rute** memperlihatkan seluruh rute.
- Info driver pura-pura (nama, plat nomor, jenis mobil).
- Tombol **⏸ Jeda / ▶ Lanjut**.
- **Layar tidak mati** selama perjalanan.
- Perjalanan tetap berlanjut dari posisi yang sama saat layar diputar.

---

## 🗺️ Kota & 10 Rute

Peta adalah **kota fiktif** yang digambar langsung oleh aplikasi (Jetpack Compose `Canvas`), bukan Google Maps. Kota ini berisi jalan kotak-kotak, jalan raya kuning, sungai dengan jembatan, taman, bundaran, dan jalan layang (flyover).

| # | Rute | Ciri |
|---|---|---|
| 1 | 🏠 Rumah → 🏫 Sekolah | Pendek, beberapa belokan |
| 2 | 🏠 Rumah → 🎠 Taman Bermain | Memutari taman |
| 3 | 🏫 Sekolah → 🦁 Kebun Binatang | Panjang, menyusuri sungai |
| 4 | 🏠 Rumah → 👵 Rumah Nenek | Zig-zag di perumahan |
| 5 | 🛍️ Mall → ✈️ Bandara | Jalan lurus panjang |
| 6 | 🚉 Stasiun → 🏖️ Pantai | Melewati bundaran |
| 7 | 🏠 Rumah → 🏥 Rumah Sakit | Menyeberang jembatan |
| 8 | 🧺 Pasar → 🕌 Masjid Raya | Banyak belokan pendek |
| 9 | 📚 Perpustakaan → 🏊 Kolam Renang | Memutar blok |
| 10 | 🏡 Rumah Teman → 🍦 Toko Es Krim | Lewat jalan layang |

---

## 🛠️ Teknologi

- **Kotlin** + **Jetpack Compose** (Material 3), single activity
- **Navigation Compose** dengan rute type-safe (`kotlinx.serialization`)
- **ViewModel** + `StateFlow` + `SavedStateHandle`
- Tidak ada library peta dan tidak ada izin `INTERNET`
- minSdk 24, targetSdk 37

## ▶️ Cara Menjalankan

```bash
./gradlew :app:installDebug              # pasang ke HP / emulator
./gradlew :app:testDebugUnitTest         # unit test
./gradlew :app:connectedDebugAndroidTest # UI test (butuh emulator/HP)
```

Atau buka folder proyek di Android Studio lalu tekan **Run**.

## 📁 Struktur Kode

```
app/src/main/java/com/alifu/liatinmobil/
├── data/                  # Data lokal (offline)
│   ├── CityLayout.kt      # Tata letak kota: jalan, sungai, taman, bundaran, flyover
│   ├── Landmarks.kt       # Tempat-tempat bernama (emoji)
│   ├── LocalRouteRepository.kt  # 10 rute contoh
│   └── CarCatalog.kt      # Pilihan mobil & driver pura-pura
├── domain/
│   ├── model/             # Route, CarType, RidePhase, RideDuration, ...
│   ├── geometry/          # Polyline: posisi & arah berdasarkan jarak tempuh
│   └── simulation/        # RideSimulator: waktu → posisi, arah, tahap, ETA
└── ui/
    ├── dashboard/         # Halaman 1
    ├── carselection/      # Halaman 2
    ├── ride/              # Halaman 3 + ViewModel
    │   └── map/           # Gambar kota, mobil, kamera, bottom sheet
    ├── navigation/        # NavHost & tujuan navigasi
    ├── common/            # Komponen bersama
    └── theme/             # Warna hijau & putih
```

### Cara kerja animasi (singkat)
1. `RideSimulator` menyusun **timeline**: cari driver → jalan ke titik jemput → jemput → jalan (dengan lampu merah) → tiba. Total waktunya sama persis dengan durasi yang dipilih.
2. Waktu jalan dibagi menurut **jarak**, jadi kecepatan mobil stabil. Setiap kali berangkat atau berhenti, kecepatannya naik-turun perlahan.
3. Setiap frame layar, `RideViewModel` menambah waktu berjalan. Simulator lalu mengembalikan posisi dan arah mobil, dan `CityMapCanvas` menggambarnya.

## ℹ️ Catatan
Gaya visual (hijau & putih, mobil dilihat dari atas) **terinspirasi** aplikasi ride-hailing. Aplikasi ini **tidak memakai** logo, nama, atau aset milik merek mana pun. Semua nama driver, plat nomor, dan tempat adalah fiktif.
