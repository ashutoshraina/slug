package org.slug.log

class LogEvent {
  val onInvocation = Event<String>()

  fun log(message: String) {
    onInvocation(message)
  }

}