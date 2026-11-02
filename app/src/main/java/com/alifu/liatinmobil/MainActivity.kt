package com.alifu.liatinmobil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.alifu.liatinmobil.ui.navigation.AppNavHost
import com.alifu.liatinmobil.ui.theme.LiatinMobilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiatinMobilTheme {
                AppNavHost()
            }
        }
    }
}
