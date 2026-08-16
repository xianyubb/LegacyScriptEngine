package org.legacy.scriptengine.kotlin.api

interface LseKotlinPlugin {
    fun onEnable(context: KotlinPluginContext)

    fun onDisable() = Unit

    companion object {
        const val API_VERSION: String = "2"
    }
}
