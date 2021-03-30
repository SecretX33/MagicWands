package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Messages(plugin: Plugin) {
    private val manager = YamlManager(plugin, "messages")
    private val cache = ConcurrentHashMap<MessageKeys, String>()

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

enum class MessageKeys(val default: String) {
    CONFIGS_RELOADED("Configs were reloaded."),
    NOT_ENOUGH_FUEL("You don't have enough fuel to cast this spell."),
    CASTED_VANISH("You vanished!"),
    CANNOT_BLINK_TO_THERE("Sorry, you cannot blink to there"),
    SPELL_IN_COOLDOWN("Spell is in cooldown right now, <cooldown> seconds remaining.");

    val configEntry = this.name.toLowerCase(Locale.US).replace('_','-')
}