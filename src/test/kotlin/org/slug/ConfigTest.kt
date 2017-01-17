package org.slug

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.util.Config
import java.util.*

class ConfigTest {

  @Test
  fun getBooleanProperty() {
    val properties = Properties()
    properties.setProperty("default.swing", "false")
    val config = Config.fromValues(properties)
    assertEquals(false, config.getBooleanProperty("default.swing"))
  }

  @Test
  fun getIntegerProperty() {
    val properties = Properties()
    properties.setProperty("foo", "120")
    val config = Config.fromValues(properties)
    assertEquals(120, config.getIntegerProperty("foo"))

  }

}