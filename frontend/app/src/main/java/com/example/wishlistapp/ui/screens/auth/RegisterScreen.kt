package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.components.AppOutlinedTextField
import com.example.wishlistapp.ui.components.PasswordVisibilityToggle
import com.example.wishlistapp.ui.components.PulsingStarIcon
import com.example.wishlistapp.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun RegisterScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = koinViewModel()
) {
    val state = viewModel.state

    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

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
        PulsingStarIcon()
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(R.string.register_label),
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(28.dp))

        AppOutlinedTextField(
            textPlaceholder = R.string.nickname_label,
            value = login,
            onChanged = { login = it },
            leadingIcon = Icons.Default.Person
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppOutlinedTextField(
            textPlaceholder = R.string.email_label,
            value = email,
            onChanged = { email = it },
            leadingIcon = Icons.Default.Email
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppOutlinedTextField(
            textPlaceholder = R.string.password_label,
            value = password,
            onChanged = { password = it },
            leadingIcon = Icons.Default.Lock,
            isPasswordField = true,
            isPasswordVisible = isPasswordVisible,
            onVisibilityClick = {
                PasswordVisibilityToggle(
                    isPasswordVisible = isPasswordVisible,
                    onToggle = { isPasswordVisible = !isPasswordVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppOutlinedTextField(
            textPlaceholder = R.string.repeat_password_label,
            value = repeatPassword,
            onChanged = { repeatPassword = it },
            leadingIcon = Icons.Default.Lock,
            isPasswordField = true,
            isPasswordVisible = isPasswordVisible,
            onVisibilityClick = {
                PasswordVisibilityToggle(
                    isPasswordVisible = isPasswordVisible,
                    onToggle = { isPasswordVisible = !isPasswordVisible }
                )
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (state is AuthState.RegisterError) {
            Text(
                text = state.message,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.register(login, email, password, repeatPassword)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = state !is AuthState.Loading
        ) {
            if (state is AuthState.Loading) {
                LinearProgressIndicator(modifier = Modifier)
            } else {
                Text(stringResource(R.string.register))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}