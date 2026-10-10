package com.infinity.linkleap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.infinity.linkleap.ui.screen.route.AppNavGraph
import com.infinity.linkleap.ui.theme.LinkLeapTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LinkLeapTheme {
                AppNavGraph()
            }
        }
    }
}