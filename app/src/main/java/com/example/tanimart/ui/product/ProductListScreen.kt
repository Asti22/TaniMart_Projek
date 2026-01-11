package com.example.tanimart.ui.product

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.ui.theme.GreenTani
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductListScreen(
    viewModel: ProductViewModel,
    onAddClick: () -> Unit,
    onEditClick: (ProductModel) -> Unit,
    onProfileClick: () -> Unit,
    onNavigateToOrders: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var productToDelete by remember { mutableStateOf<ProductModel?>(null) }

    LaunchedEffect(Unit) {
        viewModel.ambilSemuaProduk()
        viewModel.monitorPesananMasuk()
    }

    LaunchedEffect(viewModel.adaPesananBaru) {
        if (viewModel.adaPesananBaru) {
            scope.launch {
                val result = snackbarHostState.showSnackbar(
                    message = "Ada pesanan baru masuk!",
                    actionLabel = "Lihat",
                    duration = SnackbarDuration.Long
                )
                if (result == SnackbarResult.ActionPerformed) {
                    viewModel.resetNotifikasi()
                    onNavigateToOrders()
                }
            }
        }
    }

    val filteredProducts = viewModel.productList.value.filter {
        it.nama.contains(searchQuery, ignoreCase = true)
    }

    if (showDeleteDialog && productToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Produk", fontWeight = FontWeight.ExtraBold) },
            text = { Text("Apakah Anda yakin ingin menghapus '${productToDelete?.nama}'?") },
            confirmButton = {
                Button(
                    onClick = {
                        productToDelete?.let { viewModel.hapusProduk(it.id) }
                        showDeleteDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))
                ) {
                    Text("Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Batal", color = Color.Gray)
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("TaniMart", fontSize = 14.sp, color = Color.White.copy(alpha = 0.8f))
                        Text("Dashboard Petani", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenTani,
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = {
                        viewModel.resetNotifikasi()
                        onNavigateToOrders()
                    }) {
                        BadgedBox(
                            badge = {
                                if (viewModel.adaPesananBaru) {
                                    Badge(containerColor = Color.Yellow) { Text("!") }
                                }
                            }
                        ) {
                            Icon(Icons.Default.Notifications, "Pesanan", tint = Color.White)
                        }
                    }
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, "Profil", tint = Color.White, modifier = Modifier.size(30.dp))
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddClick,
                containerColor = GreenTani,
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp),
                elevation = FloatingActionButtonDefaults.elevation(8.dp)
            ) {
                Icon(Icons.Default.Add, "Tambah")
                Spacer(Modifier.width(8.dp))
                Text("Tambah Produk")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // Header Dashboard Section dengan Gradient Background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(GreenTani, Color(0xFFF8F9FA)),
                            startY = 0f,
                            endY = 500f
                        )
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardCard(
                        label = "Jenis Produk",
                        value = "${viewModel.jumlahProduk}",
                        icon = Icons.Default.Category,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardCard(
                        label = "Total Stok",
                        value = "${viewModel.totalStok} kg",
                        icon = Icons.Default.Inventory2,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Search Bar Bergaya Elegan
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .shadow(4.dp, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = Color.White
            ) {
                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari produk Anda...", color = Color.Gray) },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = GreenTani) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                    ),
                    singleLine = true
                )
            }

            Text(
                text = "Daftar Produk Anda",
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, bottom = 8.dp),
                color = Color.DarkGray
            )

            if (viewModel.isLoading.value) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = GreenTani)
                }
            } else if (filteredProducts.isEmpty()) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Inventory, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                    Spacer(Modifier.height(8.dp))
                    Text("Belum ada produk.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = 80.dp), // Beri ruang untuk FAB
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(filteredProducts, key = { it.id }) { produk ->
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn() + slideInVertically()
                        ) {
                            ProductItem(
                                produk = produk,
                                onEdit = { onEditClick(produk) },
                                onDelete = {
                                    productToDelete = produk
                                    showDeleteDialog = true
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = GreenTani, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
        }
    }
}

@Composable
fun ProductItem(produk: ProductModel, onEdit: () -> Unit, onDelete: () -> Unit) {
    val firstImageUrl = remember(produk.imageUrl) {
        produk.imageUrl.split(",").firstOrNull { it.isNotEmpty() } ?: ""
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = firstImageUrl,
                contentDescription = null,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Surface(
                    color = GreenTani.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = produk.kategori.uppercase(),
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        fontSize = 10.sp,
                        color = GreenTani,
                        fontWeight = FontWeight.Bold
                    )
                }
                Text(
                    text = produk.nama,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = Color.Black,
                    maxLines = 1
                )
                Text(
                    text = "Rp ${produk.harga}",
                    color = GreenTani,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Layers, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = "Stok: ${produk.stok} kg",
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp).background(Color(0xFFE3F2FD), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, null, tint = Color(0xFF1976D2), modifier = Modifier.size(18.dp))
                }
                Spacer(Modifier.height(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp).background(Color(0xFFFFEBEE), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}