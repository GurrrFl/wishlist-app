package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.data.model.GiftStatus
import com.example.wishlistapp.ui.components.AppButton
import com.example.wishlistapp.ui.components.NestedScreenHeader
import com.example.wishlistapp.ui.components.RoundedImage
import com.example.wishlistapp.ui.components.generateRandomColor
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GiftDetailsScreen(
    navController: NavHostController,
    giftId: Int,
    viewModel: WishlistViewModel = koinViewModel()
) {
    val gift = viewModel.getGift(giftId) ?: return
    
    //при подключении репы будут сравниваться Idшники, а не имена
    val isOwner = viewModel.isGiftOwner(gift.ownerName)

    val iconBackgroundColor = remember {
        generateRandomColor().copy(alpha = 0.65f)
    }

    Scaffold(
        topBar = {
            NestedScreenHeader(
                stringResource(R.string.gift_details_title)
            ) { navController.navigateUp() }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            GiftImage(iconBackgroundColor = iconBackgroundColor)

            Text(
                text = gift.name,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Text(
                text = gift.price + stringResource(R.string.price),
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            GiftInfoRow(
                gift = gift,
                isOwner = isOwner
            )
            //вопрос как лучше делать, так, чтоб не загромождать вызовами экран
            // или 1 кард компоненту, а тут ее 3 раза вызывать? или сделать еще 1 функцию где делать 3 вызова?
            DescriptionCard(description = gift.description)
            gift.link?.let {
                LinkCard(link = it)
            }
            CreatedCard(created = gift.created)

            ActionButtons(
                isOwner = isOwner,
                gift = gift,
                viewModel = viewModel,
                onNavigateUp = { navController.navigateUp() }
            )
        }
    }
}

@Composable
fun GiftImage(
    iconBackgroundColor: Color
) {
    RoundedImage(
        imageRes = R.drawable.free_icon_gift,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f),
        imageModifier = Modifier.size(120.dp),
        backgroundColor = iconBackgroundColor,
        shape = RoundedCornerShape(16.dp),
        contentDescription = stringResource(R.string.gift_details_image_desc)
    )
}

@Composable
fun GiftInfoRow(
    gift: com.example.wishlistapp.data.model.Gift,
    isOwner: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                stringResource(R.string.gift_details_status_label),
                style = MaterialTheme.typography.bodySmall
            )
            val statusText = if (isOwner) {
                stringResource(R.string.gift_details_status_hidden)
            } else {
                if (gift.status == GiftStatus.AVAILABLE) {
                    stringResource(R.string.gift_details_status_available)
                } else {
                    stringResource(R.string.gift_details_status_reserved)
                }
            }
            val statusColor = if (isOwner) {
                Color.Gray
            } else {
                if (gift.status == GiftStatus.AVAILABLE) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    Color.Gray
                }
            }
            Text(text = statusText, color = statusColor)
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                stringResource(R.string.gift_details_owner_label),
                style = MaterialTheme.typography.bodySmall
            )
            Text(gift.ownerName)
        }

        Column(horizontalAlignment = Alignment.End) {
            Text(
                stringResource(R.string.gift_details_id_label),
                style = MaterialTheme.typography.bodySmall
            )
            Text("#${gift.id}")
        }
    }
}

@Composable
fun DescriptionCard(description: String) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(stringResource(R.string.gift_details_description_label))
            }
            Text(description)
        }
    }
}

@Composable
fun LinkCard(link: String) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.Link,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(stringResource(R.string.gift_details_link_label))
            }
            Text(
                text = link,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable { }
            )
        }
    }
}

@Composable
fun CreatedCard(created: LocalDate) {
    Card {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Text(stringResource(R.string.gift_details_created_label))
            }
            Text(created.toString())
        }
    }
}

@Composable
fun ActionButtons(
    isOwner: Boolean,
    gift: com.example.wishlistapp.data.model.Gift,
    viewModel: WishlistViewModel,
    onNavigateUp: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = { },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceDim
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        ) {
            Text(
                stringResource(R.string.gift_details_open_store),
                style = MaterialTheme.typography.bodyMedium
            )
        }

        val buttonText = when {
            isOwner -> stringResource(R.string.gift_details_delete)
            gift.status == GiftStatus.AVAILABLE -> stringResource(R.string.gift_details_reserve)
            else -> stringResource(R.string.gift_details_cancel_reservation)
        }

        val buttonColor = if (isOwner) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary

        AppButton(
            buttonText = buttonText,
            modifier = Modifier.weight(1f),
            buttonColor = buttonColor,
            buttonTextColor = if (isOwner) Color.White else MaterialTheme.colorScheme.onPrimary,
            onClick = {
                if (isOwner) {
                    viewModel.deleteGift(gift.id)
                    onNavigateUp()
                } else {
                    if (gift.status == GiftStatus.AVAILABLE) {
                        viewModel.reserveGift(gift.wishlistId, gift.id, "Вы")
                    } else {
                        viewModel.cancelReservation(gift.id)
                    }
                }
            }
        )
    }
}
