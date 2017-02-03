package org.slug.util

import org.apache.commons.io.IOUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slug.log.Templates
import java.io.*


object ResourceHelper {
  private val logger: Logger? = LoggerFactory.getLogger(javaClass)
  private val EMPTY = ""
  fun readResourceFile(file: String): String {
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

  fun readResourceAsMap(file: String): Map<String, Int> {
    val resource = readResourceFile(file)
    if (!resource.isNullOrEmpty()) {
      val split = resource.split("\n")
      return split.map { it -> it.split("=") }.filter { it -> !it[0].isNullOrEmpty() }.map { it -> it[0] to it[1].toInt() }.toMap()
    }
    return emptyMap()
  }

  fun readTemplates(): Templates {
    val resourceDir = "templates"
    val templates = Templates()

    val readLines = IOUtils.readLines(javaClass.classLoader.getResourceAsStream(resourceDir), Charsets.UTF_8)

    for (file in readLines) {
      val data = IOUtils.toString(javaClass.classLoader.getResourceAsStream(resourceDir + File.separator + file), Charsets.UTF_8)
      templates.add(file.split('.')[0], data)
    }
    return templates
  }
}