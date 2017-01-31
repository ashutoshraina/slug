package org.slug.log

import org.junit.Assert.assertEquals
import org.junit.Test
import org.slug.util.ResourceHelper

class EventTest {

  @Test
  fun shouldBeAbleToAddAnEvent() {
    val log = LogEvent()
    log.onInvocation += { println(it) }
    log.log("hello world")

  }

  @Test
  fun shouldBeAbleToLogAnEvent() {
    LogEventWriter.log("hello world with an event", { message -> println(message) })
  }

  @Test
  fun shouldBeAbleToRegisterATemplate() {
    val templates = Templates()
    val template = ResourceHelper.readResourceFile("templates/redis.template")
    templates.add("redis", template)
    assertEquals(template, templates.getTemplate("redis"))

  }

  @Test
  fun shouldLogEventsForTemplates(){
    val templates = Templates()
    val template = ResourceHelper.readResourceFile("templates/redis.template")
    templates.add("redis", template)
    LogEventWriter.log(template, { message -> println(message)}, "set", "foo", "bar")
  }
}
