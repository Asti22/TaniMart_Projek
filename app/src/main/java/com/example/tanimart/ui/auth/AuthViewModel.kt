package com.example.tanimart.ui.auth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.tanimart.data.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Listener untuk mematikan koneksi snapshot saat tidak dibutuhkan
    private var profileListener: ListenerRegistration? = null

    // State untuk memantau status login Firebase (UID & Email)
    var currentUser = mutableStateOf<FirebaseUser?>(auth.currentUser)

    // State untuk menyimpan data profil lengkap (Alamat, NoHp, Role, dll)
    // State ini akan otomatis update setiap kali ada perubahan di Firestore
    var userData = mutableStateOf<UserModel?>(null)

    // State UI
    var isLoading = mutableStateOf(false)
    var authState = mutableStateOf<Result<Boolean>?>(null)

    init {
        checkUserStatus()
    }

    private fun checkUserStatus() {
        val user = auth.currentUser
        if (user != null) {
            currentUser.value = user
            listenDataProfil(user.uid)
        }
    }

    // --- FUNGSI REAL-TIME PROFIL ---
    fun listenDataProfil(uid: String) {
        profileListener?.remove()

        profileListener = db.collection("users").document(uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener

                if (snapshot != null && snapshot.exists()) {
                    userData.value = snapshot.toObject(UserModel::class.java)
                }
            }
    }

    // --- FUNGSI UPDATE ALAMAT UTAMA ---
    // Dipanggil dari AddressListScreen saat user memilih salah satu alamat dari daftar
    fun updateAlamatUtama(uid: String, alamatBaru: String, onSuccess: () -> Unit = {}) {
        db.collection("users").document(uid)
            .update("alamat", alamatBaru)
            .addOnSuccessListener {
                onSuccess()
            }
    }

    // --- FUNGSI REGISTER ---
    fun register(user: UserModel, pass: String) {
        isLoading.value = true
        auth.createUserWithEmailAndPassword(user.email, pass)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                val dataLengkap = user.copy(uid = userId)

                db.collection("users").document(userId).set(dataLengkap)
                    .addOnSuccessListener {
                        isLoading.value = false
                        currentUser.value = auth.currentUser
                        listenDataProfil(userId)
                        authState.value = Result.success(true)
                    }
                    .addOnFailureListener { e ->
                        isLoading.value = false
                        authState.value = Result.failure(e)
                    }
            }
            .addOnFailureListener { e ->
                isLoading.value = false
                authState.value = Result.failure(e)
            }
    }

    // --- FUNGSI LOGIN ---
    fun login(email: String, pass: String, onResult: (String?) -> Unit) {
        isLoading.value = true
        auth.signInWithEmailAndPassword(email, pass)
            .addOnSuccessListener { result ->
                val userId = result.user?.uid ?: ""
                currentUser.value = auth.currentUser
                listenDataProfil(userId)

                db.collection("users").document(userId).get()
                    .addOnSuccessListener { document ->
                        isLoading.value = false
                        val profil = document.toObject(UserModel::class.java)
                        onResult(profil?.role)
                    }
                    .addOnFailureListener {
                        isLoading.value = false
                        onResult(null)
                    }
            }
            .addOnFailureListener {
                isLoading.value = false
                onResult(null)
            }
    }
    fun getUserData(uid: String) {
        db.collection("users").document(uid).addSnapshotListener { snapshot, error ->
            if (error != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                userData.value = snapshot.toObject(UserModel::class.java)
            }
        }
    }

    fun logout() {
        profileListener?.remove()
        auth.signOut()
        currentUser.value = null
        userData.value = null
    }

    fun resetState() {
        authState.value = null
    }

    override fun onCleared() {
        super.onCleared()
        profileListener?.remove()
    }
}