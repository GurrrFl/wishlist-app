package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.launch

data class WishlistOption(
    val id: Int,
    val name: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddGiftScreen(navController: NavHostController) {

    var giftName by remember { mutableStateOf(TextFieldValue("")) }
    var giftPrice by remember { mutableStateOf(TextFieldValue("")) }
    var giftLink by remember { mutableStateOf(TextFieldValue("")) }
    var selectedWishlistId by remember { mutableStateOf<Int?>(null) }
    var expanded by remember { mutableStateOf(false) } // Для DropdownMenu
    var giftDescription by remember { mutableStateOf(TextFieldValue("")) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()


    val userWishlists = listOf(
        WishlistOption(1, "День Рождения 2024"),
        WishlistOption(2, "Новый Год"),
        WishlistOption(3, "Годовщина"),
        WishlistOption(4, "Путешествие")
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Добавить желание") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBackIosNew,
                            contentDescription = "Назад"
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {
                        if (giftName.text.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Укажите название желания")
                            }
                            return@TextButton
                        }
                        if (selectedWishlistId == null) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Выберите вишлист")
                            }
                            return@TextButton
                        }

                        navController.navigateUp()
                    }) {
                        Text("Готово")
                    }
                }
            )
        },
        content = { paddingValues ->

           Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues)
                    .padding(horizontal = 24.dp)
                    .background(MaterialTheme.colorScheme.surface),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))


                Text("Название", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = giftName,
                    onValueChange = { giftName = it },
                    placeholder = { Text("Напишите название вашего желания") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    isError = giftName.text.isBlank()
                )

                Spacer(modifier = Modifier.height(12.dp))


                Text("Цена", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = giftPrice,
                    onValueChange = { giftPrice = it },
                    placeholder = { Text("Напишите примерную стоимость подарка") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Ссылка", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = giftLink,
                    onValueChange = { giftLink = it },
                    placeholder = { Text("Добавьте ссылку") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))


                Text("Вишлист", style = MaterialTheme.typography.bodySmall)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = userWishlists.find { it.id == selectedWishlistId }?.name ?: "",
                        onValueChange = { },
                        readOnly = true,
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        placeholder = { Text("Выберите вишлист") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        isError = selectedWishlistId == null
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        userWishlists.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.name) },
                                onClick = {
                                    selectedWishlistId = option.id
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))


                Text("Описание", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(
                    value = giftDescription,
                    onValueChange = { giftDescription = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 100.dp),
                    maxLines = 5,
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    )
}
