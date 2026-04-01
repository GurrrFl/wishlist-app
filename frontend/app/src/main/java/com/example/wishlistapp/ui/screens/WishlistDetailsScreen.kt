package com.example.wishlistapp.ui.screens

import InfoCard
import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.ui.components.GiftCardNew
import com.example.wishlistapp.ui.components.NestedScreenHeader
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.androidx.compose.koinViewModel


@SuppressLint("LocalContextGetResourceValueCall")
@Composable
fun WishlistDetailsScreen(
    navController: NavHostController,
    wishlistId: Int,
    viewModel: WishlistViewModel = koinViewModel()
) {
    val wishlist = viewModel.getWishlist(wishlistId)!!

    Scaffold(
        topBar = {
            NestedScreenHeader(text = wishlist.title) { navController.popBackStack() }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                InfoCard(wishlist)
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, start = 2.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        stringResource(R.string.details_gifts_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                    Button(onClick = {
                        navController.navigate(Screen.AddGift.createRoute(wishlistId))
                    }) {
                        Text(stringResource(R.string.details_add_button))
                    }
                }
            }

            if (wishlist.gifts.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth().padding(top = 8.dp)
                            .clickable { navController.navigate(Screen.AddGift.createRoute(wishlistId)) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            stringResource(R.string.details_empty_gifts),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
            } else {
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
