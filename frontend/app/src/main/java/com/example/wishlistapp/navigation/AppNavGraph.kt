package com.example.wishlistapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.wishlistapp.ui.screens.AddGiftScreen
import com.example.wishlistapp.ui.screens.AddWishlistScreen
import com.example.wishlistapp.ui.screens.FindWishlistScreen
import com.example.wishlistapp.ui.screens.GiftDetailsScreen
import com.example.wishlistapp.ui.screens.ProfileScreen
import com.example.wishlistapp.ui.screens.ReserveGiftsScreen
import com.example.wishlistapp.ui.screens.SearchScreen
import com.example.wishlistapp.ui.screens.SettingsScreen
import com.example.wishlistapp.ui.screens.WishlistDetailsScreen
import com.example.wishlistapp.ui.screens.WishlistsScreen
import com.example.wishlistapp.ui.screens.auth.AuthViewModel
import com.example.wishlistapp.ui.screens.auth.LoginScreen
import com.example.wishlistapp.ui.screens.auth.RegisterScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screens.LOGIN_SCREEN.route
    ) {
        composable(route = Screens.LOGIN_SCREEN.route) {
            val     viewModel: AuthViewModel = koinViewModel()
            LoginScreen( navController = navController, viewModel)
        }
        composable(route = Screens.REGISTER_SCREEN.route) {
            RegisterScreen(navController = navController)
        }
        composable(route = Screens.PROFILE_SCREEN.route) {
            ProfileScreen(navController = navController)
        }
        composable(route = Screens.WISHLISTS_SCREEN.route) {
            WishlistsScreen(navController = navController)
        }
        composable(route = Screens.WISHLIST_DETAILS_SCREEN.route) {
            WishlistDetailsScreen(navController = navController)
        }
        composable(route = Screens.ADD_WISHLIST_SCREEN.route) {
            AddWishlistScreen(navController = navController)
        }

        composable(route = Screens.GIFT_DETAILS_SCREEN.route) {
            val isNotYours = false
            GiftDetailsScreen(navController = navController, isNotYours)
        }
        composable(route = Screens.ADD_GIFT_SCREEN.route) {
            AddGiftScreen(navController = navController)
        }
        composable(route = Screens.FIND_WISHLISTS_SCREEN.route) {
            FindWishlistScreen(navController = navController)
        }
        composable(route = Screens.SEARCH_SCREEN.route) {
            SearchScreen(navController = navController)
        }

        composable(route = Screens.RESERVE_GIFTS_SCREEN.route) {
            ReserveGiftsScreen(navController = navController)
        }

        composable(route = Screens.SETTINGS_SCREEN.route) {
            SettingsScreen(navController = navController)
        }
    }
}
