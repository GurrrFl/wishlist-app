package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.ui.components.AppButton
import com.example.wishlistapp.ui.components.NestedScreenHeader
import com.example.wishlistapp.ui.components.obtainGift
import com.example.wishlistapp.viewmodel.WishlistViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGiftScreen(
    navController: NavHostController,
    wishlistId: Int,
    viewModel: WishlistViewModel = koinViewModel()
) {
    var giftName by remember { mutableStateOf("") }
    var giftPrice by remember { mutableStateOf("") }
    var giftLink by remember { mutableStateOf("") }
    var giftDescription by remember { mutableStateOf("") }

    val wishlists = viewModel.getWishlists()
    var selectedWishlistId by remember { mutableStateOf<Int?>(if (wishlistId != -1) wishlistId else null) }
    var expanded by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            NestedScreenHeader(
                stringResource(R.string.add_gift_title)
            ) { navController.navigateUp() }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AppButton(
                buttonText = stringResource(R.string.done_button),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(84.dp)
                    .padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                onClick = {
                    obtainGift(
                        context = context,
                        name = giftName,
                        price = giftPrice,
                        link = giftLink,
                        description = giftDescription,
                        wishlistId = selectedWishlistId
                    ).fold(
                        onSuccess = {
                            viewModel.addGift(
                                name = giftName,
                                price = giftPrice,
                                link = giftLink,
                                description = giftDescription,
                                wishlistId = selectedWishlistId!!
                            )
                            navController.popBackStack()
                        },
                        onFailure = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error.message.toString())
                            }
                        }
                    )
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            AppTextField(
                label = stringResource(R.string.gift_name_label),
                value = giftName,
                onValueChange = { giftName = it },
                placeholder = stringResource(R.string.gift_name_placeholder),
                singleLine = true
            )

            AppTextField(
                label = stringResource(R.string.gift_price_label),
                value = giftPrice,
                onValueChange = { giftPrice = it },
                placeholder = stringResource(R.string.gift_price_placeholder),
                singleLine = true
            )

            AppTextField(
                label = stringResource(R.string.gift_link_label),
                value = giftLink,
                onValueChange = { giftLink = it },
                placeholder = stringResource(R.string.gift_link_placeholder),
                singleLine = true
            )

            AppDropdownField(
                label = stringResource(R.string.wishlist_label),
                selectedValue = wishlists.find { it.id == selectedWishlistId }?.title ?: "",
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = stringResource(R.string.wishlist_placeholder),
                items = wishlists,
                onItemSelected = { wishlist ->
                    selectedWishlistId = wishlist.id
                    expanded = false
                }
            )

            AppTextField(
                label = stringResource(R.string.gift_description_label),
                value = giftDescription,
                onValueChange = { giftDescription = it },
                placeholder = stringResource(R.string.gift_description_placeholder),
                minLines = 3,
                maxLines = 5
            )
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun AppTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = false,
    minLines: Int = 1,
    maxLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            maxLines = maxLines,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDropdownField(
    label: String,
    selectedValue: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    placeholder: String,
    items: List<com.example.wishlistapp.data.model.Wishlist>,
    onItemSelected: (com.example.wishlistapp.data.model.Wishlist) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                placeholder = { Text(placeholder) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) }
            ) {
                items.forEach { wishlist ->
                    DropdownMenuItem(
                        text = { Text(wishlist.title) },
                        onClick = {
                            onItemSelected(wishlist)
                        }
                    )
                }
            }
        }
    }
}
