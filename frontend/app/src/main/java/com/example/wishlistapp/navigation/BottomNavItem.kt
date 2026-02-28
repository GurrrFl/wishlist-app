package com.example.wishlistapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    object Profile : BottomNavItem(
        route = Screens.PROFILE_SCREEN.route,
        title = "Профиль",
        icon = Icons.Default.Person
    )

    object Wishlists : BottomNavItem(
        route = Screen.Wishlists.route,
        title = "Вишлисты",
        icon = Icons.Default.List
    )

    object FindWishlist : BottomNavItem(
        route = Screen.Search.route,
        title = "Поиск",
        icon = Icons.Default.CardGiftcard
    )

    object ReserveGifts : BottomNavItem(
        route = Screens.RESERVE_GIFTS_SCREEN.route,
        title = "Резерв",
        icon = Icons.Default.Bookmark
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        title = "Настройки",
        icon = Icons.Default.Settings
    )
}
