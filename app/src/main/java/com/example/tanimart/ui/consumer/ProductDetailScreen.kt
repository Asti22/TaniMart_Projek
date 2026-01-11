package com.example.tanimart.ui.consumer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
fun ProductDetailScreen(
    product: ProductModel?,
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    onNavigateToCart: () -> Unit,
    onNavigateToLogin: () -> Unit,
    onNavigateToFarmerStore: (idPetani: String, namaToko: String) -> Unit
) {
    if (product == null) return

    val context = LocalContext.current
    val firebaseUser by authViewModel.currentUser // Cek status login

    var quantity by remember { mutableIntStateOf(1) }
    var isErrorStok by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()
    val labelSatuan = product.getFormattedSatuan()

    val alamatRealtime by viewModel.alamatPenjualTerbaru

    LaunchedEffect(product.idPetani) {
        viewModel.ambilProfilPenjual(product.idPetani)
    }

    val images = remember(product.imageUrl) {
        if (product.imageUrl.contains(",")) {
            product.imageUrl.split(",").filter { it.isNotEmpty() }
        } else {
            listOf(product.imageUrl)
        }
    }

    val pagerState = rememberPagerState(pageCount = { images.size })

    Scaffold(
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().shadow(20.dp),
                color = Color.White
            ) {
                Column {
                    if (isErrorStok) {
                        Text(
                            text = "Maksimal pembelian ${product.stok} $labelSatuan",
                            color = Color.Red,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.fillMaxWidth().background(Color(0xFFFFEBEE)).padding(vertical = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }

                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                            .navigationBarsPadding(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF5F5F5))
                                .padding(horizontal = 4.dp)
                        ) {
                            IconButton(onClick = {
                                if (quantity > 1) {
                                    quantity--
                                    isErrorStok = false
                                }
                            }) {
                                Icon(Icons.Default.Remove, null, modifier = Modifier.size(18.dp), tint = GreenTani)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.width(65.dp)
                            ) {
                                BasicTextField(
                                    value = quantity.toString(),
                                    onValueChange = { newValue ->
                                        val filtered = newValue.filter { it.isDigit() }
                                        if (filtered.isEmpty()) {
                                            quantity = 1
                                            isErrorStok = false
                                        } else {
                                            val inputNum = filtered.toInt()
                                            if (inputNum > product.stok) {
                                                quantity = product.stok
                                                isErrorStok = true
                                            } else {
                                                quantity = inputNum
                                                isErrorStok = false
                                            }
                                        }
                                    },
                                    textStyle = TextStyle(
                                        fontWeight = FontWeight.Black,
                                        fontSize = 18.sp,
                                        color = if (isErrorStok) Color.Red else Color(0xFF2D3436),
                                        textAlign = TextAlign.Center
                                    ),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true
                                )
                                Text(labelSatuan, fontSize = 10.sp, color = Color.Gray)
                            }

                            IconButton(onClick = {
                                if (quantity < product.stok) {
                                    quantity++
                                    isErrorStok = false
                                } else {
                                    isErrorStok = true
                                }
                            }) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp), tint = GreenTani)
                            }
                        }

                        // TOMBOL TAMBAH KERANJANG (DENGAN CEK LOGIN)
                        OutlinedButton(
                            onClick = {
                                if (firebaseUser == null) {
                                    Toast.makeText(context, "Login dulu untuk belanja ya!", Toast.LENGTH_SHORT).show()
                                    onNavigateToLogin()
                                } else {
                                    viewModel.addToCart(product, quantity)
                                    Toast.makeText(context, "Berhasil masuk keranjang", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, GreenTani)
                        ) {
                            Icon(Icons.Default.AddShoppingCart, null, tint = GreenTani)
                        }

                        // TOMBOL BELI SEKARANG (DENGAN CEK LOGIN)
                        Button(
                            onClick = {
                                if (firebaseUser == null) {
                                    Toast.makeText(context, "Login dulu untuk belanja ya!", Toast.LENGTH_SHORT).show()
                                    onNavigateToLogin()
                                } else {
                                    viewModel.addToCart(product, quantity)
                                    onNavigateToCart()
                                }
                            },
                            modifier = Modifier.weight(1f).height(50.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GreenTani)
                        ) {
                            Text("Beli Sekarang", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize().background(Color.White)) {
            Column(modifier = Modifier.verticalScroll(scrollState).fillMaxSize()) {
                Box(modifier = Modifier.fillMaxWidth().height(420.dp)) {
                    HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                        AsyncImage(
                            model = images[page],
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp).background(Brush.verticalGradient(listOf(Color.Black.copy(0.4f), Color.Transparent))))

                    if (images.size > 1) {
                        Surface(
                            modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp),
                            color = Color.Black.copy(0.5f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "${pagerState.currentPage + 1} / ${images.size}",
                                color = Color.White,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.padding(20.dp)) {
                    Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text("Rp", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = GreenTani, modifier = Modifier.padding(bottom = 4.dp))
                            Spacer(Modifier.width(2.dp))
                            Text(product.harga.toString(), fontSize = 32.sp, fontWeight = FontWeight.Black, color = GreenTani)
                            Text(" / $labelSatuan", fontSize = 16.sp, color = Color.Gray, modifier = Modifier.padding(bottom = 6.dp, start = 4.dp))
                        }
                        Surface(color = GreenTani.copy(0.12f), shape = RoundedCornerShape(8.dp)) {
                            Text(
                                text = product.kategori,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                color = GreenTani,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = product.nama,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2D3436),
                        lineHeight = 32.sp,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = Color(0xFFF8F9FA),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Icon(Icons.Default.FmdGood, null, tint = GreenTani, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = alamatRealtime,
                                    fontSize = 13.sp,
                                    color = Color.DarkGray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Inventory2, null, tint = Color(0xFFE67E22), modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Stok: ${product.stok}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    Text("Deskripsi Produk", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D3436))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = product.deskripsi,
                        fontSize = 15.sp,
                        color = Color(0xFF636E72),
                        lineHeight = 24.sp,
                        textAlign = TextAlign.Justify
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    Text("Informasi Penjual", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2D3436))
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable {
                            if (product.idPetani.isNotEmpty()) onNavigateToFarmerStore(product.idPetani, product.namaToko)
                        },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F1F1))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(56.dp).clip(CircleShape).background(GreenTani.copy(0.1f)), contentAlignment = Alignment.Center) {
                                Icon(Icons.Outlined.Storefront, null, tint = GreenTani, modifier = Modifier.size(30.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = product.namaToko.ifBlank { "Toko Tani" }, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp, color = Color(0xFF2D3436))
                                Text(
                                    text = alamatRealtime,
                                    fontSize = 12.sp,
                                    color = Color.Gray,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                        }
                    }
                    Spacer(modifier = Modifier.height(120.dp))
                }
            }

            IconButton(
                onClick = onBack,
                modifier = Modifier.statusBarsPadding().padding(16.dp).size(44.dp).background(Color.White, CircleShape).shadow(2.dp, CircleShape)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = Color.Black, modifier = Modifier.size(24.dp))
            }
        }
    }
}