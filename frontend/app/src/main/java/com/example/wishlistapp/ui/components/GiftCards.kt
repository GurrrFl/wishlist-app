package com.example.wishlistapp.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.wishlistapp.R
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.data.model.GiftStatus

@Composable
fun GiftCardNew(
    gift: Gift,
    isSearched: Boolean = false,

    onClick: () -> Unit
) {
    val iconBackgroundColor = remember {
        generateRandomColor().copy(alpha = 0.35f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
               Image(
                    painter = painterResource(id = R.drawable.free_icon_gift),
                    contentDescription = gift.name,
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
)
            }

            Column(
                modifier = Modifier.weight(1f).padding(start = 16.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = gift.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = gift.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = gift.price + stringResource(R.string.price),
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.align(Alignment.CenterStart),
                        color = MaterialTheme.colorScheme.primary
                    )
                    if (isSearched) {
                        Surface(
                            modifier = Modifier.align(Alignment.TopEnd),
                            shape = RoundedCornerShape(18.dp),
                            color = if (gift.status == GiftStatus.AVAILABLE) MaterialTheme.colorScheme.tertiaryFixedDim.copy(alpha = 0.2f) else MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                        ) {

                            //TODO("Добавить логику, вызов проверки, доступен ли подарок")
                            ItemAccessTeg(gift.status)

                        }
                    }


                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = stringResource(R.string.arrow_right),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)

            )
        }
    }
}

@Composable
fun ItemAccessTeg(type: GiftStatus){

        Text(
            text = if (type == GiftStatus.AVAILABLE) stringResource(R.string.find_free_item) else stringResource(R.string.find_reserved_item),
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 2.dp),
            color = if (type == GiftStatus.AVAILABLE) MaterialTheme.colorScheme.tertiaryFixedDim else MaterialTheme.colorScheme.error
        )
}