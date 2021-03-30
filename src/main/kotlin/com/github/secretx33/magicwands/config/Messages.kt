package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Messages(plugin: Plugin) {
    private val manager = YamlManager(plugin, "messages")
    private val cache = ConcurrentHashMap<YamlEnum, String>()

    fun get(key: MessageKeys): String {
        return cache.getOrPut(key) {
            manager.getString(key.configEntry) ?: key.default
        }
    }

    fun get(key: MessageKeys, default: String): String {
        return cache.getOrPut(key) {
            manager.getString(key.configEntry) ?: default
        }
    }

    fun reload() {
        cache.clear()
        manager.reload()
    }
}

enum class MessageKeys(override val default: String): YamlEnum {
    CONFIGS_RELOADED("Configs were reloaded."),
    SPELL_IN_COOLDOWN("Spell is in cooldown right now, <cooldown> seconds remaining.");

    override val configEntry = this.name.toLowerCase(Locale.US).replace('_','-')
}