package com.example.tanimart.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.tanimart.data.model.ProductModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class ProductRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Fungsi Upload Gambar ke Cloudinary
    fun uploadKeCloudinary(uri: Uri, onResult: (String?) -> Unit) {
        MediaManager.get().upload(uri)
            .unsigned("uedmlqmo") // Saran: Nantinya pindahkan ke Constants/BuildConfig
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String?, resultData: Map<*, *>?) {
                    val url = resultData?.get("secure_url") as? String
                    onResult(url)
                }
                override fun onError(requestId: String?, error: ErrorInfo?) {
                    onResult(null)
                }
                override fun onStart(requestId: String?) {}
                override fun onProgress(requestId: String?, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String?, error: ErrorInfo?) {}
            }).dispatch()
    }

    /**
     * Menambahkan produk baru ke Firestore.
     * Menerima objek ProductModel utuh dari ViewModel (Clean Architecture Approach).
     */
    suspend fun addProduct(product: ProductModel): Result<String> {
        return try {
            // Membuat referensi dokumen baru untuk mendapatkan ID otomatis
            val docRef = db.collection("products").document()

            // Masukkan ID dokumen tersebut ke dalam field 'id' di model
            val finalProduct = product.copy(id = docRef.id)

            // Simpan data ke Firestore
            docRef.set(finalProduct).await()

            Result.success("Produk berhasil ditambahkan!")
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mengambil produk milik Petani yang sedang login
    suspend fun getAllProducts(): Result<List<ProductModel>> {
        return try {
            val currentId = auth.currentUser?.uid ?: return Result.success(emptyList())
            val snapshot = db.collection("products")
                .whereEqualTo("idPetani", currentId)
                .get()
                .await()

            val products = snapshot.toObjects(ProductModel::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Menghapus produk berdasarkan ID
    suspend fun deleteProduct(productId: String): Result<Unit> {
        return try {
            db.collection("products").document(productId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Update sebagian data produk (misal: stok saja atau harga saja)
    suspend fun updateProduct(productId: String, data: Map<String, Any>): Result<Unit> {
        return try {
            db.collection("products").document(productId).update(data).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // Mengambil semua produk untuk halaman Pembeli/Consumer
    suspend fun getAllProductsForConsumer(): Result<List<ProductModel>> {
        return try {
            val snapshot = db.collection("products").get().await()
            val products = snapshot.toObjects(ProductModel::class.java)
            Result.success(products)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}