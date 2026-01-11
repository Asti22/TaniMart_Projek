package com.example.tanimart.ui.consumer

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani
import com.google.android.gms.location.LocationServices
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import kotlin.math.*

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ConsumerHomeScreen(
    viewModel: ProductViewModel,
    onProductClick: (ProductModel) -> Unit,
    onAddToCart: (ProductModel) -> Unit,
    onNotificationClick: () -> Unit
) {
    // State Observables
    val productList by viewModel.productList
    val isLoading by viewModel.isLoading
    val context = LocalContext.current
    val uLat = viewModel.userLat
    val uLng = viewModel.userLng

    var selectedCategory by remember { mutableStateOf("Semua") }
    var searchQuery by remember { mutableStateOf("") }

    val locationPermissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    // 1. Initial Load & Permission Request
    LaunchedEffect(Unit) {
        viewModel.ambilSemuaProdukKonsumen()
        if (!locationPermissionState.allPermissionsGranted) {
            locationPermissionState.launchMultiplePermissionRequest()
        }
    }

    // 2. Fetch Location when permission granted
    LaunchedEffect(locationPermissionState.allPermissionsGranted) {
        if (locationPermissionState.allPermissionsGranted) {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        viewModel.updateLocation(it.latitude, it.longitude)
                        viewModel.ambilSemuaProdukKonsumen()
                    }
                }
            } catch (e: SecurityException) { e.printStackTrace() }
        }
    }

    // 3. Logic Filter & Sorting
    val filteredProducts = remember(productList, selectedCategory, searchQuery, uLat, uLng) {
        productList.filter { product ->
            val matchesCategory = if (selectedCategory == "Semua") true else product.kategori == selectedCategory
            val matchesSearch = product.nama.contains(searchQuery, ignoreCase = true)
            matchesCategory && matchesSearch
        }.sortedBy { product ->
            if (uLat != null && uLng != null) {
                calculateDistance(uLat, uLng, product.lat, product.lng)
            } else 0.0
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            HeaderBannerSection(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onNotificationClick = onNotificationClick,
                locationStatus = if (uLat != null) "Lokasi aktif • Terdekat" else "Aktifkan GPS"
            )

            Spacer(modifier = Modifier.height(60.dp))

            CategoryGridSection(
                selectedCategory = selectedCategory,
                onCategorySelect = { selectedCategory = it }
            )

            Spacer(modifier = Modifier.height(24.dp))

            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = if (searchQuery.isNotEmpty()) "Hasil Pencarian" else "Produk Segar Terdekat",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF2D3436)
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                        CircularProgressIndicator(color = GreenTani)
                    }
                } else if (filteredProducts.isEmpty()) {
                    Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) {
                        Text("Produk tidak ditemukan", color = Color.Gray)
                    }
                } else {
                    filteredProducts.chunked(2).forEach { rowProducts ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            rowProducts.forEach { product ->
                                Box(Modifier.weight(1f)) {
                                    ProductCard(
                                        product = product,
                                        userLat = uLat,
                                        userLng = uLng,
                                        onProductClick = { onProductClick(product) },
                                        onAddToCart = { onAddToCart(product) }
                                    )
                                }
                            }
                            if (rowProducts.size == 1) Spacer(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@Composable
fun HeaderBannerSection(
    query: String,
    onQueryChange: (String) -> Unit,
    onNotificationClick: () -> Unit,
    locationStatus: String
) {
    Box(modifier = Modifier.fillMaxWidth().height(240.dp)) {
        AsyncImage(
            model = "https://i.pinimg.com/1200x/c8/6d/d7/c86dd70e9f9deb4d725c627a03e74891.jpg",
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(0.6f), Color.Transparent)
                    )
                )
        )

        Column(modifier = Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("TaniMart", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Black)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, null, tint = Color.White, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(locationStatus, color = Color.White, fontSize = 12.sp)
                    }
                }
                IconButton(
                    onClick = onNotificationClick,
                    modifier = Modifier.background(Color.White.copy(0.2f), CircleShape)
                ) {
                    Icon(Icons.Default.Notifications, null, tint = Color.White)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.BottomCenter)
                .offset(y = 35.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth().padding(4.dp),
                placeholder = { Text("Cari sayur atau buah...", fontSize = 14.sp) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = GreenTani) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true
            )
        }
    }
}

