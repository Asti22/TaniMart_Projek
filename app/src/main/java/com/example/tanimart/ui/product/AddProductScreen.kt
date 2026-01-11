package com.example.tanimart.ui.product

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.data.repository.ProductRepository
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.theme.GreenTani
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.views.overlay.MapEventsOverlay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProductScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    editingProduct: ProductModel? = null
) {
    val repository = remember { ProductRepository() }
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Data user (Petani) untuk sinkronisasi lokasi otomatis
    val userData by authViewModel.userData

    // --- STATE INPUT ---
    val nama = remember { mutableStateOf(editingProduct?.nama ?: "") }
    val harga = remember { mutableStateOf(editingProduct?.harga?.toString() ?: "") }
    val stok = remember { mutableStateOf(editingProduct?.stok?.toString() ?: "") }
    val deskripsi = remember { mutableStateOf(editingProduct?.deskripsi ?: "") }
    val kategori = remember { mutableStateOf(editingProduct?.kategori ?: "Sayur Daun") }
    val jangkauanKirim = remember { mutableStateOf(editingProduct?.jangkauanKirim ?: "Dekat") }
    val estimasiPengiriman = remember { mutableStateOf(editingProduct?.estimasiPengiriman ?: "") }

    // --- STATE LOKASI ---
    val lokasiLahan = remember { mutableStateOf(editingProduct?.lokasiLahan ?: "") }
    val lat = remember { mutableDoubleStateOf(editingProduct?.lat ?: -7.7956) }
    val lng = remember { mutableDoubleStateOf(editingProduct?.lng ?: 110.3695) }

    // Satuan otomatis berdasarkan kategori
    val satuan = remember(kategori.value) {
        when (kategori.value) {
            "Bibit" -> "Pcs"
            "Sayur Daun", "Sayur Akar", "Buah", "Rempah" -> "kg"
            else -> "Unit"
        }
    }

    // Sinkronisasi otomatis dari profil jika produk baru
    LaunchedEffect(userData) {
        if (editingProduct == null && userData != null) {
            userData?.let { user ->
                if (user.lat != 0.0 && user.lng != 0.0) {
                    lat.doubleValue = user.lat
                    lng.doubleValue = user.lng
                    if (lokasiLahan.value.isEmpty()) {
                        lokasiLahan.value = user.alamatLahan
                    }
                }
            }
        }
    }

    // State untuk daftar URL gambar (Cloudinary)
    val listUrl = remember { mutableStateListOf<String>().apply {
        editingProduct?.imageUrl?.let {
            if(it.isNotEmpty()) {
                if(it.contains(",")) addAll(it.split(",")) else add(it)
            }
        }
    } }

    var expandedKategori by remember { mutableStateOf(false) }

    // Update estimasi pengiriman secara otomatis berdasarkan kategori
    LaunchedEffect(kategori.value) {
        if (editingProduct == null) {
            estimasiPengiriman.value = when (kategori.value) {
                "Sayur Daun" -> "Maksimal 1 Hari (Instan)"
                "Sayur Buah" -> "1-2 Hari"
                "Rempah", "Sayur Akar" -> "2-3 Hari"
                "Bibit" -> "3-5 Hari"
                else -> "2-3 Hari"
            }
        }
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.isLoading.value = true
            repository.uploadKeCloudinary(it) { hasilUrl ->
                viewModel.isLoading.value = false
                if (hasilUrl != null) listUrl.add(hasilUrl)
            }
        }
    }

    // Navigasi balik jika berhasil simpan
    LaunchedEffect(viewModel.isSuccess.value) {
        if (viewModel.isSuccess.value) {
            viewModel.isSuccess.value = false
            onBack()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editingProduct == null) "Tambah Produk" else "Edit Produk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = GreenTani, titleContentColor = Color.White)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.padding(padding).fillMaxSize().padding(16.dp).verticalScroll(scrollState)
        ) {
            Text("Foto Produk (Maks 5)", fontWeight = FontWeight.Bold, color = GreenTani)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().height(120.dp)) {
                items(listUrl) { url ->
                    Box(modifier = Modifier.size(120.dp).clip(RoundedCornerShape(12.dp))) {
                        AsyncImage(model = url, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        IconButton(
                            onClick = { listUrl.remove(url) },
                            modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).background(Color.Black.copy(0.5f), CircleShape)
                        ) { Icon(Icons.Default.Close, null, tint = Color.White, modifier = Modifier.size(16.dp)) }
                    }
                }
                if (listUrl.size < 5) {
                    item {
                        OutlinedCard(onClick = { launcher.launch("image/*") }, modifier = Modifier.size(120.dp)) {
                            Column(Modifier.fillMaxSize(), Arrangement.Center, Alignment.CenterHorizontally) {
                                if (viewModel.isLoading.value) CircularProgressIndicator(Modifier.size(24.dp), color = GreenTani)
                                else { Icon(Icons.Default.AddAPhoto, null, tint = Color.Gray); Text("Tambah", fontSize = 12.sp, color = Color.Gray) }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = nama.value, onValueChange = { nama.value = it },
                label = { Text("Nama Sayur / Bibit") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = harga.value,
                    onValueChange = { if (it.all { char -> char.isDigit() }) harga.value = it },
                    label = { Text("Harga /$satuan") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    prefix = { Text("Rp ") }
                )
                OutlinedTextField(
                    value = stok.value,
                    onValueChange = { if (it.all { char -> char.isDigit() }) stok.value = it },
                    label = { Text("Stok ($satuan)") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Text("Kategori Produk", Modifier.padding(top = 16.dp), color = GreenTani, fontWeight = FontWeight.Bold)
            ExposedDropdownMenuBox(
                expanded = expandedKategori,
                onExpandedChange = { expandedKategori = !expandedKategori }
            ) {
                OutlinedTextField(
                    value = kategori.value, onValueChange = {}, readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedKategori) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(expanded = expandedKategori, onDismissRequest = { expandedKategori = false }) {
                    listOf("Sayur Daun", "Sayur Akar", "Buah", "Rempah", "Bibit").forEach { item ->
                        DropdownMenuItem(text = { Text(item) }, onClick = { kategori.value = item; expandedKategori = false })
                    }
                }
            }

            Text("Estimasi Lama Pengiriman", Modifier.padding(top = 16.dp), color = GreenTani, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = estimasiPengiriman.value,
                onValueChange = { estimasiPengiriman.value = it },
                label = { Text("Contoh: 1-2 Hari") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                leadingIcon = { Icon(Icons.Default.Schedule, null, tint = GreenTani) }
            )

            Text("Lokasi Lahan (Tap untuk ubah pin)", Modifier.padding(top = 16.dp), color = GreenTani, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Box(modifier = Modifier.fillMaxWidth().height(200.dp).clip(RoundedCornerShape(12.dp))) {
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)

                            val startPoint = GeoPoint(lat.doubleValue, lng.doubleValue)
                            controller.setCenter(startPoint)

                            val marker = Marker(this)
                            marker.position = startPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            overlays.add(marker)

                            val mapEventsReceiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint): Boolean {
                                    lat.doubleValue = p.latitude
                                    lng.doubleValue = p.longitude
                                    marker.position = p
                                    invalidate()
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint): Boolean = false
                            }
                            overlays.add(MapEventsOverlay(mapEventsReceiver))
                        }
                    },
                    update = { view ->
                        val target = GeoPoint(lat.doubleValue, lng.doubleValue)
                        view.controller.animateTo(target)
                        val marker = view.overlays.filterIsInstance<Marker>().firstOrNull()
                        marker?.position = target
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = lokasiLahan.value,
                onValueChange = { lokasiLahan.value = it },
                label = { Text("Detail Lokasi Lahan") },
                placeholder = { Text("Contoh: Kec. Lembang, Bandung") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Text("Jangkauan Pengiriman", color = GreenTani, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = jangkauanKirim.value == "Jauh", onClick = { jangkauanKirim.value = "Jauh" })
                Text("Bisa Luar Kota", fontSize = 14.sp)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = jangkauanKirim.value == "Dekat", onClick = { jangkauanKirim.value = "Dekat" })
                Text("Khusus Dalam Kota", fontSize = 14.sp)
            }

            OutlinedTextField(
                value = deskripsi.value, onValueChange = { deskripsi.value = it },
                label = { Text("Deskripsi Produk") },
                modifier = Modifier.fillMaxWidth().height(120.dp).padding(vertical = 8.dp), shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val finalUrl = listUrl.joinToString(",")
                    val sellerId = userData?.uid ?: ""
                    val sellerName = if (!userData?.namaToko.isNullOrBlank()) userData?.namaToko!! else userData?.nama ?: "Toko Petani"

                    if (editingProduct == null) {
                        viewModel.simpanProduk(
                            nama = nama.value,
                            harga = harga.value,
                            stok = stok.value,
                            url = finalUrl,
                            kategori = kategori.value,
                            lokasi = lokasiLahan.value,
                            deskripsi = deskripsi.value,
                            jangkauan = jangkauanKirim.value,
                            idPetani = sellerId,
                            namaToko = sellerName,
                            lat = lat.doubleValue,
                            lng = lng.doubleValue,
                            estimasiPengiriman = estimasiPengiriman.value,
                            satuan = satuan
                        )
                    } else {
                        viewModel.editProduk(
                            productId = editingProduct.id,
                            nama = nama.value,
                            harga = harga.value,
                            stok = stok.value,
                            url = finalUrl,
                            kategori = kategori.value,
                            lokasi = lokasiLahan.value,
                            deskripsi = deskripsi.value,
                            jangkauan = jangkauanKirim.value,
                            idPetani = sellerId,
                            namaToko = sellerName,
                            lat = lat.doubleValue,
                            lng = lng.doubleValue,
                            estimasiPengiriman = estimasiPengiriman.value,
                            satuan = satuan
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth().height(54.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                enabled = !viewModel.isLoading.value && listUrl.isNotEmpty() && nama.value.isNotBlank(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(if (viewModel.isLoading.value) "Menyimpan..." else "POSTING PRODUK SEKARANG", fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}