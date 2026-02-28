package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.data.model.GiftStatus
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.ui.components.generateRandomColor
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Забронированные подарки",
            style = MaterialTheme.typography.headlineMedium
        )

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(gifts) { gift ->
                ReservedGiftCard(
                    gift = gift,
                    onCancelReservation = { giftToCancel = gift },
                    onOpenDetails = {
                        navController.navigate(
                            Screen.GiftDetails.createRoute(gift.id)
                        )
                    }
                )
            }
        }
    }

    giftToCancel?.let { gift ->
        AlertDialog(
            onDismissRequest = { giftToCancel = null },
            title = { Text("Отменить бронь?") },
            text = {
                Text("Бронь подарка «${gift.name}» будет отменена.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.cancelReservation(gift.id)
                        giftToCancel = null
                    }
                ) {
                    Text("Отменить бронь")
                }
            },
            dismissButton = {
                Button(
                    onClick = { giftToCancel = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text("Нет")
                }
            }
        )
    }
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
        Column(modifier = Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(iconBackgroundColor),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(R.drawable.free_icon_gift),
                        contentDescription = gift.name,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = gift.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = gift.price,
                        style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 6.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = "Забронировано ${gift.created}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

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
                    Text("Удалить бронь", style = MaterialTheme.typography.bodySmall)
                }

                Button(onClick = onOpenDetails) {
                    Text("Открыть детали", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
