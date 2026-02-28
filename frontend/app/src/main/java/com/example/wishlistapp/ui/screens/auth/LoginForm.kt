package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.components.AppErrorsText
import com.example.wishlistapp.ui.components.AppOutlinedTextField
import com.example.wishlistapp.ui.components.PasswordVisibilityToggle


@Composable
fun LoginForm(
    state: AuthState,
    onLogin: (String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }

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
        isPasswordVisible = isPasswordVisible
    ) {
        PasswordVisibilityToggle(isPasswordVisible, { isPasswordVisible = !isPasswordVisible })
    }


    if (state is AuthState.LoginError) {
        AppErrorsText(state.message)
    } else Spacer(modifier = Modifier.height(32.dp))

    AuthButton(onClick = {onLogin(email, password)},
        stringResource(R.string.login_label),
        isLoading = state is AuthState.Loading)

}