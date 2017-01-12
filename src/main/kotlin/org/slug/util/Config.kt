package org.slug.util

import java.util.*

object Config {
    val properties = Properties()

    fun getBooleanProperty(key: String): Boolean {
        val value = properties[key]
        return (value as String).toBoolean()
    }

    fun getIntegerProperty(key: String): Int {
        val value = properties[key]
        return (value as String).toInt()
    }

    fun getProperty(key: String): String {
        return properties[key] as String
    }

    fun fromConfig(file: String) : Config {
        val classLoader = javaClass.classLoader
        val inputStream = classLoader.getResourceAsStream(file)
        properties.load(inputStream)
        return this
    }

    fun fromValues(default : Properties): Config {
        properties.putAll(default)
        return this
    }

}
