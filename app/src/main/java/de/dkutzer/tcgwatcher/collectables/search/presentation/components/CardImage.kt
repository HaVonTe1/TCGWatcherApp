package de.dkutzer.tcgwatcher.collectables.search.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.imageLoader
import coil.request.ImageRequest
import coil.util.DebugLogger
import de.dkutzer.tcgwatcher.collectables.search.data.REFERER
import de.dkutzer.tcgwatcher.collectables.search.data.USER_AGENT
import de.dkutzer.tcgwatcher.collectables.search.data.referrer
import de.dkutzer.tcgwatcher.collectables.search.data.userAgent
import de.dkutzer.tcgwatcher.collectables.search.domain.ProductModel
import de.dkutzer.tcgwatcher.ui.theme.TCGWatcherTheme
import java.time.Instant

@Composable
fun CardImage(
    productModel: ProductModel,
    onImageClick: (item: ProductModel) -> Unit
) {
    // Define mutable state variables to keep track of the scale and offset.
    var scale by remember(Float) { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset(0f, 0f)) }


    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(productModel.imageUrl)
            .setHeader(USER_AGENT, userAgent)
            .setHeader(
                REFERER,
                referrer
            )
            .build(),

        contentDescription = productModel.id,
        modifier = Modifier
            .padding(1.dp)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    // Update the scale based on zoom gestures.
                    scale *= zoom

                    // Limit the zoom levels within a certain range (optional).
                    scale = scale.coerceIn(0.5f, 3f)

                    // Update the offset to implement panning when zoomed.
                    offset = if (scale == 1f) Offset(0f, 0f) else offset + pan
                }
            }
            .graphicsLayer(
                scaleX = scale, scaleY = scale,
                translationX = offset.x, translationY = offset.y
            )
            .fillMaxWidth()
            .clickable { onImageClick(productModel) },

        contentScale = ContentScale.FillWidth,
        imageLoader = LocalContext.current.imageLoader.newBuilder()
            .logger(DebugLogger())
            .build()
    )
}

@Composable
@PreviewLightDark
fun CardImagePreview() {
    TCGWatcherTheme {
        CardImage(
            productModel = ProductModel(
                "test",
                "Blitza",
                "blitza-1234",
                "Jolteon",
                "https://havonte.ddns.net/core/img/logo/logo.svg",
                "https://havonte.ddns.net/core/img/logo/logo.svg",
                "12.34",
                "56.78",
                Instant.now().epochSecond,
            ),
            onImageClick = {})
    }
}