package com.example.tanimart.data.model

data class UserModel(
    val uid: String = "",
    val nama: String = "",
    val email: String = "",
    val role: String = "",
    val alamat: String = "",
    val noHp: String = "",
    val namaToko: String? = null,
    val alamatLahan: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)