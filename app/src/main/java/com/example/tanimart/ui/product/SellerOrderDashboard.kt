package com.example.tanimart.ui.product

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.data.model.OrderModel
import com.example.tanimart.ui.theme.GreenTani

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SellerOrderDashboard(
    viewModel: ProductViewModel,
    onBack: () -> Unit
) {
    // Memanggil fungsi untuk ambil data pesanan secara real-time
    LaunchedEffect(Unit) {
        viewModel.ambilSemuaPesananMasuk()
    }

    val orders = viewModel.orderList.value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pesanan Masuk", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Kembali")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (orders.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("Belum ada pesanan masuk", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(Color(0xFFF7F7F7)),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(orders, key = { it.orderId }) { order ->
                    OrderCard(
                        order = order,
                        onUpdateStatus = { statusBaru ->
                            // PERBAIKAN: Mengirim 3 parameter (ID Pesanan, ID Pembeli, Status)
                            viewModel.updateStatusPesanan(order.orderId, order.consumerId, statusBaru)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderModel,
    onUpdateStatus: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ID Pesanan: #${order.orderId.take(6).uppercase()}",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = order.consumerEmail,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                // Badge Status
                val statusColor = when (order.status) {
                    "Pending" -> Color(0xFFFFC107) // Kuning
                    "Dikirim" -> Color(0xFF2196F3) // Biru
                    "Selesai" -> Color(0xFF4CAF50) // Hijau
                    else -> Color.Gray
                }

                Surface(
                    color = statusColor,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // PERBAIKAN: Menggunakan HorizontalDivider (Material 3)
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                thickness = 0.5.dp,
                color = Color.LightGray
            )

            // Daftar Barang
            Text(
                text = "Daftar Barang:",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            order.items.forEach { item ->
                Text(
                    text = "• ${item.nama} (${item.stok} kg)",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Total Pendapatan: Rp ${order.totalPrice}",
                fontWeight = FontWeight.ExtraBold,
                color = GreenTani,
                fontSize = 16.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tombol Aksi
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (order.status == "Pending") {
                    Button(
                        onClick = { onUpdateStatus("Dikirim") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.LocalShipping, null, modifier = Modifier.size(16.dp))
                        Text(" Kirim", fontSize = 13.sp)
                    }
                }

                if (order.status == "Dikirim") {
                    Button(
                        onClick = { onUpdateStatus("Selesai") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, null, modifier = Modifier.size(16.dp))
                        Text(" Selesaikan", fontSize = 13.sp)
                    }
                }
            }
        }
    }
}