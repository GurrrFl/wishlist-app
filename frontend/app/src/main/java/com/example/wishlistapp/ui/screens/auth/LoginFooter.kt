package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginFooter(text: String, textButton: String, onButtonClick: () -> Unit) {
    Spacer(modifier = Modifier.height(32.dp))
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = text,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(
            onClick = onButtonClick
        ) {
            Text(text = textButton)
        }
    }
}