package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.wishlistapp.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state = viewModel.state

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            navController.popBackStack()
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
        AuthHeader(stringResource(R.string.register_label))
        RegisterForm(state,
            onRegister = {login, email, password, repeatPassword -> viewModel.register(login, email, password, repeatPassword)})
        LoginFooter(
            stringResource(R.string.has_account_label),
            stringResource(R.string.has_login_acc_label),
        ){ navController.popBackStack(); viewModel.resetState() }

    }
}