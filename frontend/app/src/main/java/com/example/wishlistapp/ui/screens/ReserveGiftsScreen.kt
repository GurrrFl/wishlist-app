package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.data.model.GiftStatus
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.ui.components.AppHeader
import com.example.wishlistapp.ui.components.RoundedImage
import com.example.wishlistapp.ui.components.generateRandomColor
import com.example.wishlistapp.ui.components.headerDivider
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReserveGiftsScreen(
    navController: NavHostController,
    viewModel: WishlistViewModel = koinViewModel()
) {
    val gifts = viewModel
        .getAllGifts()
        .filter { it.status == GiftStatus.RESERVED }

    var giftToCancel by remember { mutableStateOf<Gift?>(null) }
    Scaffold(
        topBar = { AppHeader(stringResource(R.string.reserve_gifts_title)) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            headerDivider()
            if (gifts.isEmpty()) {
                EmptyReserveGiftsCard()
            } else {
                ReservedGiftsList(
                    gifts = gifts,
                    onGiftClick = { gift ->
                        navController.navigate(Screen.GiftDetails.createRoute(gift.id))
                    },
                    onCancelReservation = { gift ->
                        giftToCancel = gift
                    }
                )
            }
        }

        if (giftToCancel != null) {
            CancelReservationDialog(
                gift = giftToCancel!!,
                onConfirm = {
                    viewModel.cancelReservation(giftToCancel!!.id)
                    giftToCancel = null
                },
                onDismiss = { giftToCancel = null }
            )
        }
    }
}


@Composable
fun EmptyReserveGiftsCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RoundedImage(
                imageRes = R.drawable.free_icon_gift,
                modifier = Modifier.size(60.dp),
                contentDescription = stringResource(R.string.reserve_gifts_card_image_desc)
            )

            Text(
                text = stringResource(R.string.reserve_gifts_empty_message),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
fun ReservedGiftsList(
    gifts: List<Gift>,
    onGiftClick: (Gift) -> Unit,
    onCancelReservation: (Gift) -> Unit
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(gifts) { gift ->
            ReservedGiftCard(
                gift = gift,
                onCancelReservation = { onCancelReservation(gift) },
                onOpenDetails = { onGiftClick(gift) }
            )
        }
    }
}

@Composable
fun CancelReservationDialog(
    gift: Gift,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.reserve_gifts_cancel_dialog_title)) },
        text = {
            Text(
                stringResource(R.string.reserve_gifts_cancel_dialog_message, gift.name)
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(stringResource(R.string.reserve_gifts_cancel_confirm))
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(stringResource(R.string.reserve_gifts_cancel_dismiss))
            }
        }
    )
}

@Composable
fun ReservedGiftCard(
    gift: Gift,
    onCancelReservation: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val iconBackgroundColor = remember {
        generateRandomColor().copy(alpha = 0.65f)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                RoundedImage(
                    imageRes = R.drawable.free_icon_gift,
                    modifier = Modifier.size(60.dp),
                    imageModifier = Modifier.size(32.dp),
                    backgroundColor = iconBackgroundColor,
                    shape = RoundedCornerShape(12.dp),
                    contentDescription = gift.name
                )

                Column(Modifier.weight(1f)) {
                    Text(
                        text = gift.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = gift.price + stringResource(R.string.price),
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = stringResource(R.string.reserve_gifts_card_reserved, gift.created),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = onCancelReservation,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceDim
                    )
                ) {
                    Text(
                        stringResource(R.string.reserve_gifts_card_cancel_button),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Button(onClick = onOpenDetails) {
                    Text(
                        stringResource(R.string.reserve_gifts_card_details_button),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
