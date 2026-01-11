package com.example.tanimart.ui.consumer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainContainerScreen(
    authViewModel: AuthViewModel,
    productViewModel: ProductViewModel,
    navController: NavController,
    onNavigateToLogin: () -> Unit,
    onLogout: () -> Unit,
    onNavigateToDetail: (com.example.tanimart.data.model.ProductModel) -> Unit,
    onNotificationClick: () -> Unit,
    onNavigateToAddress: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val currentUser = authViewModel.currentUser.value

    // --- SINKRONISASI DATA OTOMATIS ---
    // Efek ini akan berjalan setiap kali currentUser berubah (misal setelah login)
    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { uid ->
            // Mengambil data keranjang dari cloud
            productViewModel.ambilDataKeranjang(uid)
            // Mengambil daftar alamat tersimpan
            productViewModel.ambilAlamatUser(uid)
            // Memulai listener notifikasi real-time
            productViewModel.monitorNotifications(uid)
        }
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = GreenTani,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                val items = listOf("Home", "Keranjang", "Pesanan", "Profil")
                val icons = listOf(
                    Icons.Default.Home,
                    Icons.Default.ShoppingCart,
                    Icons.Default.Receipt,
                    Icons.Default.Person
                )

                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = productViewModel.currentSelectedTab == index,
                        onClick = { productViewModel.currentSelectedTab = index },
                        label = { Text(item) },
                        icon = {
                            if (index == 1) { // Badge jumlah barang di keranjang
                                BadgedBox(
                                    badge = {
                                        if (productViewModel.cartList.isNotEmpty()) {
                                            Badge(containerColor = Color.Red) {
                                                Text(productViewModel.cartList.size.toString())
                                            }
                                        }
                                    }
                                ) {
                                    Icon(icons[index], contentDescription = item)
                                }
                            } else {
                                Icon(icons[index], contentDescription = item)
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = GreenTani,
                            selectedTextColor = GreenTani,
                            indicatorColor = GreenTani.copy(alpha = 0.1f),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {
            when (productViewModel.currentSelectedTab) {
                // --- TAB 0: BERANDA ---
                0 -> ConsumerHomeScreen(
                    viewModel = productViewModel,
                    onProductClick = onNavigateToDetail,
                    onNotificationClick = onNotificationClick,
                    onAddToCart = { product ->
                        if (currentUser != null) {
                            productViewModel.addToCart(product, 1)
                            scope.launch {
                                snackbarHostState.currentSnackbarData?.dismiss()
                                snackbarHostState.showSnackbar(
                                    message = "${product.nama} berhasil ditambah ke keranjang!",
                                    duration = SnackbarDuration.Short
                                )
                            }
                        } else {
                            onNavigateToLogin()
                        }
                    }
                )

                // --- TAB 1: KERANJANG BELANJA ---
                1 -> CartScreen(
                    viewModel = productViewModel,
                    authViewModel = authViewModel,
                    onNavigateToLogin = onNavigateToLogin,
                    onNavigateToAddress = onNavigateToAddress
                )

                // --- TAB 2: DAFTAR PESANAN ---
                2 -> MyOrdersScreen(
                    viewModel = productViewModel,
                    authViewModel = authViewModel,
                    onBack = { productViewModel.currentSelectedTab = 0 }
                )

                // --- TAB 3: PROFIL PENGGUNA ---
                3 -> ConsumerProfileScreen(
                    authViewModel = authViewModel,
                    onLogoutSuccess = onLogout,
                    onNavigateToOrders = { productViewModel.currentSelectedTab = 2 },
                    onNavigateToAddress = onNavigateToAddress,
                    onNavigateToLogin = onNavigateToLogin
                )
            }
        }
    }
}