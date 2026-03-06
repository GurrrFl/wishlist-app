package com.example.wishlistapp.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.wishlistapp.R

@Composable
fun obtainWishlist(title : String,
                   description : String,
                   eventDate :String
): Result<String> {
    return if (title.isEmpty()) Result.failure(Exception(stringResource(R.string.wishlist_add_error_text)))
    else if (eventDate.isEmpty()) Result.failure(Exception(stringResource(R.string.wishlist_add_error_date)))
    else if (description.isEmpty()) Result.failure(Exception(stringResource(R.string.wishlist_add_error_description)))
    else Result.success(stringResource(R.string.wishlist_add_validation_success))
}
