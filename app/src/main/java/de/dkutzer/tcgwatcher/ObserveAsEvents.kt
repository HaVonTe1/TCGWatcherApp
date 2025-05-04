package de.dkutzer.tcgwatcher

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

private val logger = KotlinLogging.logger {}

@Composable
fun <T> ObserveAsEvents(flow: Flow<T>, onEvent: (T) -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(lifecycleOwner.lifecycle, flow) {
        logger.debug { "ObserveAsEvents" }
        lifecycleOwner.repeatOnLifecycle (Lifecycle.State.STARTED){
            logger.debug { "ObserveAsEvents repeatOnLifecycle" }
            withContext ( Dispatchers.Main.immediate ) {
                logger.debug { "ObserveAsEvents withContext" }
                flow.collect { onEvent }
            }
        }
    }
}