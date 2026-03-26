package com.example.wishlistapp.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.wishlistapp.R

sealed class BottomNavItem(
    val route: String,
    @StringRes val titleRes: Int,
    val icon: ImageVector
) {
    object Profile : BottomNavItem(
        route = Screen.Profile.route,
        titleRes = R.string.nav_profile,
        icon = Icons.Default.Person
    )

    object Wishlists : BottomNavItem(
        route = Screen.Wishlists.route,
        titleRes = R.string.nav_wishlists,
        icon = Icons.Default.List
    )

    object FindWishlist : BottomNavItem(
        route = Screen.Search.route,
        titleRes = R.string.nav_search,
        icon = Icons.Default.CardGiftcard
    )

    object ReserveGifts : BottomNavItem(
        route = Screen.ReserveGifts.route,
        titleRes = R.string.nav_reserve,
        icon = Icons.Default.Bookmark
    )

    object Settings : BottomNavItem(
        route = Screen.Settings.route,
        titleRes = R.string.nav_settings,
        icon = Icons.Default.Settings
    )

    companion object {
        val all: List<BottomNavItem> = listOf(
            Wishlists,
            FindWishlist,
            ReserveGifts,
            Profile,
        )
    }
}