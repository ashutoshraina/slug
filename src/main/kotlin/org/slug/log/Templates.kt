package org.slug.log


class Templates {
  private var templates = mutableMapOf<String, String>()

  fun add(of: String, template: String) {
    templates.put(of, template)
  }

  fun getTemplate(of: String): String {
    val lookUp = templates.keys.firstOrNull { element -> of.toLowerCase().startsWith(element.toLowerCase()) }
    return if (lookUp == null) templates["default"]!! else templates[lookUp]!!
  }
}