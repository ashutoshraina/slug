package org.slug

import org.graphstream.graph.Node

open class Event<T> {
    var handlers = listOf<(T) -> Unit>()

    infix fun on(handler: (T) -> Unit) {
        handlers += handler
    }

    fun emit(event: T) {
        for (subscriber in handlers) {
            subscriber(event)
        }
    }
}

sealed class ComponentEvent {
    companion object : Event<ComponentEvent>()

    data class Crashed(val node : Node) {
        companion object : Event<Crashed>()
        fun emit() = Companion.emit(this)
    }

    data class Added(val node : Node) {
        companion object : Event<Added>()
        fun emit() = Companion.emit(this)
    }

    data class Removed(val node : Node){
        companion object : Event<Removed>()
        fun emit() = Companion.emit(this)
    }
}

