package org.slug

import org.graphstream.graph.Node

sealed class ComponentEvent {
    companion object : Event<ComponentEvent>()

    data class Crashed(val node : Node) {
        companion object : Event<Crashed>()
        fun emit() = emit(this)
    }

    data class Added(val node : Node) {
        companion object : Event<Added>()
        fun emit() = emit(this)
    }

    data class Removed(val node : Node){
        companion object : Event<Removed>()
        fun emit() = emit(this)
    }
}