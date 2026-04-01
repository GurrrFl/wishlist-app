package com.example.wishlistapp.ui.screens.auth

sealed class WishState {
    object Idle : WishState()
    object Loading : WishState()
    data class AddWishlistError(val message: String) : WishState()
    data class AddGiftError(val message: String) : WishState()
    object Success : WishState()
}