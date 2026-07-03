package com.asensiodev.carbura

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.asensiodev.carbura.core.designsystem.CarburaTheme
import com.asensiodev.carbura.feature.garage.presentation.GarageRoute

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CarburaTheme {
                CarburaApp()
            }
        }
    }
}

@Composable
private fun CarburaApp() {
    GarageRoute()
}

@Preview
@Composable
private fun CarburaAppPreview() {
    CarburaTheme {
        CarburaApp()
    }
}
