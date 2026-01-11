package com.example.tanimart.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani

@Composable
fun ProfileScreen(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    onLogoutSuccess: () -> Unit,
    onBack: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToMap: () -> Unit // Parameter navigasi baru
) {
    // Ambil data user dari State
    val user = authViewModel.userData.value

    val totalProduk = productViewModel.jumlahProduk
    val totalStok = productViewModel.totalStok

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // --- HEADER HIJAU ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(GreenTani)
                .padding(top = 48.dp, bottom = 64.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = GreenTani,
                        modifier = Modifier.size(50.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = user?.nama ?: "Petani TaniMart",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp
                )
                Text(
                    text = user?.email ?: "email@petani.com",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }

            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.TopStart).padding(16.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = null, tint = Color.White)
            }
        }

        // --- RINGKASAN STATISTIK ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .offset(y = (-30).dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Total Produk",
                value = "$totalProduk",
                icon = Icons.Default.Inventory
            )
            StatCard(
                modifier = Modifier.weight(1f),
                label = "Total Stok",
                value = "$totalStok kg",
                icon = Icons.Default.Agriculture
            )
        }

        Text(
            text = "Manajemen Toko",
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            Column {
                ProfileMenuItem(
                    icon = Icons.Default.ListAlt,
                    label = "Daftar Pesanan Masuk",
                    onClick = onNavigateToOrders
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ProfileMenuItem(
                    icon = Icons.Default.LocationOn,
                    label = "Atur Lokasi Lahan",
                    // Menampilkan alamat jika sudah ada di database
                    subLabel = if (user?.alamatLahan.isNullOrEmpty()) "Belum diatur" else user?.alamatLahan,
                    onClick = onNavigateToMap
                )
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)

                ProfileMenuItem(
                    icon = Icons.Default.Storefront,
                    label = "Status Toko (Buka/Tutup)",
                    onClick = { /* Bisa ditambah logika status toko nanti */ }
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                authViewModel.logout()
                onLogoutSuccess()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Logout, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Keluar dari Akun", color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun StatCard(modifier: Modifier, label: String, value: String, icon: ImageVector) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = GreenTani, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = GreenTani)
            Text(text = label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    label: String,
    subLabel: String? = null, // Tambahan untuk teks kecil di bawah label
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(GreenTani.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = GreenTani, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = label, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subLabel != null) {
                Text(
                    text = subLabel,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }

        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.LightGray)
    }
}