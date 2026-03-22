package com.example.wishlistapp.ui.screens

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.navigation.AppNavGraph
import com.example.wishlistapp.navigation.BottomNavigationBar
import com.example.wishlistapp.ui.theme.WishlistAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sessionManager = SessionManager(this)

        setContent {
            var darkThemeEnabled by rememberSaveable {
                mutableStateOf(sessionManager.isDarkTheme())
            }

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
                            context = this@MainActivity,
                            darkThemeEnabled = darkThemeEnabled,
                            onThemeChange = { 
                                darkThemeEnabled = it
                                sessionManager.saveDarkTheme(it)
                            }
                        )
                    }

                }
            }
        }
    }
}

