package com.example.tanimart.ui.consumer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.data.model.OrderModel
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val orders by viewModel.orderList // Pastikan menggunakan 'by' jika di ViewModel berupa State
    val userId = authViewModel.currentUser.value?.uid ?: ""
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Berlangsung", "Selesai")

    // Sinkronisasi data saat layar dibuka atau saat tab berpindah
    LaunchedEffect(userId, selectedTabIndex) {
        if (userId.isNotBlank()) {
            viewModel.ambilPesananSaya(userId)
        }
    }

    // Filter pesanan secara real-time berdasarkan status
    val filteredOrders = remember(orders, selectedTabIndex) {
        if (selectedTabIndex == 0) {
            orders.filter { it.status != "Selesai" && it.status != "Dibatalkan" }
        } else {
            orders.filter { it.status == "Selesai" }
        }
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color.White)) {
                CenterAlignedTopAppBar(
                    title = { Text("Pesanan Saya", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, null, tint = Color.Black)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White)
                )
                TabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = Color.White,
                    contentColor = GreenTani,
                    indicator = { tabPositions ->
                        if (selectedTabIndex < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = GreenTani
                            )
                        }
                    }
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 14.sp
                                )
                            }
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            if (filteredOrders.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = Color.LightGray.copy(alpha = 0.5f)
                        )
                        Spacer(Modifier.height(16.dp))
                        Text("Belum ada transaksi di bagian ini", color = Color.Gray, fontSize = 14.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filteredOrders, key = { it.orderId }) { order ->
                        OrderCard(
                            order = order,
                            onConfirmReceived = {
                                viewModel.updateStatusPesanan(order.orderId, userId, "Selesai")
                                Toast.makeText(context, "Terima kasih! Pesanan selesai.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OrderCard(
    order: OrderModel,
    onConfirmReceived: () -> Unit
) {
    val statusColor = when (order.status) {
        "Pending" -> Color(0xFFFFA000)
        "Dikonfirmasi" -> Color(0xFF673AB7)
        "Dikirim" -> Color(0xFF1976D2)
        "Selesai" -> GreenTani
        else -> Color.Gray
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ShoppingBag, null, tint = GreenTani, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Order #${order.orderId.take(7).uppercase()}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                Surface(
                    color = statusColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = order.status,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        color = statusColor,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFF1F1F1))
            Spacer(Modifier.height(12.dp))

            // Menampilkan daftar item dalam satu pesanan
            order.items.forEach { item ->
                Row(
                    modifier = Modifier.padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(36.dp).background(Color(0xFFF5F5F5), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Eco, null, tint = GreenTani, modifier = Modifier.size(18.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.nama, fontSize = 14.sp, fontWeight = FontWeight.Bold, maxLines = 1)
                        Text("${item.stok} unit x Rp ${item.harga}", fontSize = 12.sp, color = Color.Gray)
                    }
                    Text(
                        "Rp ${item.harga * item.stok}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF9F9F9), RoundedCornerShape(12.dp))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Total Tagihan", fontSize = 11.sp, color = Color.Gray)
                    Text(
                        "Rp ${order.totalPrice}",
                        fontWeight = FontWeight.Black,
                        color = GreenTani, // Menggunakan GreenTani agar lebih konsisten
                        fontSize = 16.sp
                    )
                }

                if (order.status == "Dikirim") {
                    Button(
                        onClick = onConfirmReceived,
                        colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text("Terima Barang", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                } else if (order.status == "Selesai") {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Selesai", color = GreenTani, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenTani, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}