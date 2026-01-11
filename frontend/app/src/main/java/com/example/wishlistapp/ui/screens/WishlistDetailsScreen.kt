//
package com.example.wishlistapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.data.model.Gift
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.ui.components.GiftCardNew
import com.example.wishlistapp.viewmodel.WishlistViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistDetailsScreen(
    navController: NavHostController,
    wishlistId: Int,
    viewModel: WishlistViewModel
) {
    val context = LocalContext.current
    val wishlist = viewModel.getWishlist(wishlistId) ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {

        TopAppBar(
            title = { Text(wishlist.title,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 26.dp)
            ) },
            navigationIcon = {
                IconButton(onClick = { navController.navigateUp() }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = null)
                }
            },
            colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card (colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)){
            Column(modifier = Modifier.padding(16.dp)) {

                InfoRow("Владелец", wishlist.ownerName)
                InfoRow(
                    "Дата",
                    wishlist.eventDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
                )

                InfoRow(
                    "Тип",
                    if (wishlist.isPrivate) "Приватный" else "Публичный",
                    if (wishlist.isPrivate) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Описание", fontWeight = FontWeight.Bold)
                Text(
                    wishlist.description,
                    style = MaterialTheme.typography.bodySmall
                )

                if (!wishlist.isPrivate && wishlist.publicLink != null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Публичная ссылка", fontWeight = FontWeight.Bold)

                    OutlinedTextField(
                        value = wishlist.publicLink,
                        onValueChange = {},
                        shape = RoundedCornerShape(16.dp),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                Toast
                                    .makeText(context, "Ссылка скопирована", Toast.LENGTH_SHORT)
                                    .show()
                            }) {
                                Icon(Icons.Default.ContentCopy, null)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface),
                        colors = outlinedTextFieldColors(focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Подарки", style = MaterialTheme.typography.titleLarge)
            Button(onClick = {
                navController.navigate(Screen.AddGift.route)
            }) {
                Text("Добавить")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        if(wishlist.gifts.isEmpty()) {
            Text(
                "Пока подарков нет. Добавьте свое первое желание!",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
        else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(wishlist.gifts) { gift ->
                    GiftCardNew(gift) {
                        navController.navigate(
                            Screen.GiftDetails.createRoute(gift.id)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, color: Color = MaterialTheme.colorScheme.onBackground) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, color = color)
    }
}

@Composable
fun GiftCard(
    gift: Gift,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Column(modifier = Modifier.weight(1f)) {
                Text(gift.name, fontWeight = FontWeight.Bold)
                Text(
                    gift.description,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(gift.price, color = MaterialTheme.colorScheme.primary)
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null
            )
        }
    }
}
