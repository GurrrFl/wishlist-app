package com.example.wishlistapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.wishlistapp.R
import com.example.wishlistapp.data.SearchHistoryStorage
import com.example.wishlistapp.navigation.Screen
import com.example.wishlistapp.ui.components.AppHeader
import com.example.wishlistapp.viewmodel.WishlistViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    navController: NavHostController,
    viewModel: WishlistViewModel = viewModel()
) {
    var searchQuery by remember { mutableStateOf("") }

    val context = LocalContext.current
    val historyStorage = remember { SearchHistoryStorage(context) }
    val searchHistory by historyStorage.historyFlow.collectAsState(initial = emptyList())
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            AppHeader(stringResource(R.string.search_title))
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
                .padding( 16.dp)
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.primary,
                thickness = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f).padding(end = 8.dp),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_placeholder),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    },
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = stringResource(R.string.search_clear_content_desc)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.Transparent
                    )
                )


                Button(
                    onClick = {
                        scope.launch {
                            if (searchQuery.isBlank()) {
                                snackbarHostState.showSnackbar(message = "Введите ссылку")
                                return@launch
                            }

                            val wishlist = viewModel.findWishlist(searchQuery)

                            if (wishlist != null) {
                                if (!searchHistory.contains(searchQuery)) {
                                    historyStorage.addLink(searchQuery)

                                }
                                navController.navigate(
                                    Screen.FindWishlist.createRoute(wishlist.id)
                                )
                            } else {
                                snackbarHostState.showSnackbar(
                                    "Вишлист не найден"
                                )
                            }
                        }
                    },
                    modifier = Modifier.height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Открыть")
                }
            }

            SearchHistoryComponent(
                searchHistory = searchHistory,
                onItemClicked = { query -> searchQuery = query },
                onItemDeleted = { link ->
                    scope.launch {
                    historyStorage.removeLink(link)} },
                scope = scope
            )

        }
    }
}
@Composable
fun SearchHistoryComponent(
    searchHistory: List<String>,
    onItemClicked: (String) -> Unit,
    onItemDeleted: (String) -> Unit,
    scope: CoroutineScope
) {
    if (searchHistory.isNotEmpty()) {
        Column(modifier = Modifier.padding(top = 24.dp)) {
            Text(
                text = stringResource(R.string.search_history_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(searchHistory) { item ->
                    SearchHistoryItem(
                        url = item,
                        onClick = { onItemClicked(item) },
                        onDelete = {
                            scope.launch {
                                onItemDeleted(item)
                            }
                        }
                    )
                }
            }
        }
    }
}
@Composable
private fun SearchHistoryItem(
    url: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = url,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium
            )

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Удалить",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
        }
    }
}
