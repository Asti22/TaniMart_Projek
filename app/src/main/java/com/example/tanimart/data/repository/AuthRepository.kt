package com.example.tanimart.data.repository

import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Fungsi Registrasi yang sudah diperbarui dengan UpdateProfile
    suspend fun registerUser(user: UserModel, password: String): Result<String> {
        return try {
            // 1. Buat akun di Firebase Auth
            val result = auth.createUserWithEmailAndPassword(user.email, password).await()
            val firebaseUser = result.user ?: throw Exception("Gagal mendapatkan User")
            val uid = firebaseUser.uid

            // 2. UPDATE PROFILE: Simpan nama toko ke DisplayName Firebase Auth
            // Ini agar ProfileScreen bisa baca user?.displayName
            val profileUpdates = userProfileChangeRequest {
                displayName = user.nama // 'user.nama' adalah Nama Toko yang diinput petani
            }
            firebaseUser.updateProfile(profileUpdates).await()

            // 3. Simpan data lengkap ke Firestore
            val finalUser = user.copy(uid = uid)
            db.collection("users").document(uid).set(finalUser).await()

            Result.success("Registrasi Berhasil")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Fungsi Login
    suspend fun loginUser(email: String, password: String): Result<UserModel> {
        return try {
            // 1. Login ke Firebase Auth
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID tidak ditemukan")

            // 2. Ambil data Role dan Profil dari Firestore
            val snapshot = db.collection("users").document(uid).get().await()
            val userProfile = snapshot.toObject(UserModel::class.java)

            if (userProfile != null) {
                Result.success(userProfile)
            } else {
                throw Exception("Data profil tidak ditemukan")
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    fun logout() {
        auth.signOut()
    }
}