package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.dkutzer.tcgwatcher.R

@Composable
fun SearchTrailingIcon(
    query: String,
    onClick: () -> Unit,
) {

    Row(modifier = Modifier.padding(end = 8.dp)) {
        if (query.isNotEmpty()) {
            IconButton(
                onClick = { onClick() }
            )
            {
                Icon(
                    imageVector = Icons.Default.Close,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = stringResource(id = R.string.clearSearch)
                )
            }
        }
    }
}