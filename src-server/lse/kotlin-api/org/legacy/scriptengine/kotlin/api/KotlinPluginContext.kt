package org.legacy.scriptengine.kotlin.api

interface KotlinPluginContext {
    fun apiVersion(): String
    fun log(message: String)
}
