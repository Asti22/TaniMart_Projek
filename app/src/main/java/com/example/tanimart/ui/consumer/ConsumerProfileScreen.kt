package com.example.tanimart.ui.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.theme.GreenTani

@Composable
fun ConsumerProfileScreen(
    authViewModel: AuthViewModel,
    onLogoutSuccess: () -> Unit,
    onNavigateToOrders: () -> Unit,
    onNavigateToAddress: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    // Data user diambil dari state authViewModel
    val user = authViewModel.userData.value
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
    ) {
        if (user == null) {
            EmptyProfileState(onNavigateToLogin)
        } else {
            // --- HEADER PROFIL ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GreenTani, Color(0xFF66BB6A))
                        ),
                        shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                    )
                    .padding(top = 60.dp, bottom = 40.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .shadow(8.dp, CircleShape),
                        shape = CircleShape,
                        color = Color.White
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(50.dp),
                                tint = GreenTani
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = user.nama.ifBlank { "Pembeli TaniMart" },
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = user.email,
                        color = Color.White.copy(0.85f),
                        fontSize = 14.sp
                    )
                }
            }

            Column(modifier = Modifier.padding(24.dp).fillMaxWidth()) {
                // --- BANNER ALAMAT ---
                Surface(
                    color = if (user.alamat.isBlank()) Color(0xFFFFF3E0) else Color.White,
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                        .clickable { onNavigateToAddress() }
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (user.alamat.isBlank()) Color(0xFFFFCCBC) else GreenTani.copy(alpha = 0.1f),
                            shape = CircleShape,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (user.alamat.isBlank()) Icons.Default.LocationOff else Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (user.alamat.isBlank()) Color(0xFFD84315) else GreenTani,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Alamat Pengiriman Utama",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Gray
                            )
                            Text(
                                text = user.alamat.ifBlank { "Belum ada alamat dipilih" },
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (user.alamat.isBlank()) Color.Red else Color.Black,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Text("Pengaturan Akun", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(
                    title = "Pesanan Saya",
                    subtitle = "Cek riwayat belanja dan status paket",
                    icon = Icons.Outlined.ShoppingBag,
                    onClick = onNavigateToOrders
                )

                ProfileMenuItem(
                    title = "Daftar Alamat",
                    subtitle = "Atur alamat rumah, kantor, atau lainnya",
                    icon = Icons.Outlined.Map,
                    onClick = onNavigateToAddress
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text("Bantuan & Informasi", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color.DarkGray)
                Spacer(modifier = Modifier.height(12.dp))

                ProfileMenuItem(
                    title = "Tentang TaniMart",
                    subtitle = "Versi aplikasi 1.0.0",
                    icon = Icons.Outlined.Info,
                    onClick = { /* Buka Info */ }
                )

                Spacer(modifier = Modifier.height(32.dp))

                // --- TOMBOL LOGOUT ---
                Button(
                    onClick = {
                        authViewModel.logout()
                        onLogoutSuccess()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Logout, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Keluar dari Akun", fontWeight = FontWeight.Bold, color = Color.Red)
                }

                Spacer(modifier = Modifier.height(80.dp)) // Padding bawah agar tidak tertutup bottom bar
            }
        }
    }
}

@Composable
fun ProfileMenuItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = GreenTani.copy(alpha = 0.08f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = GreenTani, modifier = Modifier.size(22.dp))
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF2D3436))
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}

@Composable
fun EmptyProfileState(onNavigateToLogin: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(120.dp),
                shadowElevation = 4.dp
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    null,
                    modifier = Modifier.padding(20.dp),
                    tint = Color.LightGray
                )
            }
            Spacer(Modifier.height(24.dp))
            Text(
                "Profil Tidak Tersedia",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                "Silakan login untuk mengatur profil dan melihat riwayat belanja kamu.",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize = 14.sp
            )
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onNavigateToLogin,
                colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text("Login Sekarang", fontWeight = FontWeight.Bold)
            }
        }
    }
}