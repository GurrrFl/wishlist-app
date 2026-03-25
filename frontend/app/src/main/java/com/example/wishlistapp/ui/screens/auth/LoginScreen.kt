package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state = viewModel.state
    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            navController.navigate(Screen.Wishlists.route) {
                popUpTo(Screen.Login.route) {
                    inclusive = true
                }
            }
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AuthHeader(stringResource(R.string.log_in_to_your_acc))
        LoginForm(state, onLogin = { email, password -> viewModel.login(email, password) })
        LoginFooter(
            stringResource(R.string.no_account_label),
            stringResource(R.string.register_label),
        ){ navController.navigate(Screen.Register.route) }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
