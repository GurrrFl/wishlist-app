package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.components.PulsingStarIcon

@Composable
fun AuthHeader(textAfterAppName: String) {
    PulsingStarIcon()
    Text(
        text = stringResource(R.string.app_name),
        style = MaterialTheme.typography.titleLarge,
        color =MaterialTheme.colorScheme.onBackground,
        modifier = Modifier.padding(top = 38.dp, bottom = 8.dp)
    )
    Text(
        text = textAfterAppName,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(bottom = 48.dp)
    )

}