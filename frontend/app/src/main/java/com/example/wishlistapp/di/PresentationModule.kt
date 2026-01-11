package com.example.wishlistapp.di

import com.example.wishlistapp.ui.screens.auth.AuthViewModel
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val presentationModule = module{
    viewModel {
        AuthViewModel(repository = get())
    }
    viewModel {
        WishlistViewModel()
    }
}