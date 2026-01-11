package com.example.tanimart.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tanimart.data.model.UserModel
import com.example.tanimart.ui.theme.GreenTani

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    // VARIABEL DIPISAH AGAR DATA TIDAK TERTUKAR/NULL
    var namaLengkap by remember { mutableStateOf("") }
    var namaToko by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var alamat by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Konsumen") }

    val isLoading by viewModel.isLoading
    val authState by viewModel.authState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Daftar TaniMart",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = GreenTani
        )

        Text(
            text = "Bergabunglah untuk hasil bumi terbaik",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Pilihan Role
        Text("Daftar sebagai:", style = MaterialTheme.typography.titleSmall, color = GreenTani)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            RadioButton(
                selected = selectedRole == "Konsumen",
                onClick = { selectedRole = "Konsumen" },
                colors = RadioButtonDefaults.colors(selectedColor = GreenTani)
            )
            Text("Konsumen")

            Spacer(modifier = Modifier.width(20.dp))

            RadioButton(
                selected = selectedRole == "Petani",
                onClick = { selectedRole = "Petani" },
                colors = RadioButtonDefaults.colors(selectedColor = GreenTani)
            )
            Text("Petani")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // INPUT 1: Nama Lengkap (Selalu ada untuk profil)
        OutlinedTextField(
            value = namaLengkap,
            onValueChange = { namaLengkap = it },
            label = { Text("Nama Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Contoh: Budi Santoso") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // INPUT 2: Nama Toko (Hanya muncul jika memilih Petani)
        if (selectedRole == "Petani") {
            OutlinedTextField(
                value = namaToko,
                onValueChange = { namaToko = it },
                label = { Text("Nama Toko / Kelompok Tani") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Contoh: Tani Makmur") },
                singleLine = true
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (Min. 6 Karakter)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = alamat,
            onValueChange = { alamat = it },
            label = { Text("Alamat Lengkap") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Kecamatan, Kota") }
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (isLoading) {
            CircularProgressIndicator(color = GreenTani)
        } else {
            Button(
                onClick = {
                    val user = UserModel(
                        nama = namaLengkap,
                        namaToko = if (selectedRole == "Petani") namaToko else "",
                        email = email,
                        role = selectedRole,
                        alamat = alamat
                    )
                    viewModel.register(user, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GreenTani),
                shape = RoundedCornerShape(12.dp),
                enabled = namaLengkap.isNotEmpty() &&
                        email.contains("@") &&
                        password.length >= 6 &&
                        (selectedRole != "Petani" || namaToko.isNotEmpty())
            ) {
                Text("DAFTAR SEKARANG", fontWeight = FontWeight.Bold)
            }
        }

        TextButton(onClick = onNavigateToLogin) {
            Text("Sudah punya akun? Login di sini", color = GreenTani)
        }

        authState?.let { result ->
            if (result.isSuccess) {
                LaunchedEffect(Unit) {
                    onNavigateToLogin()
                    viewModel.resetState()
                }
            } else {
                Text(
                    text = "Gagal: ${result.exceptionOrNull()?.message}",
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }
        }
    }
}