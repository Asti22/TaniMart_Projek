package com.example.tanimart.ui.product

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tanimart.data.model.OrderModel
import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.ui.consumer.NotificationData
import com.example.tanimart.data.repository.ProductRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    private val repo = ProductRepository()
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // --- LISTENER REGISTRATIONS ---
    private var cartListener: ListenerRegistration? = null
    private var addressListener: ListenerRegistration? = null
    private var notificationListener: ListenerRegistration? = null
    private var orderListener: ListenerRegistration? = null

    // --- STATE UI & NAVIGASI ---
    var isLoading = mutableStateOf(false)
    var isSuccess = mutableStateOf(false)
    var currentSelectedTab by mutableIntStateOf(0)
    var adaPesananBaru by mutableStateOf(false)

    // --- STATE DATA PRODUK ---
    var productList = mutableStateOf<List<ProductModel>>(emptyList())
    var selectedProduct by mutableStateOf<ProductModel?>(null)

    // --- STATE LOKASI PEMBELI ---
    var userLat by mutableStateOf<Double?>(null)
    var userLng by mutableStateOf<Double?>(null)

    // --- STATE KERANJANG & PESANAN ---
    val cartList = mutableStateListOf<ProductModel>()
    var orderList = mutableStateOf<List<OrderModel>>(emptyList())

    // --- STATE NOTIFIKASI & ALAMAT ---
    var notificationsList = mutableStateOf<List<NotificationData>>(emptyList())
    var addressList = mutableStateOf<List<Map<String, String>>>(emptyList())

    // --- STATE ALAMAT REALTIME PENJUAL ---
    var alamatPenjualTerbaru = mutableStateOf("Memuat lokasi...")

    // --- GETTERS ---
    val totalStok: Int get() = productList.value.sumOf { it.stok }
    val jumlahProduk: Int get() = productList.value.size

    // --- FUNGSI MEMBERSIHKAN DATA (SAAT LOGOUT) ---
    fun resetDataOnLogout() {
        cartListener?.remove()
        addressListener?.remove()
        notificationListener?.remove()
        orderListener?.remove()

        cartList.clear()
        orderList.value = emptyList()
        notificationsList.value = emptyList()
        addressList.value = emptyList()
        productList.value = emptyList()
        adaPesananBaru = false
        selectedProduct = null
    }

    // --- FUNGSI LOKASI ---
    fun updateLocation(lat: Double, lng: Double) {
        userLat = lat
        userLng = lng
    }

    // --- MANAJEMEN PRODUK (PETANI) ---
    fun ambilSemuaProduk() {
        viewModelScope.launch {
            isLoading.value = true
            val result = repo.getAllProducts()
            if (result.isSuccess) {
                productList.value = result.getOrNull() ?: emptyList()
            }
            isLoading.value = false
        }
    }

    fun simpanProduk(
        nama: String, harga: String, stok: String, url: String,
        kategori: String, lokasi: String, deskripsi: String, jangkauan: String,
        idPetani: String, namaToko: String,
        lat: Double, lng: Double,
        estimasiPengiriman: String,
        satuan: String
    ) {
        viewModelScope.launch {
            isLoading.value = true
            val produkBaru = ProductModel(
                nama = nama,
                harga = harga.toIntOrNull() ?: 0,
                stok = stok.toIntOrNull() ?: 0,
                imageUrl = url,
                kategori = kategori,
                lokasiLahan = lokasi,
                deskripsi = deskripsi,
                jangkauanKirim = jangkauan,
                idPetani = idPetani,
                namaToko = namaToko,
                lat = lat,
                lng = lng,
                estimasiPengiriman = estimasiPengiriman,
                satuan = satuan
            )
            val result = repo.addProduct(produkBaru)
            if (result.isSuccess) {
                isSuccess.value = true
                ambilSemuaProduk()
            }
            isLoading.value = false
        }
    }

    fun editProduk(
        productId: String, nama: String, harga: String, stok: String, url: String,
        kategori: String, lokasi: String, deskripsi: String, jangkauan: String,
        idPetani: String, namaToko: String,
        lat: Double, lng: Double,
        estimasiPengiriman: String,
        satuan: String
    ) {
        viewModelScope.launch {
            isLoading.value = true
            val updateData = mapOf(
                "nama" to nama,
                "harga" to (harga.toIntOrNull() ?: 0),
                "stok" to (stok.toIntOrNull() ?: 0),
                "imageUrl" to url,
                "kategori" to kategori,
                "lokasiLahan" to lokasi,
                "deskripsi" to deskripsi,
                "jangkauanKirim" to jangkauan,
                "idPetani" to idPetani,
                "namaToko" to namaToko,
                "lat" to lat,
                "lng" to lng,
                "estimasiPengiriman" to estimasiPengiriman,
                "satuan" to satuan
            )
            val result = repo.updateProduct(productId, updateData)
            if (result.isSuccess) {
                isSuccess.value = true
                ambilSemuaProduk()
            }
            isLoading.value = false
        }
    }

    fun hapusProduk(productId: String) {
        viewModelScope.launch {
            val result = repo.deleteProduct(productId)
            if (result.isSuccess) {
                ambilSemuaProduk()
            }
        }
    }

    // --- FUNGSI BELANJA (KONSUMEN) ---
    fun ambilSemuaProdukKonsumen() {
        viewModelScope.launch {
            isLoading.value = true
            val result = repo.getAllProductsForConsumer()
            if (result.isSuccess) {
                productList.value = result.getOrNull() ?: emptyList()
            }
            isLoading.value = false
        }
    }

    fun ambilDataKeranjang(userId: String) {
        cartListener?.remove()
        cartList.clear()

        cartListener = db.collection("users").document(userId).collection("cart")
            .addSnapshotListener { snapshot, e ->
                if (e != null) return@addSnapshotListener
                if (snapshot != null) {
                    val items = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ProductModel::class.java)?.copy(id = doc.id)
                    }
                    cartList.clear()
                    cartList.addAll(items)
                }
            }
    }

    fun addToCart(product: ProductModel, qty: Int) {
        val userId = auth.currentUser?.uid ?: return
        if (product.id.isEmpty()) return

        val cartItem = product.copy(stok = qty)
        db.collection("users").document(userId).collection("cart")
            .document(product.id)
            .set(cartItem)
            .addOnSuccessListener {
                ambilDataKeranjang(userId)
            }
    }

    fun removeFromCart(userId: String, productId: String) {
        db.collection("users").document(userId).collection("cart")
            .document(productId)
            .delete()
    }

    // --- FUNGSI HITUNG HARGA ---
    fun getTotalPrice(selectedItems: List<ProductModel>): Int {
        return selectedItems.sumOf { item -> (item.harga * item.stok) }
    }

    // --- FUNGSI CHECKOUT (FIXED: Hanya hapus produk yang dicheckout) ---
    fun checkout(
        userId: String,
        userEmail: String,
        itemsToCheckout: List<ProductModel>, // Menerima list produk yang dicentang saja
        onSuccess: () -> Unit
    ) {
        if (itemsToCheckout.isEmpty()) return
        isLoading.value = true

        val batch = db.batch()
        val orderId = "ORD-${System.currentTimeMillis()}"
        val orderRef = db.collection("orders").document(orderId)

        val newOrder = OrderModel(
            orderId = orderId,
            consumerId = userId,
            consumerEmail = userEmail,
            items = itemsToCheckout,
            totalPrice = getTotalPrice(itemsToCheckout),
            status = "Pending",
            timestamp = System.currentTimeMillis()
        )

        batch.set(orderRef, newOrder)


        itemsToCheckout.forEach { item ->
            // Potong stok di koleksi produk global
            val productRef = db.collection("products").document(item.id)
            batch.update(productRef, "stok", FieldValue.increment(-item.stok.toLong()))

            // Hapus item spesifik dari keranjang user
            val cartRef = db.collection("users").document(userId).collection("cart").document(item.id)
            batch.delete(cartRef)
        }

        batch.commit().addOnSuccessListener {
            kirimNotifikasiOtomatis(userId, "Pesanan Berhasil", "Pesanan #$orderId telah dibuat.", "order")
            isLoading.value = false
            onSuccess()
        }.addOnFailureListener { e ->
            Log.e("CheckoutError", "Gagal checkout: ${e.message}")
            isLoading.value = false
        }
    }

    // --- FUNGSI NOTIFIKASI ---
    fun monitorNotifications(userId: String) {
        notificationListener?.remove()
        notificationListener = db.collection("notifications")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    notificationsList.value = snapshot.toObjects(NotificationData::class.java)
                }
            }
    }

    fun markAsRead(notificationId: String) {
        db.collection("notifications").document(notificationId).update("isRead", true)
    }

    private fun kirimNotifikasiOtomatis(userId: String, title: String, message: String, type: String) {
        val notifId = db.collection("notifications").document().id
        val dataNotif = NotificationData(
            id = notifId, userId = userId, title = title, message = message,
            timestamp = System.currentTimeMillis(), isRead = false, type = type
        )
        db.collection("notifications").document(notifId).set(dataNotif)
    }

    // --- FUNGSI PESANAN (UNTUK SELLER) ---
    fun monitorPesananMasuk() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("orders")
            .whereEqualTo("status", "Pending")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    val allOrders = snapshot.toObjects(OrderModel::class.java)
                    adaPesananBaru = allOrders.any { order ->
                        order.items.any { item -> item.idPetani == currentUserId }
                    }
                }
            }
    }

    fun ambilPesananSaya(userId: String) {
        orderListener?.remove()
        orderListener = db.collection("orders")
            .whereEqualTo("consumerId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    orderList.value = snapshot.toObjects(OrderModel::class.java)
                }
            }
    }

    fun ambilSemuaPesananMasuk() {
        val currentUserId = auth.currentUser?.uid ?: return
        db.collection("orders")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val allOrders = snapshot.toObjects(OrderModel::class.java)
                    val filteredOrders = allOrders.filter { order ->
                        order.items.any { it.idPetani == currentUserId }
                    }
                    orderList.value = filteredOrders
                }
            }
    }

    fun updateStatusPesanan(orderId: String, consumerId: String, statusBaru: String) {
        db.collection("orders").document(orderId).update("status", statusBaru)
            .addOnSuccessListener {
                kirimNotifikasiOtomatis(consumerId, "Update Pesanan", "Status pesanan $orderId: $statusBaru", "order")
            }
    }

    fun resetNotifikasi() {
        adaPesananBaru = false
    }

    // --- ALAMAT & PROFIL ---
    fun ambilAlamatUser(userId: String) {
        addressListener?.remove()
        addressListener = db.collection("users").document(userId).collection("addresses")
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    addressList.value = snapshot.documents.map { doc ->
                        mapOf(
                            "id" to doc.id,
                            "label" to (doc.getString("label") ?: ""),
                            "detail" to (doc.getString("detail") ?: "")
                        )
                    }
                }
            }
    }

    fun tambahAlamat(userId: String, label: String, detail: String) {
        val data = mapOf(
            "label" to label,
            "detail" to detail,
            "timestamp" to FieldValue.serverTimestamp()
        )
        db.collection("users").document(userId).collection("addresses").add(data)
    }

    fun updateAlamat(userId: String, addressId: String, label: String, detail: String) {
        val data = mapOf("label" to label, "detail" to detail)
        db.collection("users").document(userId).collection("addresses").document(addressId).update(data)
    }

    fun hapusAlamat(userId: String, addressId: String) {
        db.collection("users").document(userId).collection("addresses").document(addressId).delete()
    }

    fun updateAlamatLahan(userId: String, alamatBaru: String, lat: Double, lng: Double, onSuccess: () -> Unit) {
        val dataUpdate = mapOf("alamatLahan" to alamatBaru, "lat" to lat, "lng" to lng)
        db.collection("users").document(userId).update(dataUpdate)
            .addOnSuccessListener { onSuccess() }
    }

    fun ambilProfilPenjual(idPetani: String) {
        if (idPetani.isBlank()) return
        db.collection("users").document(idPetani)
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    alamatPenjualTerbaru.value = document.getString("alamatLahan") ?: "Lokasi tidak tersedia"
                }
            }
    }
}