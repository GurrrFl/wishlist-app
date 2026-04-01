package com.example.wishlistapp.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NestedScreenHeader(text: String,  onClick: () -> Unit) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
            )
        },
        navigationIcon = {
            IconButton(onClick = onClick) {
                Icon(Icons.Default.ArrowBackIosNew, null)
            }
        },
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppHeader(text: String) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = text,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground,
            )
        },
        windowInsets = WindowInsets(0.dp, 0.dp, 0.dp, 0.dp),
        colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
    )
}

@Composable
fun headerDivider(){
    HorizontalDivider(
        color = MaterialTheme.colorScheme.primary,
        thickness = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}