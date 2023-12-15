package com.example.exploringexoplayer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.exploringexoplayer.ui.audioequaliser.AudioEqualizerView
import com.example.exploringexoplayer.ui.theme.ExploringExoplayerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExploringExoplayerTheme {
                // A surface container using the 'background' color from the theme
                AudioEqualizerView()
            }
        }
    }
}