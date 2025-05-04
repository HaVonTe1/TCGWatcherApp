package de.dkutzer.tcgwatcher

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

private val logger = KotlinLogging.logger {}

object EventBus {
    private val _events = MutableSharedFlow<String>(replay = 1)
    val events = _events.asSharedFlow()

    suspend fun postEvent(event: String) {
        logger.debug { "postEvent: $event" }
        _events.emit(event)
    }

    // For non-suspend contexts, use a wrapper to launch a coroutine
    fun postEventFromAnywhere(event: String, scope: CoroutineScope) {
        scope.launch {
            postEvent(event)
        }
    }
}