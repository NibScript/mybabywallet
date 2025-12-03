package com.example.mybabywallet.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
// IMPORTANTE: Aquí debe ir el nombre que encontraste en el Paso 1
import com.example.mybabywallet.ui.theme.MybabywalletTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Si en el paso 1 tu tema se llamaba diferente, cambia este nombre aquí también
            MybabywalletTheme() {
                WalletScreen()
            }
        }
    }
}