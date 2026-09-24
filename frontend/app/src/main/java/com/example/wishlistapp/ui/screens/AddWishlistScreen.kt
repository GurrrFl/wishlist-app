package com.example.wishlistapp.ui.screens

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.domain.result.AppResult
import com.example.wishlistapp.ui.components.AppButton
import com.example.wishlistapp.ui.components.AppOutlinedTextField
import com.example.wishlistapp.ui.components.NestedScreenHeader
import com.example.wishlistapp.ui.components.obtainWishlist
import com.example.wishlistapp.viewmodel.WishlistViewModel
import com.example.wishlistapp.viewmodel.toUserMessage
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishlistScreen(
    navController: NavHostController,
    viewModel: WishlistViewModel
) {

    var wishlistName by remember { mutableStateOf("") }
    var eventDate by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    val selectedDate = eventDate.toLocalDateOrNull(dateFormatter)

    Scaffold(
        topBar = {
            NestedScreenHeader(
                text = stringResource(R.string.add_wishlist_title),
                onClick = { navController.popBackStack() }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },

    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AppOutlinedTextField(
                textLabel = R.string.wishlist_name_hint,
                value = wishlistName,
                onChanged = { wishlistName = it.filterWishlistText() },
                modifier = Modifier.fillMaxWidth(),
            )

            DatePickerField(
                value = eventDate,
                onDateSelected = { eventDate = it },
                initialDate = selectedDate ?: LocalDate.now()
            )

            AppOutlinedTextField(
                textLabel = R.string.wishlist_description_hint,
                textPlaceholder = R.string.wishlist_description_placeholder,
                value = description,
                onChanged = { description = it.filterWishlistText(allowNewLines = true) },
                modifier = Modifier.fillMaxWidth(),
                isSingleLine = false
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it }
                )
                Text(
                    text = stringResource(R.string.wishlist_private_label),
                    modifier = Modifier.padding(8.dp)
                )
            }

            Text(
                text = stringResource(R.string.wishlist_private_description),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 28.dp)
            )
            AppButton(stringResource(R.string.done_button), modifier = Modifier.fillMaxWidth().height(56.dp),
                onClick = {
                obtainWishlist(context,wishlistName, description, eventDate).fold(
                        onSuccess = {
                            scope.launch {
                                when (val result = viewModel.createWishlistAndWait(wishlistName, isPrivate, eventDate)) {
                                    is AppResult.Success -> navController.popBackStack()
                                    is AppResult.Failure -> snackbarHostState.showSnackbar(result.error.toUserMessage())
                                }
                            }
                        },
                        onFailure = Button@{ error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error.message.toString())
                            }
                            return@Button
                        })
            })
        }
    }
}

@Composable
private fun DatePickerField(value: String, onDateSelected: (String) -> Unit, initialDate: LocalDate) {
    val context = LocalContext.current
    val formatter = remember { DateTimeFormatter.ofPattern("dd.MM.yyyy") }
    AppOutlinedTextField(
        textLabel = R.string.wishlist_date_hint,
        textPlaceholder = R.string.wishlist_card_date_prefix,
        value = value,
        onChanged = {},
        readOnly = true,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                DatePickerDialog(context, { _, year, month, day ->
                    onDateSelected(LocalDate.of(year, month + 1, day).format(formatter))
                }, initialDate.year, initialDate.monthValue - 1, initialDate.dayOfMonth).show()
            },
        leadingIcon = Icons.Default.CalendarToday
    )
}

private fun String.toLocalDateOrNull(formatter: DateTimeFormatter): LocalDate? =
    runCatching { LocalDate.parse(this, formatter) }.getOrNull()

private fun String.filterWishlistText(allowNewLines: Boolean = false): String =
    filter { it.isLetterOrDigit() || it == ' ' || allowNewLines && it == '\n' || it in "-.,!?():;\"'" }
