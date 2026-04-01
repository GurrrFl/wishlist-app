package com.example.wishlistapp.ui.screens.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
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
fun RegisterForm(state: AuthState,
                 onRegister:(String,String, String, String)->Unit
){
    var login by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    Column(modifier = Modifier.padding(0.dp,8.dp,0.dp,8.dp,)) {

        AppOutlinedTextField(
            textLabel = R.string.nickname_label,
            value = login,
            onChanged = { login = it },
            leadingIcon = Icons.Default.Person
        )
        AppOutlinedTextField(
            textLabel = R.string.email_label,
            value = email,
            onChanged = { email = it },
            leadingIcon = Icons.Default.Email
        )
        AppOutlinedTextField(
            textLabel = R.string.password_label,
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
        AppOutlinedTextField(
            textLabel = R.string.repeat_password_label,
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

    }
    if (state is AuthState.RegisterError) {
        AppErrorsText(state.message)
    } else Spacer(modifier = Modifier.height(32.dp))

    AuthButton(
        onClick = { onRegister(login, email, password, repeatPassword) },
        text = stringResource(R.string.register),
        isLoading = state is AuthState.Loading,
    )
}