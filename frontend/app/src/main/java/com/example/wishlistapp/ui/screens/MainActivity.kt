package com.example.wishlistapp.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.wishlistapp.navigation.AppNavGraph
import com.example.wishlistapp.navigation.BottomNavigationBar
import com.example.wishlistapp.ui.theme.WishlistAppTheme
import com.example.wishlistapp.viewmodel.SettingsViewModel
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val settings: SettingsViewModel by inject()
            val darkThemeEnabled = settings.darkThemeState.collectAsState().value
            WishlistAppTheme(darkTheme = darkThemeEnabled) {
                val navController = rememberNavController()

                Scaffold(
                    bottomBar = {
                        BottomNavigationBar(navController = navController)
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        AppNavGraph(
                            navController = navController,
                            darkThemeEnabled = darkThemeEnabled,
                            onThemeChange = { newTheme -> settings.toggleDarkTheme(newTheme) },
                            settings
                        )
                    }

                }
            }
        }
    }
}

