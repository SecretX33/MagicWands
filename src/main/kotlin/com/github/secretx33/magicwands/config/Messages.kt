package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.ChatColor
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Messages(plugin: Plugin) {
    private val manager = YamlManager(plugin, "messages")
    private val cache = ConcurrentHashMap<MessageKeys, String>()

    fun get(key: MessageKeys, default: String? = null): String {
        return cache.getOrPut(key) {
            manager.getString(key.configEntry)?.correctColorCodes() ?: default ?: key.default
        }
    }

    fun reload() {
        cache.clear()
        manager.reload()
    }

    private fun String.correctColorCodes(): String = ChatColor.translateAlternateColorCodes('§', this)
}

enum class MessageKeys(val default: String) {
    CONSOLE_CANNOT_USE("Sorry, the console cannot use this command."),
    INVALID_WAND_MATERIAL("<item> cannot be an wand, please use a <allowed_material>."),
    CONFIGS_RELOADED("Configs were reloaded."),
    NOT_ENOUGH_FUEL("You don't have enough fuel to cast this spell."),
    CASTED_VANISH("You vanished!"),
    CANNOT_BLINK_TO_THERE("Sorry, you cannot blink to there"),
    SPELL_IN_COOLDOWN("Spell is in cooldown right now, <cooldown> seconds remaining."),
    ITEM_IS_ALREARY_WAND("This item is already a wand!");

    val configEntry = this.name.toLowerCase(Locale.US).replace('_','-')
}