package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.plugin.Plugin
import java.util.concurrent.ConcurrentHashMap

class Config(plugin: Plugin) {
    private val manager = YamlManager(plugin, "config")
    private val cache = ConcurrentHashMap<String, Any>()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String, default: T): T {
        return cache.getOrPut(key) {
            manager.get(key, default)
        } as T
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: ConfigKeys): T = get(key.configEntry, key.defaultValue) as T

    fun <T> get(key: ConfigKeys, default: T): T = get(key.configEntry, default)

    fun has(path: String): Boolean = manager.contains(path)

    fun set(key: String, value: Any) {
        cache[key] = value
        manager.set(key, value)
    }

    fun set(key: ConfigKeys, value: Any) = set(key.configEntry, value)

    fun reload() {
        cache.clear()
        manager.reload()
    }

    fun save() = manager.save()
}

enum class ConfigKeys(val configEntry: String, val defaultValue: Any) {
    COOLDOWN_NOTIFICATION_DELAY("cooldown-for-notifying-spell-is-cd", 3.0),
    DELETE_WAND_ON_DEATH("delete-wand-on-death", false),
    DISABLE_ALL_COOLDOWNS("disable-all-cooldowns", false),
    DISABLE_FUEL_USAGE("disable-fuel-usage", false),
    ENABLE_EFFECTS("enable-spell-effects", true),
    ENABLE_MESSAGE_ON_SPELL_SWITCH("enable-message-on-spell-switch", true),
    PLAYERS_ONLY_SEE_THEIR_OWN_WANDS("players-only-see-their-own-wands", true),
    REMOVE_SPELLTEACHER_WORLD_NOT_FOUND("remove-spellteacher-from-db-if-world-not-found", true),
    SPELL_FUEL("spell-fuel", listOf("COAL", "CHARCOAL")),
    VANISH_FULL_INVISIBLE("spells.vanish.full-invisible", false),
}
