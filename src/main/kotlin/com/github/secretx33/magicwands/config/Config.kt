package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Config(private val plugin: Plugin) {

    private val manager = YamlManager(plugin, "config")
    private val cache = ConcurrentHashMap<ConfigKeys, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ConfigKeys): T {
        return cache.getOrPut(key) {
            manager.get(key.configEntry, key.default)
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ConfigKeys, default: T): T {
        return cache.getOrPut(key) {
            manager.get(key.configEntry, default)
        } as T
    }

    fun set(key: ConfigKeys, value: Any) {
        cache[key] = value
        manager.set(key.configEntry, value)
    }

    fun reload() {
        cache.clear()
        manager.reload()
    }

    fun saveAll() = manager.save()
}

enum class ConfigKeys(val default: Any) {
    ENABLE_EFFECTS(true);

    val configEntry = this.name.toLowerCase(Locale.US).replace('_','-')
}