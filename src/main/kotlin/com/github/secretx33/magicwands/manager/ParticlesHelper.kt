package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.model.SpellType
import org.bukkit.*
import org.bukkit.entity.Firework
import org.bukkit.persistence.PersistentDataType
import org.koin.core.component.KoinApiExtension
import java.util.logging.Logger

@KoinApiExtension
class ParticlesHelper (
    private val config: Config,
    private val log: Logger,
    private val fireworkId: NamespacedKey
) {

    fun sendFireworkParticle(loc: Location, spellType: SpellType) {
        if(!globalEffectsEnabled || !spellEffectEnabled(spellType)) return
        val world = loc.world ?: return

        val firework = world.spawn(loc, Firework::class.java) {
            it.persistentDataContainer.set(fireworkId, PersistentDataType.BYTE, 1)
            it.applyEffects(spellType)
        }
        firework.detonate()
    }

    private fun Firework.applyEffects(spellType: SpellType) {
        val type = FireworkEffect.Type.valueOf(config.get(spellType.configEffectType, "BALL"))
        val mainColor = config.get(spellType.configEffectMainColor, listOf("255, 255, 255")).map { it.toColor() }
        val fadeColor = config.get(spellType.configEffectFadeColor, emptyList<String>()).map { it.toColor() }
        val flicker = config.get(spellType.configEffectFlicker, false)
        val trail = config.get(spellType.configEffectTrail, false)

        val meta = fireworkMeta
        val effect = FireworkEffect.builder().with(type)
            .withColor(mainColor)
            .withFade(fadeColor)
            .flicker(flicker)
            .trail(trail).build()

        meta.clearEffects()
        meta.addEffect(effect)
        fireworkMeta = meta
    }

    private fun String.toColor(): Color {
        val results = COLOR_PATTERN.find(this.trim())?.groupValues
        if(results?.size != 4) {
            log.warning("${ChatColor.RED}Seems like you have malformed color string in your config file, please fix spell effect color entry with value '$this' and reload MagicWands plugin configuration.")
            return Color.FUCHSIA
        }
        val r = results[1].toInt()
        val g = results[2].toInt()
        val b = results[3].toInt()
        return try {
            Color.fromRGB(r, g, b)
        } catch(e: IllegalArgumentException) {
            log.warning("Seems like you have typed a invalid number somewhere in '$this', please only use values between 0 and 255 to write the colors. Original error message: ${e.message}")
            Color.FUCHSIA
        }
    }

    private val globalEffectsEnabled
        get() = config.get<Boolean>(ConfigKeys.ENABLE_EFFECTS)

    private val spellEffectEnabled = { spellType: SpellType -> config.get(spellType.configEffectEnabled, true) }

    private companion object {
        val COLOR_PATTERN = """^(\d+?),\s*(\d+?),\s*(\d+)$""".toRegex()
    }
}
