package com.example.wishlistapp.navigation

enum class Screens(val route: String){
    ADD_GIFT_SCREEN("add_gift_screen"),
    ADD_WISHLIST_SCREEN("add_wishlist_screen"),
    WISHLISTS_SCREEN("wishlist_screen"),
    WISHLIST_DETAILS_SCREEN("wishlist_details_screen"),
    FIND_WISHLISTS_SCREEN("find_screen"),
    SEARCH_SCREEN("search_screen"),
    GIFT_DETAILS_SCREEN("gift_details_screen"),
    RESERVE_GIFTS_SCREEN("reserve_screen"),
    PROFILE_SCREEN("profile_screen"),
    LOGIN_SCREEN("login_screen"),
    REGISTER_SCREEN("register_screen"),
    SETTINGS_SCREEN("settings_screen"),
}


sealed class Screen(val route: String) {

    object Login : Screen("login")
    object Register : Screen("register")

    object Profile : Screen("profile")
    object Wishlists : Screen("wishlists")
    object Settings : Screen("settings")

    object AddWishlist : Screen("add_wishlist/{wishlistId}") {
        fun createRoute(wishlistId: Int) =
            "add_wishlist/$wishlistId"
    }
    object AddGift : Screen("add_gift")

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
