package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.components.AppButton
import com.example.wishlistapp.ui.components.AppOutlinedTextField
import com.example.wishlistapp.ui.components.NestedScreenHeader
import com.example.wishlistapp.ui.components.obtainWishlist
import com.example.wishlistapp.viewmodel.WishlistViewModel
import kotlinx.coroutines.launch

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
    var isValid by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            NestedScreenHeader(
                text = stringResource(R.string.add_wishlist_title),
                onClick = { navController.popBackStack() }
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            AppOutlinedTextField(
                textLabel = R.string.wishlist_name_hint,
                value = wishlistName,
                onChanged = { wishlistName = it },
                modifier = Modifier.fillMaxWidth(),
            )

            AppOutlinedTextField(
                textLabel = R.string.wishlist_date_hint,
                textPlaceholder = R.string.wishlist_card_date_prefix,
                value = eventDate,
                onChanged = { eventDate = it },
                modifier = Modifier.fillMaxWidth()
            )

            AppOutlinedTextField(
                textLabel = R.string.wishlist_description_hint,
                textPlaceholder = R.string.wishlist_description_placeholder,
                value = description,
                onChanged = { description = it },
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
                modifier = Modifier.padding(start = 16.dp, end = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))
            AppButton(stringResource(R.string.done_button), modifier = Modifier.fillMaxWidth()
                .height(56.dp),
                onClick = {
                obtainWishlist(wishlistName, description, eventDate).fold(
                        onSuccess = { gift ->
                            val id = viewModel.addWishlist(title = wishlistName, description = description, isPrivate = isPrivate, eventDate = eventDate)
                            navController.popBackStack()
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