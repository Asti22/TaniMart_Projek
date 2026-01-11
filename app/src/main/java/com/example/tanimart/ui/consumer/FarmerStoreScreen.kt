package com.example.tanimart.ui.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FarmerStoreScreen(
    idPetani: String,
    namaToko: String,
    viewModel: ProductViewModel,
    onProductClick: (ProductModel) -> Unit,
    onBack: () -> Unit
) {
    val productList by viewModel.productList
    val uLat = viewModel.userLat
    val uLng = viewModel.userLng

    val farmerProducts = remember(productList, idPetani) {
        productList.filter { it.idPetani == idPetani }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil Toko", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenTani,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF8F9FA))
        ) {
            // --- HEADER SECTION (Gaya Modern) ---
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
                    .padding(20.dp)
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar Toko Bulat
                            Surface(
                                modifier = Modifier.size(65.dp),
                                shape = CircleShape,
                                color = GreenTani.copy(alpha = 0.1f)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.Storefront,
                                        null,
                                        tint = GreenTani,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = namaToko,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        null,
                                        tint = Color(0xFF1DA1F2),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = "Petani Lokal Terverifikasi",
                                    fontSize = 12.sp,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Color(0xFFF1F1F1))
                        Spacer(modifier = Modifier.height(15.dp))

                        // Row Statistik Singkat
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StoreStatItem("Produk", "${farmerProducts.size}")
                            StoreStatItem("Rating", "4.8")
                            StoreStatItem("Bergabung", "2024")
                        }
                    }
                }
            }

            // --- DAFTAR PRODUK ---
            Text(
                text = "Katalog Produk",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )

            if (farmerProducts.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Inventory, null, modifier = Modifier.size(48.dp), tint = Color.LightGray)
                        Text("Belum ada produk.", color = Color.Gray)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(farmerProducts) { product ->
                        ProductCard(
                            product = product,
                            userLat = uLat,
                            userLng = uLng,
                            onProductClick = { onProductClick(product) },
                            onAddToCart = {
                                viewModel.addToCart(product, 1)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StoreStatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = GreenTani)
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
    }
}