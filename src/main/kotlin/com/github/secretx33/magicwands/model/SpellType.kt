package com.github.secretx33.magicwands.model

import com.github.secretx33.magicwands.utils.capitalizeFully
import java.util.*

enum class SpellType {
    BLIND,
    BLINK,
    ENSNARE,
    LEAP,
    POISON,
    THRUST,
    VANISH;

    val configRoot = "spells.${name.toLowerCase(Locale.US)}"
    val configFuelAmount = "$configRoot.fuel-usage"
    val configCooldown = "$configRoot.cooldown"
    val configLearnPrice = "$configRoot.learn-price"
    val configDuration = "$configRoot.duration"
    val displayName = name.replace('_',' ').capitalizeFully()

    companion object {
        fun of(string: String): SpellType = values()
            .first { it.name.equals(string, ignoreCase = true) || it.displayName.equals(string, ignoreCase = true) }

        fun ofOrNull(string: String?): SpellType? = values()
            .firstOrNull { it.name.equals(string, ignoreCase = true) || it.displayName.equals(string, ignoreCase = true) }
    }
}