package com.example.tanimart.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color // Import ini wajib
import androidx.compose.ui.platform.LocalContext

// --- TARUH DI SINI (DI LUAR FUNGSI) ---
val GreenTani = Color(0xFF2E7D32)
// --------------------------------------

private val DarkColorScheme = darkColorScheme(
    primary = GreenTani, // Kamu juga bisa mengganti primary theme di sini
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = GreenTani,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun TaniMartTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Saya sarankan false agar warna hijau kamu tetap konsisten
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}