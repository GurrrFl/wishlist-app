package com.example.wishlistapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun AppOutlinedTextField(
    textLabel: Int,
    textPlaceholder: Int? = null,
    value: String,
    onChanged:(String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    leadingIcon: ImageVector? = null,
    isSingleLine: Boolean = true,
    isCost: Boolean = false,
    isPasswordField: Boolean = false,
    isPasswordVisible: Boolean = false,
    isError: Boolean = false,
    onVisibilityClick: @Composable (()-> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChanged,
        label = { Text(text = stringResource(textLabel)) },
        placeholder = textPlaceholder?.let{
            { Text(text = stringResource(textPlaceholder)) }
        },
        leadingIcon = leadingIcon?.let {
            { Icon(it, contentDescription = null) }
        },
        trailingIcon = onVisibilityClick,
        visualTransformation = if(isPasswordField && !isPasswordVisible){
            PasswordVisualTransformation()
        }else VisualTransformation.None,
        singleLine = isSingleLine,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier,
        suffix = if (isCost) {
            { Text(text = "₽") }
        } else null,
        isError = isError
    )

}
@Composable
fun PasswordVisibilityToggle(
    isPasswordVisible: Boolean,
    onToggle: () -> Unit
) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isPasswordVisible) {
                Icons.Default.Visibility
            } else {
                Icons.Default.VisibilityOff
            },
            contentDescription = null
        )
    }
}

@Preview
@Composable
fun AppOutlinedTextFieldPreview() {
    AppOutlinedTextField(textLabel = 0, value = "", onChanged = {})
}