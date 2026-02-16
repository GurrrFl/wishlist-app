package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.navigation.Screens
import com.example.wishlistapp.ui.components.AppOutlinedTextField
import com.example.wishlistapp.ui.components.PasswordVisibilityToggle
import com.example.wishlistapp.ui.components.PulsingStarIcon
import com.example.wishlistapp.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel


@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state = viewModel.state

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            navController.navigate(Screen.Wishlists.route) {
                popUpTo(Screens.LOGIN_SCREEN.route) {
                    inclusive = true
                }
            }
            viewModel.resetState()
        }
    }
    if (state is AuthState.Loading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(26.dp, 35.dp, 26.dp, 16.dp))
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState())
            .alpha(if (state is AuthState.Loading) 0.5f else 1f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(25.dp))
        PulsingStarIcon()

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color =MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.log_in_to_your_acc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(48.dp))

        AppOutlinedTextField(
            textPlaceholder = R.string.email_label,
            onChanged = { email = it },
            leadingIcon = Icons.Default.Email,
            value = email,
            isSingleLine = true,
            isCost = false,
            isPasswordField = false,
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppOutlinedTextField(
            textPlaceholder = R.string.password_label,
            value = password,
            onChanged = { password = it },
            leadingIcon = Icons.Default.Lock,
            isSingleLine = true,
            isPasswordField = true,
            isPasswordVisible = isPasswordVisible,
            onVisibilityClick = {
                PasswordVisibilityToggle(isPasswordVisible, {isPasswordVisible = !isPasswordVisible})
            },
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (state is AuthState.LoginError) {
            Text(
                text = "${state.message} ",
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
        Box() {
            Button(
                onClick = {
                    viewModel.login(email, password)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = state !is AuthState.Loading
            ) {
                Text(stringResource(R.string.login_label),
                    color = Color.White)
            }
            if (state is AuthState.Loading) {
                LinearProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.no_account_label),
                color = MaterialTheme.colorScheme.onSurface
            )
            TextButton(
                onClick = {
                    navController.navigate(Screens.REGISTER_SCREEN.route)
                    viewModel.resetState()
                }
            ) {
                Text(text = stringResource(R.string.register_label))
            }
        }
    }
}
