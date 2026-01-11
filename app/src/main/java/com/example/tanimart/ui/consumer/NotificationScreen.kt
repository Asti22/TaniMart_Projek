package com.example.tanimart.ui.consumer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani

// --- MODEL DATA (Diletakkan di sini agar tidak unresolved reference) ---
data class NotificationData(
    val id: String = "",
    val userId: String = "",
    val title: String = "",
    val message: String = "",
    val time: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "info"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val user = authViewModel.currentUser.value
    val notifications = viewModel.notificationsList.value

    LaunchedEffect(user?.uid) {
        user?.let { viewModel.monitorNotifications(it.uid) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Notifikasi", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { padding ->
        if (notifications.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.NotificationsNone, null, modifier = Modifier.size(80.dp), tint = Color.LightGray)
                    Text("Belum ada notifikasi", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(
                    items = notifications,
                    key = { it.id }
                ) { notificationItem ->
                    NotificationItem(
                        data = notificationItem,
                        onClick = { viewModel.markAsRead(notificationItem.id) }
                    )
                    HorizontalDivider(thickness = 0.5.dp, color = Color(0xFFEEEEEE))
                }
            }
        }
    }
}

@Composable
fun NotificationItem(data: NotificationData, onClick: () -> Unit) {
    val icon = when(data.type) {
        "order" -> Icons.Default.ShoppingBag
        "promo" -> Icons.Default.Notifications // Diganti agar tidak error LocalOffer
        else -> Icons.Default.Info
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (data.isRead) Color.White else GreenTani.copy(alpha = 0.05f))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(45.dp)
                .background(GreenTani.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = GreenTani, modifier = Modifier.size(22.dp))
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = data.title,
                fontWeight = if (data.isRead) FontWeight.Medium else FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = data.message,
                fontSize = 13.sp,
                color = if (data.isRead) Color.Gray else Color.Black
            )
            Text(
                text = data.time,
                fontSize = 11.sp,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        if (!data.isRead) {
            Box(
                modifier = Modifier.size(8.dp).background(Color.Red, CircleShape)
            )
        }
    }
}