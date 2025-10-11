package com.markopetrovic.leaflog 

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.markopetrovic.leaflog.App.LeafLogApp
import com.markopetrovic.leaflog.views.theme.LeafLogTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            LeafLogTheme {
                LeafLogApp() 
            }
        }
    }
}
