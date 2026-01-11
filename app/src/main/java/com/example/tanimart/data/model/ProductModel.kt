package com.example.tanimart.data.model

/**
 * Model data untuk Produk TaniMart.
 * Diperbarui untuk mendukung fitur lokasi, estimasi pengiriman, dan satuan dinamis (kg/Pcs).
 */
data class ProductModel(
    val id: String = "",
    val idPetani: String = "",       // ID pemilik toko/petani
    val nama: String = "",
    val harga: Int = 0,
    val stok: Int = 0,
    val imageUrl: String = "",
    val kategori: String = "Sayur Daun", // Contoh: Sayur Daun, Sayur Akar, Buah, dll.
    val deskripsi: String = "",
    val jangkauanKirim: String = "Dekat", // Dekat (Instan) atau Jauh (Reguler)
    val namaToko: String = "",

    // --- FITUR SATUAN DINAMIS ---
    // Menyimpan apakah produk dijual per "kg", "Pcs", atau "Unit"
    val satuan: String = "kg",

    // --- FITUR ESTIMASI PENGIRIMAN ---
    val estimasiPengiriman: String = "",

    // --- FITUR LOKASI ---
    val lat: Double = 0.0,           // Koordinat Latitude lahan
    val lng: Double = 0.0,           // Koordinat Longitude lahan
    val lokasiLahan: String = "",    // Nama singkat wilayah (contoh: Sleman, Yogyakarta)
    val alamatLengkap: String = ""   // Alamat detail
) {
    /**
     * Fungsi helper untuk mendapatkan label estimasi secara otomatis
     * jika data estimasiPengiriman di database kosong.
     */
    fun getAutoEstimasi(): String {
        if (estimasiPengiriman.isNotEmpty()) return estimasiPengiriman

        return when (kategori) {
            "Sayur Daun" -> "Maksimal 1 Hari (Wajib Instan)"
            "Sayur Buah" -> "1-2 Hari"
            "Rempah", "Umbi-umbian" -> "2-3 Hari"
            "Bibit" -> "3-5 Hari" // Disamakan dengan kategori di UI
            else -> "2-3 Hari"
        }
    }

    /**
     * Helper untuk menentukan satuan otomatis jika data satuan di DB kosong (migrasi data lama)
     */
    fun getFormattedSatuan(): String {
        return if (satuan.isNotEmpty()) satuan
        else if (kategori == "Bibit") "Pcs"
        else "kg"
    }
}