package com.example.wishlistapp.ui.components

import android.content.Context
import com.example.wishlistapp.R

fun obtainWishlist(
    context: Context,
    title: String,
    description: String,
    eventDate: String
): Result<String> {
    return if (title.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.wishlist_add_error_text)))
    } else if (eventDate.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.wishlist_add_error_date)))
    } else if (description.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.wishlist_add_error_description)))
    } else {
        Result.success(context.getString(R.string.wishlist_add_validation_success))
    }
}

fun obtainGift(
    context: Context,
    name: String,
    price: String,
    link: String,
    description: String,
    wishlistId: Int?
): Result<String> {
    return if (name.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.gift_add_error_name)))
    } else if (price.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.gift_add_error_price)))
    } else if (link.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.gift_add_error_link)))
    } else if (wishlistId == null) {
        Result.failure(Exception(context.getString(R.string.gift_add_error_wishlist)))
    } else if (description.isEmpty()) {
        Result.failure(Exception(context.getString(R.string.gift_add_error_description)))
    } else {
        Result.success(context.getString(R.string.gift_add_validation_success))
    }
}