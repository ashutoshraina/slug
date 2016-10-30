package org.slug

import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException

class CSSLoader {
    fun loadCSS(): String {
        val EMPTY = ""
        try {
            val classLoader = javaClass.classLoader
            val inputReader = classLoader.getResourceAsStream("style.css")
            val buffer = StringBuffer()
            val bufferedReader = BufferedReader(InputStreamReader(inputReader, "UTF-8"))
            var character = bufferedReader.read()
            while (character != -1) {
                buffer.append(character.toChar())
                character = bufferedReader.read()
            }
            return buffer.toString()
        } catch (e: UnsupportedEncodingException) {
            return EMPTY
        } catch (e: IOException) {
            return EMPTY
        }
    }
}
