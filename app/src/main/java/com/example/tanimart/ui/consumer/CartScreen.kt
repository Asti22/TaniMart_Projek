package com.example.tanimart.ui.consumer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.tanimart.data.model.ProductModel
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit,
    onNavigateToAddress: () -> Unit
) {
    val cartItems = viewModel.cartList
    val context = LocalContext.current
    val firebaseUser by authViewModel.currentUser
    val userProfile by authViewModel.userData
    val userAddress = userProfile?.alamat ?: ""

    // --- STATE UNTUK PRODUK YANG DIPILIH ---
    val selectedItemIds = remember { mutableStateListOf<String>() }
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Logika menghitung total harga hanya untuk yang dicentang
    val totalSelectedPrice = remember(selectedItemIds.size, cartItems.size) {
        cartItems.filter { selectedItemIds.contains(it.id) }
            .sumOf { it.harga * it.stok }
    }

    LaunchedEffect(firebaseUser?.uid) {
        val uid = firebaseUser?.uid
        if (uid != null) {
            viewModel.ambilDataKeranjang(uid)
        } else {
            viewModel.cartList.clear()
            selectedItemIds.clear()
        }
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Konfirmasi Pesanan", fontWeight = FontWeight.Black, fontSize = 20.sp) },
            text = {
                Column {
                    val count = selectedItemIds.size
                    Text("Kamu akan memesan $count produk pilihan dengan sistem COD.", fontSize = 14.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color(0xFFF1F8E9),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, GreenTani.copy(0.2f))
                    ) {
                        Row(
                            Modifier.padding(16.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationOn, null, tint = GreenTani, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = if (userAddress.isNotBlank()) userAddress else "Alamat belum diatur",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFF2D3436),
                                maxLines = 3,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        firebaseUser?.let { user ->
                            // --- PERBAIKAN: Filter hanya produk yang dicentang (ID ada di selectedItemIds) ---
                            val itemsToCheckout = cartItems.filter { selectedItemIds.contains(it.id) }

                            viewModel.checkout(
                                userId = user.uid,
                                userEmail = user.email ?: "",
                                itemsToCheckout = itemsToCheckout, // Kirim list hasil filter
                                onSuccess = {
                                    showConfirmDialog = false
                                    selectedItemIds.clear() // Reset pilihan setelah berhasil
                                    Toast.makeText(context, "Pesanan Berhasil Dibuat!", Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                    shape = RoundedCornerShape(10.dp)
                ) { Text("Buat Pesanan", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Keranjang Saya", fontWeight = FontWeight.Black, fontSize = 20.sp) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.White),
                actions = {
                    if (cartItems.isNotEmpty()) {
                        TextButton(onClick = {
                            if (selectedItemIds.size == cartItems.size) {
                                selectedItemIds.clear()
                            } else {
                                selectedItemIds.clear()
                                selectedItemIds.addAll(cartItems.map { it.id })
                            }
                        }) {
                            Text(
                                text = if (selectedItemIds.size == cartItems.size) "Batal Semua" else "Pilih Semua",
                                color = GreenTani,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            )
        },
        bottomBar = {
            if (cartItems.isNotEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 25.dp,
                    color = Color.White
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 20.dp, vertical = 20.dp)
                            .navigationBarsPadding()
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Total (${selectedItemIds.size} Produk)", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                                Text("Rp $totalSelectedPrice", fontSize = 22.sp, fontWeight = FontWeight.Black, color = GreenTani)
                            }
                            Button(
                                onClick = {
                                    if (firebaseUser == null) {
                                        onNavigateToLogin()
                                    } else if (selectedItemIds.isEmpty()) {
                                        Toast.makeText(context, "Pilih produk dulu!", Toast.LENGTH_SHORT).show()
                                    } else if (userAddress.isBlank()) {
                                        Toast.makeText(context, "Atur alamat dulu ya!", Toast.LENGTH_SHORT).show()
                                        onNavigateToAddress()
                                    } else {
                                        showConfirmDialog = true
                                    }
                                },
                                enabled = selectedItemIds.isNotEmpty(),
                                colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(54.dp).width(160.dp)
                            ) {
                                Text("Checkout", fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
                            }
                        }
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFF8F9FA))
        ) {
            if (cartItems.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Outlined.ShoppingCart, null, modifier = Modifier.size(100.dp), tint = Color.LightGray.copy(0.5f))
                        Spacer(Modifier.height(16.dp))
                        Text("Keranjang kamu masih kosong", color = Color.Gray, fontSize = 16.sp)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        AddressSelectionCard(address = userAddress, onClick = onNavigateToAddress)
                    }

                    items(
                        items = cartItems,
                        key = { it.id }
                    ) { produk ->
                        CartItemWithSelection(
                            product = produk,
                            isSelected = selectedItemIds.contains(produk.id),
                            onCheckedChange = { isChecked ->
                                if (isChecked) selectedItemIds.add(produk.id)
                                else selectedItemIds.remove(produk.id)
                            },
                            onDelete = {
                                firebaseUser?.let { user ->
                                    viewModel.removeFromCart(user.uid, produk.id)
                                    selectedItemIds.remove(produk.id)
                                }
                            }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(120.dp)) }
                }
            }
        }
    }
}

@Composable
fun CartItemWithSelection(
    product: ProductModel,
    isSelected: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    val firstImageUrl = product.imageUrl.split(",").firstOrNull { it.isNotBlank() } ?: ""
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = GreenTani)
            )

            AsyncImage(
                model = firstImageUrl,
                contentDescription = null,
                modifier = Modifier.size(75.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = product.nama,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFF2D3436),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "${product.stok} ${product.getFormattedSatuan()}",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rp ${product.harga * product.stok}",
                    color = GreenTani,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp
                )
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.background(Color(0xFFFFEBEE), CircleShape).size(36.dp)
            ) {
                Icon(Icons.Default.DeleteOutline, null, tint = Color(0xFFD32F2F), modifier = Modifier.size(20.dp))
            }
        }
    }
}

@Composable
fun AddressSelectionCard(address: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.LocationOn, null, tint = GreenTani, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Alamat Pengiriman", fontSize = 12.sp, color = GreenTani, fontWeight = FontWeight.ExtraBold)
                Text(
                    text = if (address.isNotBlank()) address else "Atur alamat pengiriman sekarang",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (address.isNotBlank()) Color(0xFF2D3436) else Color(0xFFE74C3C),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
        }
    }
}