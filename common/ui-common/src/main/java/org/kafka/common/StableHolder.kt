package org.kafka.common

import androidx.compose.runtime.Immutable

@Immutable
class ImmutableList<T>(val items: List<T>) {
    operator fun component1(): List<T> = items
}

fun <T : Any> List<T>.asImmutable() = ImmutableList(this)
