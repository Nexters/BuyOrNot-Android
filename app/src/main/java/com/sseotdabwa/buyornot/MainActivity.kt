package com.sseotdabwa.buyornot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.sseotdabwa.buyornot.core.designsystem.theme.BuyOrNotTheme
import com.sseotdabwa.buyornot.core.network.AuthEventBus
import com.sseotdabwa.buyornot.ui.BuyOrNotApp
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var authEventBus: AuthEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BuyOrNotTheme {
                BuyOrNotApp(
                    authEventBus = authEventBus,
                    onBackPressed = { finish() }
                )
            }
        }
    }
}
