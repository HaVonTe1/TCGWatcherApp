package de.dkutzer.tcgwatcher.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.R

@Composable
fun HomeScreenActivity(snackbarHostState: SnackbarHostState, modifier: Modifier = Modifier) {


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

//
//@Preview(showBackground = true)
//@Composable
//fun TestSearchPreview() {
//
//    HomeScreenActivity()
//
//}