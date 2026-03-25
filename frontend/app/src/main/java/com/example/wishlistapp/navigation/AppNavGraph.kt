package com.example.wishlistapp.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wishlistapp.ui.screens.AccountScreen
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
import com.example.wishlistapp.ui.screens.auth.LoginScreen
import com.example.wishlistapp.ui.screens.auth.RegisterScreen
import com.example.wishlistapp.viewmodel.AuthViewModel
import com.example.wishlistapp.viewmodel.SettingsViewModel
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    settingsViewModel: SettingsViewModel
) {
    val startDestination by settingsViewModel.startDestination.collectAsState()

    if (startDestination == null) {
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(route = Screen.Sample.route) {
            AccountScreen(
                navController = navController,
                settingsViewModel = settingsViewModel
            )
        }
        composable(route = Screen.Login.route) {
            val     viewModel: AuthViewModel = koinViewModel()
            LoginScreen( navController = navController, viewModel)
        }
        composable(route = Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(route = Screen.Profile.route) {
            val viewModel: AuthViewModel = koinViewModel()
            ProfileScreen(navController = navController, viewModel )
        }
        composable(route = Screen.Wishlists.route) {
            val  wishViewModel: WishlistViewModel  = koinViewModel()
            WishlistsScreen(navController = navController, wishViewModel)
        }

        composable(
            route = Screen.WishlistDetails.route,
            arguments = listOf(
                navArgument("wishlistId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val wishlistId = backStackEntry.arguments?.getInt("wishlistId") ?: return@composable
            val  wishViewModel: WishlistViewModel  = koinViewModel()
            WishlistDetailsScreen(navController = navController, wishlistId,wishViewModel)
        }
        composable(route = Screen.AddWishlist.route) {
            val  wishViewModel: WishlistViewModel  = koinViewModel()
            AddWishlistScreen(navController = navController, wishViewModel)
        }

        composable(
            route = Screen.GiftDetails.route,
            arguments = listOf(
                navArgument("giftId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val giftId =
                backStackEntry.arguments?.getInt("giftId") ?: return@composable

            GiftDetailsScreen(
                navController = navController,
                giftId = giftId,
            )
        }
        composable(
            route = Screen.AddGift.route,
            arguments = listOf(
                navArgument("wishlistId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val wishlistId =
                backStackEntry.arguments?.getInt("wishlistId") ?: return@composable
            AddGiftScreen(navController = navController, wishlistId = wishlistId)
        }
        composable(
            route = Screen.FindWishlist.route,
            arguments = listOf(
                navArgument("wishlistId") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val wishlistId =
                backStackEntry.arguments?.getInt("wishlistId") ?: return@composable

            FindWishlistScreen(
                navController = navController,
                wishlistId = wishlistId
            )
        }
        composable(route = Screen.Search.route) {
            SearchScreen(navController = navController)
        }

        composable(route = Screen.ReserveGifts.route) {
            val  wishViewModel: WishlistViewModel  = koinViewModel()
            ReserveGiftsScreen(navController = navController, wishViewModel)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController = navController)
        }

    }
}
