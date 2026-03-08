
import android.annotation.SuppressLint
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuDefaults.outlinedTextFieldColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.wishlistapp.R
import com.example.wishlistapp.data.model.Wishlist
import java.time.format.DateTimeFormatter

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoCard(wishlist: Wishlist, isSearched: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            InfoRow(
                stringResource(R.string.details_owner_label),
                wishlist.ownerName
            )
            InfoRow(
                stringResource(R.string.details_date_label),
                wishlist.eventDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            )

            InfoRow(
                stringResource(R.string.details_type_label),
                if (wishlist.isPrivate) stringResource(R.string.details_private)
                else stringResource(R.string.details_public),
                if (wishlist.isPrivate) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.primary
            )

            wishlist.description?.let {
                Text(
                    stringResource(R.string.details_description_label),
                    modifier = Modifier.padding(top = 8.dp),
                    fontWeight = FontWeight.Bold
                )

                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (!isSearched) {
                if (!wishlist.isPrivate && wishlist.publicLink != null) {
                    val context = LocalContext.current
                    val clipboardManager = LocalClipboardManager.current
                    Text(
                        stringResource(R.string.details_public_link_label),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 12.dp)
                    )

                    OutlinedTextField(
                        value = wishlist.publicLink,
                        onValueChange = {},
                        shape = RoundedCornerShape(16.dp),
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(wishlist.publicLink))
                                Toast
                                    .makeText(
                                        context,
                                        context.getString(R.string.details_copy_snackbar),
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }) {
                                Icon(Icons.Default.ContentCopy, null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface),
                        colors = outlinedTextFieldColors(
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                            disabledContainerColor = MaterialTheme.colorScheme.surface
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    color: Color = MaterialTheme.colorScheme.onBackground
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Text(value, color = color)
    }
}