package com.example.tanimart

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.cloudinary.android.MediaManager
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.auth.LoginScreen
import com.example.tanimart.ui.auth.ProfileScreen
import com.example.tanimart.ui.auth.RegisterScreen
import com.example.tanimart.ui.consumer.*
import com.example.tanimart.ui.home.LandingScreen
import com.example.tanimart.ui.product.*
import com.example.tanimart.ui.theme.TaniMartTheme
import org.osmdroid.config.Configuration


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // --- KONFIGURASI OPEN STREET MAP (OSM) ---
        Configuration.getInstance().load(
            this,
            androidx.preference.PreferenceManager.getDefaultSharedPreferences(this)
        )

        // --- INISIALISASI CLOUDINARY ---
        try {
            MediaManager.init(this, mapOf("cloud_name" to "ddxr0u2rm"))
        } catch (e: Exception) {
            // Ignored
        }

        setContent {
            TaniMartTheme {
                TaniAppNavigation()
            }
        }
    }
}

@Composable
fun TaniAppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val productViewModel: ProductViewModel = viewModel()

    // --- AUTO-FETCH DATA PROFIL & SESSION ---
    val currentUser by authViewModel.currentUser
    LaunchedEffect(currentUser) {
        currentUser?.uid?.let { uid ->
            authViewModel.getUserData(uid)
        }
    }

    NavHost(
        navController = navController,
        startDestination = "landing"
    ) {
        // --- 1. LANDING & AUTHENTICATION ---
        composable("landing") {
            LandingScreen(
                onStartClick = { navController.navigate("consumer_home") },
                onSellerLoginClick = { navController.navigate("login") }
            )
        }

        composable("login") {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = { role ->
                    // RESET data dulu sebelum login akun baru untuk keamanan
                    productViewModel.resetDataOnLogout()

                    if (role == "Petani") {
                        productViewModel.ambilSemuaProduk()
                        navController.navigate("product_list") {
                            popUpTo("landing") { inclusive = true }
                        }
                    } else {
                        productViewModel.ambilSemuaProdukKonsumen()
                        navController.navigate("consumer_home") {
                            popUpTo("landing") { inclusive = true }
                        }
                    }
                },
                onNavigateToRegister = { navController.navigate("register") }
            )
        }

        composable("register") {
            RegisterScreen(
                viewModel = authViewModel,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }

        // --- 2. KONSUMEN (PEMBELI) FLOW ---
        composable("consumer_home") {
            MainContainerScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                navController = navController,
                onNavigateToLogin = { navController.navigate("login") },
                onLogout = {
                    // --- PERBAIKAN DI SINI ---
                    productViewModel.resetDataOnLogout() // BERSIHKAN KERANJANG & LISTENER
                    authViewModel.logout()
                    navController.navigate("landing") { popUpTo("landing") { inclusive = true } }
                },
                onNavigateToDetail = { product ->
                    productViewModel.selectedProduct = product
                    navController.navigate("detail_produk")
                },
                onNotificationClick = { navController.navigate("notifications") },
                onNavigateToAddress = { navController.navigate("address_list") }
            )
        }

        composable("detail_produk") {
            val selectedProd = productViewModel.selectedProduct

            if (selectedProd != null) {
                ProductDetailScreen(
                    product = selectedProd,
                    viewModel = productViewModel,
                    authViewModel = authViewModel,
                    onBack = { navController.popBackStack() },
                    onNavigateToCart = {
                        productViewModel.currentSelectedTab = 1
                        navController.navigate("consumer_home") {
                            popUpTo("consumer_home") { saveState = true }
                            launchSingleTop = true
                        }
                    },
                    onNavigateToLogin = {
                        navController.navigate("login")
                    },
                    onNavigateToFarmerStore = { idPetani, namaToko ->
                        val encodedNama = Uri.encode(namaToko)
                        navController.navigate("farmer_store/$idPetani/$encodedNama")
                    }
                )
            }
        }

        composable(
            route = "farmer_store/{idPetani}/{namaToko}",
            arguments = listOf(
                navArgument("idPetani") { type = NavType.StringType },
                navArgument("namaToko") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val idPetani = backStackEntry.arguments?.getString("idPetani") ?: ""
            val namaToko = backStackEntry.arguments?.getString("namaToko") ?: "Toko Petani"
            FarmerStoreScreen(
                idPetani = idPetani,
                namaToko = namaToko,
                viewModel = productViewModel,
                onProductClick = { product ->
                    productViewModel.selectedProduct = product
                    navController.navigate("detail_produk")
                },
                onBack = { navController.popBackStack() }
            )
        }

        composable("notifications") {
            NotificationScreen(
                viewModel = productViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("address_list") {
            AddressListScreen(
                viewModel = productViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // --- 3. SELLER (PETANI) FLOW ---
        composable("product_list") {
            ProductListScreen(
                viewModel = productViewModel,
                onAddClick = {
                    productViewModel.selectedProduct = null
                    navController.navigate("add_product")
                },
                onEditClick = { produk ->
                    productViewModel.selectedProduct = produk
                    navController.navigate("add_product")
                },
                onProfileClick = { navController.navigate("profile") },
                onNavigateToOrders = { navController.navigate("seller_orders") }
            )
        }

        composable("add_product") {
            AddProductScreen(
                viewModel = productViewModel,
                authViewModel = authViewModel,
                onBack = { navController.popBackStack() },
                editingProduct = productViewModel.selectedProduct
            )
        }

        composable("seller_orders") {
            SellerOrderDashboard(
                viewModel = productViewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable("profile") {
            ProfileScreen(
                authViewModel = authViewModel,
                productViewModel = productViewModel,
                onLogoutSuccess = {
                    // --- PERBAIKAN DI SINI JUGA ---
                    productViewModel.resetDataOnLogout() // BERSIHKAN SEMUA DATA LAMA
                    authViewModel.logout()
                    navController.navigate("landing") { popUpTo("landing") { inclusive = true } }
                },
                onBack = { navController.popBackStack() },
                onNavigateToOrders = { navController.navigate("seller_orders") },
                onNavigateToMap = { navController.navigate("set_location") }
            )
        }

        // --- 4. MAPS / LOKASI FLOW ---
        composable("set_location") {
            SetLocationManualScreen(
                onBack = { navController.popBackStack() },
                onSave = { alamat, latitude, longitude ->
                    currentUser?.uid?.let { uid ->
                        productViewModel.updateAlamatLahan(uid, alamat, latitude, longitude) {
                            authViewModel.getUserData(uid)
                            navController.popBackStack()
                        }
                    }
                }
            )
        }
    }
}