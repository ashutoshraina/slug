package org.slug.log

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.util.ResourceHelper

class TemplateTest {

  @Test
  fun shouldBeAbleToRegisterATemplate() {
    val templates = Templates()
    val template = ResourceHelper.readResourceFile("templates/redis.template")
    templates.add("redis", template)
    assertEquals(template, templates.getTemplate("redis"))

  }

}
