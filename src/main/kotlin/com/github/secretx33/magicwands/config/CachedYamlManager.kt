package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

abstract class CachedYamlManager<in U: YamlEnum> (
    plugin: Plugin,
    fileName: String,
) {
    private val manager = YamlManager(plugin, fileName)
    private val cache = ConcurrentHashMap<YamlEnum, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: U): T {
        return cache.getOrPut(key) {
            manager.get(key.configEntry, key.default)
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: U, default: T): T {
        return cache.getOrPut(key) {
            manager.get(key.configEntry, default)
        } as T
    }

    fun set(key: U, value: Any) {
        cache[key] = value
        manager.set(key.configEntry, value)
    }

    fun reload() {
        cache.clear()
        manager.reload()
    }

    fun saveAll() = manager.save()
}

interface YamlEnum {
    val default: Any
    val configEntry: String
}