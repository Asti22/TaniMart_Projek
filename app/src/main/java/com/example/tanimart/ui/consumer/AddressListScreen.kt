package com.example.tanimart.ui.consumer

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tanimart.ui.auth.AuthViewModel
import com.example.tanimart.ui.product.ProductViewModel
import com.example.tanimart.ui.theme.GreenTani

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddressListScreen(
    viewModel: ProductViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val firebaseUser = authViewModel.currentUser.value
    val userProfile = authViewModel.userData.value

    var showDialog by remember { mutableStateOf(false) }
    var isEditMode by remember { mutableStateOf(false) }

    // State untuk input
    var label by remember { mutableStateOf("") }
    var detail by remember { mutableStateOf("") }
    var currentAddressId by remember { mutableStateOf("") } // Untuk tracking ID saat edit

    LaunchedEffect(firebaseUser) {
        firebaseUser?.let { viewModel.ambilAlamatUser(it.uid) }
    }

    // DIALOG (Tambah / Edit)
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    if (isEditMode) "Edit Alamat" else "Tambah Alamat Baru",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label (Contoh: Rumah / Kantor)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = detail,
                        onValueChange = { detail = it },
                        label = { Text("Alamat Lengkap") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (label.isNotBlank() && detail.isNotBlank()) {
                            firebaseUser?.let { user ->
                                if (isEditMode) {
                                    // Panggil fungsi Update di ViewModel
                                    viewModel.updateAlamat(user.uid, currentAddressId, label, detail)
                                    Toast.makeText(context, "Alamat diperbarui", Toast.LENGTH_SHORT).show()
                                } else {
                                    viewModel.tambahAlamat(user.uid, label, detail)
                                    Toast.makeText(context, "Alamat ditambahkan", Toast.LENGTH_SHORT).show()
                                }
                            }
                            showDialog = false
                        } else {
                            Toast.makeText(context, "Kolom tidak boleh kosong", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenTani)
                ) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text("Batal", color = Color.Gray) }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daftar Alamat", fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null) }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    isEditMode = false
                    label = ""; detail = ""; currentAddressId = ""
                    showDialog = true
                },
                containerColor = GreenTani,
                contentColor = Color.White,
                icon = { Icon(Icons.Default.Add, null) },
                text = { Text("Tambah Baru") }
            )
        }
    ) { padding ->
        val addresses = viewModel.addressList.value

        Column(modifier = Modifier.padding(padding).fillMaxSize().background(Color(0xFFF8F9FA))) {
            if (addresses.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada alamat tersimpan", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(addresses) { addr ->
                        val isMainAddress = userProfile?.alamat == addr["detail"]

                        AddressItem(
                            label = addr["label"] ?: "",
                            detail = addr["detail"] ?: "",
                            isSelected = isMainAddress,
                            onSelect = {
                                firebaseUser?.let { user ->
                                    authViewModel.updateAlamatUtama(user.uid, addr["detail"] ?: "") {
                                        Toast.makeText(context, "Alamat utama terpilih", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            onEdit = {
                                // Set data ke state lalu buka dialog
                                label = addr["label"] ?: ""
                                detail = addr["detail"] ?: ""
                                currentAddressId = addr["id"] ?: ""
                                isEditMode = true
                                showDialog = true
                            },
                            onDelete = {
                                firebaseUser?.let { viewModel.hapusAlamat(it.uid, addr["id"]!!) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AddressItem(
    label: String,
    detail: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) GreenTani else Color(0xFFEEEEEE),
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFF1F8E9) else Color.White
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) GreenTani else Color.LightGray
            )

            Spacer(Modifier.width(16.dp))

            Column(Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(detail, fontSize = 13.sp, color = Color.Gray, maxLines = 2)
            }

            // Tombol Aksi
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, "Edit", tint = GreenTani, modifier = Modifier.size(20.dp))
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, "Hapus", tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }
        }
    }
}