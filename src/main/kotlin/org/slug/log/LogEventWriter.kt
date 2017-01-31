package org.slug.log

object LogEventWriter {
  fun log(message: String, logFunc: Event<String>.(String) -> Unit) {
    val logEvent = LogEvent()
    logEvent.onInvocation += logFunc
    logEvent.log(message)
  }

  fun log(template: String, logFunc: Event<String>.(String) -> Unit, vararg fillers: String) {
    var index = 0
    var logMessage = template
    fillers.forEach { filler ->
      logMessage = logMessage.replace("$$index", filler)
      index = index.plus(1)
    }
    log(logMessage, logFunc)
  }
}