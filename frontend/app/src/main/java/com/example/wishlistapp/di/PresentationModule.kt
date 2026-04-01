package com.example.wishlistapp.di

import com.example.wishlistapp.viewmodel.AuthViewModel
import com.example.wishlistapp.viewmodel.SettingsViewModel
import com.example.wishlistapp.viewmodel.WishlistViewModel
import org.koin.core.module.dsl.viewModel


import org.koin.dsl.module

val presentationModule = module{
    viewModel {
        AuthViewModel( get())
    }
    viewModel {
        WishlistViewModel(get())
    }
    viewModel {
        SettingsViewModel(get())
    }

}