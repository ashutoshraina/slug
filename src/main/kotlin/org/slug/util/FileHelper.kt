package org.slug.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException

object FileHelper{
    private val logger: Logger? = LoggerFactory.getLogger(javaClass)
    fun readFile(file : String): String {
        val EMPTY = ""
        try {
            val classLoader = javaClass.classLoader
            val inputReader = classLoader.getResourceAsStream(file)
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
            logger?.warn("unable to load the file.")
            return EMPTY
        }
    }
}