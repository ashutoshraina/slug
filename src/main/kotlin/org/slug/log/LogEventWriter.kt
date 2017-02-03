package org.slug.log

object LogEventWriter {
  fun log(message: String, logFunc: Event<String>.(String) -> Unit): String {
    val logEvent = LogEvent()
    logEvent.onInvocation += logFunc
    logEvent.log(message)
    return message
  }

  fun log(template: String, logFunc: Event<String>.(String) -> Unit, vararg fillers: String): String {
    var index = 0
    var logMessage = template
    fillers.forEach { filler ->
      logMessage = logMessage.replace("$$index", filler)
      index = index.plus(1)
    }
    return log(logMessage, logFunc)
  }

  fun logMessage(template: String, vararg fillers: String): String {
    var index = 0
    var logMessage = template
    fillers.forEach { filler ->
      logMessage = logMessage.replace("$$index", filler)
      index = index.plus(1)
    }
    return logMessage
  }
}