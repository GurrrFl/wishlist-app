package com.example.wishlistapp.navigation

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.wishlistapp.data.SessionManager
import com.example.wishlistapp.ui.screens.AddGiftScreen
import com.example.wishlistapp.ui.screens.AddWishlistScreen
import com.example.wishlistapp.ui.screens.FindWishlistScreen
import com.example.wishlistapp.ui.screens.GiftDetailsScreen
import com.example.wishlistapp.ui.screens.ProfileScreen
import com.example.wishlistapp.ui.screens.ReserveGiftsScreen
import com.example.wishlistapp.ui.screens.SearchScreen
import com.example.wishlistapp.ui.screens.SettingsScreen
import com.example.wishlistapp.ui.screens.SplitScreen
import com.example.wishlistapp.ui.screens.WishlistDetailsScreen
import com.example.wishlistapp.ui.screens.WishlistsScreen
import com.example.wishlistapp.ui.screens.auth.LoginScreen
import com.example.wishlistapp.ui.screens.auth.RegisterScreen
import com.example.wishlistapp.viewmodel.AuthViewModel
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController,
    context: Context,
    darkThemeEnabled: Boolean,
    onThemeChange: (Boolean) -> Unit
) {
    var startDestination by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val sessionManager = SessionManager(context)
        startDestination = if (sessionManager.isLoggedIn()) {
            Screen.Wishlists.route
        } else {
            Screens.LOGIN_SCREEN.route
        }
    }

    if (startDestination == null) {
        return
    }

    NavHost(
        navController = navController,
        startDestination = startDestination!!
    ) {
        composable(route = Screen.Sample.route) {
            SplitScreen(
                navController = navController,
                context = context,
                darkThemeEnabled = darkThemeEnabled,
                onThemeChange = onThemeChange
            )
        }
        composable(route = Screens.LOGIN_SCREEN.route) {
            val     viewModel: AuthViewModel = koinViewModel()
            LoginScreen( navController = navController, viewModel)
        }
        composable(route = Screens.REGISTER_SCREEN.route) {
            RegisterScreen(navController = navController)
        }
        composable(route = Screens.PROFILE_SCREEN.route) {
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

        composable(route = Screens.RESERVE_GIFTS_SCREEN.route) {
            val  wishViewModel: WishlistViewModel  = koinViewModel()
            ReserveGiftsScreen(navController = navController, wishViewModel)
        }

        composable(route = Screen.Settings.route) {
            SettingsScreen(
                navController = navController)
        }

    }
}