@Composable
fun ProductCard(
    product: ProductModel,
    userLat: Double?,
    userLng: Double?,
    onProductClick: () -> Unit,
    onAddToCart: () -> Unit
) {
    val imgUrl = product.imageUrl.split(",").firstOrNull { it.isNotBlank() } ?: ""

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProductClick() }
            .shadow(8.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            Box {
                AsyncImage(
                    model = imgUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    contentScale = ContentScale.Crop
                )
                if (userLat != null && userLng != null) {
                    val d = calculateDistance(userLat, userLng, product.lat, product.lng)
                    Surface(
                        modifier = Modifier.padding(8.dp).align(Alignment.TopEnd),
                        color = Color.Black.copy(0.7f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            "${String.format("%.1f", d)} km",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Column(Modifier.padding(12.dp)) {
                // NAMA PRODUK: Dibuat lebih besar, tebal, dan jelas
                Text(
                    text = product.nama,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp,
                    color = Color(0xFF2D3436),
                    maxLines = 2,
                    lineHeight = 20.sp,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Nama Toko
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Store, null, tint = Color.Gray, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = product.namaToko,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Text(
                            text = "Rp ${product.harga}",
                            fontWeight = FontWeight.Black,
                            color = GreenTani,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "/${product.getFormattedSatuan()}",
                            fontSize = 11.sp,
                            color = Color.DarkGray,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Tombol Tambah
                    IconButton(
                        onClick = { onAddToCart() },
                        modifier = Modifier
                            .background(GreenTani, RoundedCornerShape(10.dp))
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryGridSection(selectedCategory: String, onCategorySelect: (String) -> Unit) {
    val cats = listOf(
        CategoryData("Semua", Icons.Default.Apps, Color(0xFFE8F5E9)),
        CategoryData("Sayur Daun", Icons.Default.Eco, Color(0xFFF1F8E9)),
        CategoryData("Sayur Akar", Icons.Default.Agriculture, Color(0xFFFFF3E0)),
        CategoryData("Buah", Icons.Default.Restaurant, Color(0xFFFCE4EC)),
        CategoryData("Rempah", Icons.Default.SoupKitchen, Color(0xFFFFFDE7)),
        CategoryData("Bibit", Icons.Default.Spa, Color(0xFFE0F2F1))
    )
    Column(Modifier.padding(horizontal = 16.dp)) {
        Text("Kategori", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Spacer(Modifier.height(12.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            cats.take(3).forEach { cat ->
                CategoryItem(cat, selectedCategory == cat.name, Modifier.weight(1f)) { onCategorySelect(cat.name) }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), Arrangement.spacedBy(8.dp)) {
            cats.takeLast(3).forEach { cat ->
                CategoryItem(cat, selectedCategory == cat.name, Modifier.weight(1f)) { onCategorySelect(cat.name) }
            }
        }
    }
}

@Composable
fun CategoryItem(cat: CategoryData, isSel: Boolean, modifier: Modifier, onClick: () -> Unit) {
    Card(
        modifier = modifier.height(80.dp).clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = if (isSel) GreenTani else cat.bgColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(cat.icon, null, tint = if (isSel) Color.White else GreenTani, modifier = Modifier.size(24.dp))
            Text(cat.name, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = if (isSel) Color.White else Color.DarkGray)
        }
    }
}

fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
    val r = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
    return r * 2 * atan2(sqrt(a), sqrt(1 - a))
}

data class CategoryData(val name: String, val icon: ImageVector, val bgColor: Color)