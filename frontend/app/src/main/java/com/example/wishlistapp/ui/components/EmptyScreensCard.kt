package com.example.wishlistapp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wishlistapp.R

@Composable
fun emptyScreensCard(idResource: Int, titleResource: Int, descriptionResource: Int) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = MaterialTheme.shapes.medium,

    ){
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,


        ){
            RoundedImage(imageRes = R.drawable.free_icon_gift,
                contentDescription = stringResource(titleResource),
                modifier = Modifier.size(100.dp));
            Text(
                modifier = Modifier.padding(8.dp),
                text = stringResource(titleResource),
                style = MaterialTheme.typography.titleMedium
            );
            Text(
                    modifier = Modifier.padding(8.dp),
                    text = stringResource(descriptionResource),
                    style = MaterialTheme.typography.bodyMedium
            );

        }
    }

}