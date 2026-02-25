package com.example.wishlistapp.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedScreenHeader(text: String, onClick: () -> Unit) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxWidth(fraction = 0.9f)) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ArrowBackIosNew, null)
            }
        },
        colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(text: String) {
    TopAppBar(
        title = {
            Box(modifier = Modifier.fillMaxSize() ) {
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        },
        colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceDim),
    )
}