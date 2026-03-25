package com.example.wishlistapp.navigation


sealed class Screen(val route: String) {
    object Sample : Screen("sample")
    object Login : Screen("login")
    object Register : Screen("register")

    object Profile : Screen("profile")
    object Wishlists : Screen("wishlists")
    object Settings : Screen("settings")

    object AddWishlist : Screen("add_wishlist/{wishlistId}") {
        fun createRoute(wishlistId: Int) = "add_wishlist/$wishlistId"
    }
    object AddGift : Screen("add_gift/{wishlistId}") {
        fun createRoute(wishlistId: Int) = "add_gift/$wishlistId"
    }

    object WishlistDetails : Screen("wishlist/{wishlistId}") {
        fun createRoute(wishlistId: Int) = "wishlist/$wishlistId"
    }

    object FindWishlist : Screen("find/{wishlistId}") {
        fun createRoute(wishlistId: Int) = "find/$wishlistId"
    }

    object GiftDetails : Screen("gift/{giftId}") {
        fun createRoute(giftId: Int) = "gift/$giftId"
    }

    object ReserveGifts : Screen("reserve")
    object Search : Screen("search")
}
