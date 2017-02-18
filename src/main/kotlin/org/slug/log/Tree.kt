package org.slug.log

data class Tree<out T>(val value : T, val children : Sequence<T>)