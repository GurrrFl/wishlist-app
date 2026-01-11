package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.example.wishlistapp.data.model.Wishlist
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.viewmodel.WishlistViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishlistScreen(
    navController: NavHostController,
    viewModel: WishlistViewModel
) {

    var wishlistName by remember { mutableStateOf(TextFieldValue("")) }
    var eventDate by remember {
        mutableStateOf(TextFieldValue(""))
    }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var isPrivate by remember { mutableStateOf(false) }

    val generatedLink = remember {
        "https://wishlistapp.com/share/${Random.nextInt(100000, 999999)}"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Создать вишлист",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 26.dp)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBackIosNew, null)
                    }
                },
                colors = topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
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

            OutlinedTextField(
                value = wishlistName,
                onValueChange = { wishlistName = it },
                label = { Text("Название") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Done
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = eventDate,
                onValueChange = { eventDate = it },
                label = { Text("Дата") },
                shape = RoundedCornerShape(16.dp),
                placeholder = {
                    Text("Введите дату в формате: dd.MM.yyyy")
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Описание") },
                shape = RoundedCornerShape(16.dp),
                placeholder = { Text("Введите описание для вишлиста") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = isPrivate,
                    onCheckedChange = { isPrivate = it }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Приватный вишлист")
                Spacer(modifier = Modifier.width(8.dp))

            }
            Text(text = "Если вишлист приватный - только вы cможете видеть его, а желания не будут доступны для резервирования!",
            style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp))

            Spacer(modifier = Modifier.height(28.dp))
            Button(
                modifier = Modifier.fillMaxWidth().height(60.dp),
                onClick = {
                    val wishlistId = Random.nextInt(1000, 9999)

                    viewModel.addWishlist(
                        Wishlist(
                            id = wishlistId,
                            title = wishlistName.text,
                            description = description.text,
                            isPrivate = isPrivate,
                            eventDate = LocalDate.parse(
                                eventDate.text,
                                DateTimeFormatter.ofPattern("дд.мм.гггг")
                            ),
                            publicLink = if (isPrivate) null else generatedLink,
                            ownerName = "Вы",
                            gifts = emptyList()
                        )
                    )

                    navController.navigate(
                        Screen.WishlistDetails.createRoute(wishlistId)
                    ) {
                        popUpTo(Screen.Wishlists.route)
                    }
                }
            ) {
                Text("Готово", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
