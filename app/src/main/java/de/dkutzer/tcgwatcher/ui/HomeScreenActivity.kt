package de.dkutzer.tcgwatcher.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R

@Composable
fun HomeScreenActivity(snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {


    Box(
        modifier = modifier
        .background(
            brush = Brush.verticalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.secondary
                )
            )
        )
        .padding(bottom = 32.dp)) {
        Column (
            modifier = Modifier.fillMaxSize()
        ) {
            AsyncImage(

                model = ImageRequest.Builder(LocalContext.current)
                    .data(R.drawable.tcgwatcher3)
                    .build(),
                placeholder = painterResource(R.drawable.tcgwatcher3),
                contentDescription ="AppScreen",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
                imageLoader = LocalContext.current.imageLoader.newBuilder().logger(DebugLogger()).build()
            )

        }
    }

}

//
//@Preview(showBackground = true)
//@Composable
//fun TestSearchPreview() {
//
//    HomeScreenActivity()
//
//}