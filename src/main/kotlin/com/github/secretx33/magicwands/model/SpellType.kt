package com.github.secretx33.magicwands.model

import com.github.secretx33.magicwands.utils.capitalizeFully
import java.util.*

enum class SpellType {
    NONE,
    BLIND,
    BLINK,
    ENSNARE,
    LEAP,
    POISON,
    THRUST,
    VANISH;

    val configFuelAmount = "spells.${name.toLowerCase(Locale.US)}.fuel-usage"
    val configCooldown = "spells.${name.toLowerCase(Locale.US)}.cooldown"
    val configLearnPrice = "spells.${name.toLowerCase(Locale.US)}.learn-price"
    val configDuration = "spells.${name.toLowerCase(Locale.US)}.duration"
    val displayName = name.replace('_',' ').capitalizeFully()

    companion object {
        fun find(string: String): SpellType = values()
            .first { it.name.equals(string, ignoreCase = true) || it.displayName.equals(string, ignoreCase = true) }
    }
}