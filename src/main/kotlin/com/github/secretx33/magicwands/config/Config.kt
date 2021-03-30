package com.github.secretx33.magicwands.config

import com.github.secretx33.magicwands.utils.YamlManager
import org.bukkit.plugin.Plugin
import sun.misc.Cache
import java.util.*
import java.util.concurrent.ConcurrentHashMap

class Config(plugin: Plugin): CachedYamlManager<ConfigKeys>(plugin, "config")

enum class ConfigKeys(override val configEntry: String, override val default: Any): YamlEnum {
    ENABLE_EFFECTS("enable-effects", true),
    LEAP_COOLDOWN("spells.leap.cooldown", 12),
}