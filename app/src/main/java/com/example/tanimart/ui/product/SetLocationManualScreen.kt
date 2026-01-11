package com.example.tanimart.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.tanimart.ui.theme.GreenTani
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetLocationManualScreen(
    onBack: () -> Unit,
    onSave: (String, Double, Double) -> Unit
) {
    var alamatLengkap by remember { mutableStateOf("") }
    var catatanTambahan by remember { mutableStateOf("") }

    // State untuk menampung lokasi yang dipilih
    val pointState = remember { mutableStateOf(GeoPoint(-7.7956, 110.3695)) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Atur Lokasi Lahan", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            // Header Info
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(GreenTani.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, tint = GreenTani)
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Tentukan Lokasi Peta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(text = "Klik pada peta untuk memindahkan titik merah", fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- BAGIAN PETA (OSM) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .background(Color.LightGray, RoundedCornerShape(16.dp))
            ) {
                AndroidView(
                    factory = { context ->
                        MapView(context).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(15.0)
                            controller.setCenter(pointState.value)

                            // 1. Tambahkan Marker awal
                            val marker = Marker(this)
                            marker.position = pointState.value
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.setInfoWindow(null) // Menghilangkan popup saat marker diklik
                            overlays.add(marker)

                            // 2. Overlay untuk menangkap klik di peta
                            val mapEventsReceiver = object : MapEventsReceiver {
                                override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                                    p?.let {
                                        // Update posisi marker di peta secara visual
                                        marker.position = it
                                        // Simpan koordinat terbaru ke state
                                        pointState.value = it
                                        // Refresh tampilan peta
                                        invalidate()
                                    }
                                    return true
                                }
                                override fun longPressHelper(p: GeoPoint?): Boolean = false
                            }

                            val mapEventsOverlay = MapEventsOverlay(mapEventsReceiver)
                            overlays.add(0, mapEventsOverlay) // Urutan 0 agar berada di lapisan bawah
                        }
                    },
                    modifier = Modifier.fillMaxSize(),
                    update = { view ->
                        // Kosongkan agar tidak terjadi re-center otomatis yang bikin macet
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input Alamat Lengkap
            OutlinedTextField(
                value = alamatLengkap,
                onValueChange = { alamatLengkap = it },
                label = { Text("Alamat (Desa, Kec, Kab)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Input Catatan (Patokan)
            OutlinedTextField(
                value = catatanTambahan,
                onValueChange = { catatanTambahan = it },
                label = { Text("Patokan / Detail Alamat") },
                placeholder = { Text("Contoh: Dekat Gapura Hijau") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.weight(1f))

            // Tombol Simpan
            Button(
                onClick = {
                    val dataGabungan = if (catatanTambahan.isNotEmpty())
                        "$alamatLengkap ($catatanTambahan)" else alamatLengkap

                    onSave(dataGabungan, pointState.value.latitude, pointState.value.longitude)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                shape = RoundedCornerShape(12.dp),
                enabled = alamatLengkap.isNotEmpty()
            ) {
                Text("Simpan Lokasi Lahan", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}