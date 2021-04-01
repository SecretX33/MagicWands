package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.model.SpellType
import com.github.secretx33.magicwands.utils.Utils.consoleMessage
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.FireworkEffect
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Firework
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class ParticlesHelper(private val config: Config) {

    fun sendFireworkParticle(loc: Location, spellType: SpellType) {
        if(!config.get<Boolean>(ConfigKeys.ENABLE_EFFECTS) || !config.get(spellType.configEffectEnabled, true)) return
        val world = loc.world ?: return

        val firework = world.spawnEntity(loc, EntityType.FIREWORK) as Firework
        firework.applyEffects(spellType)
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
        if(results?.size != 3) {
            consoleMessage("${ChatColor.RED}Seems like you have malformed color string in your config file, please fix spell effect color entry with value '$this' and reload MagicWands plugin configuration.")
            return Color.FUCHSIA
        }
        val r = results[0].toInt()
        val g = results[1].toInt()
        val b = results[2].toInt()
        return Color.fromRGB(r, g, b)
    }

    private companion object {
        val COLOR_PATTERN = """^(\d+?),\s*(\d+?),\s*(\d+)$""".toRegex()
    }
}