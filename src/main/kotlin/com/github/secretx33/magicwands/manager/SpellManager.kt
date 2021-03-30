package com.github.secretx33.magicwands.manager

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.spell.SpellType
import com.github.secretx33.magicwands.utils.YamlManager
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.RayTraceResult
import org.koin.core.component.KoinApiExtension
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.ceil
import kotlin.math.max

@KoinApiExtension
class SpellManager(plugin: Plugin, private val config: Config, private val messages: Messages) {

    private val manager = YamlManager(plugin, "spells_learned")
    private val cooldown = ConcurrentHashMap<Pair<Player, SpellType>, Long>()

    fun getWandSpell(wand: ItemStack): SpellType {
        require(wand.isWand()) { "Item passed as wand is not a wand" }
        TODO("return the selected spell in the wand")
    }

    fun getSpellCD(player: Player, spellType: SpellType): Long
        = max(0L, (cooldown.getOrDefault(Pair(player, spellType), 0L) - System.currentTimeMillis()))

    fun addSpellCD(player: Player, spellType: SpellType) {
        cooldown[Pair(player, spellType)] = System.currentTimeMillis() + config.get(spellType.configCooldown, 7)
    }

    fun knows(player: Player, spellType: SpellType): Boolean {
        return manager.contains("${player.uniqueId}.${spellType.name}")
//        manager.getStringList(player.uniqueId.toString()).contains(spell.name)
    }

    fun teach(player: Player, spellType: SpellType) {
        val path = player.uniqueId.toString()
        val spellList = manager.getStringList(path)
        if(!spellList.contains(spellType.name)) {
            spellList.add(spellType.name)
            manager.set(path, spellList)
            manager.save()
        }
    }

    fun reload() = manager.reload()

    fun castVanish(event: SpellCastEvent) {
        val player = event.player
        val spellType = event.spellType
        val duration = config.get(spellType.configDuration, 0)
        player.addPotionEffect(PotionEffect(PotionEffectType.INVISIBILITY, duration, 1, false, false))
        player.sendMessage(messages.get(MessageKeys.CASTED_VANISH))
    }

    fun castLeap(event: SpellCastEvent) {
        val player = event.player

        val impulse = player.location.direction.apply {
            x *= 3
            y = 1.5
            z *= 3
        }
        player.velocity = impulse
    }

    fun castBlink(event: BlockSpellCastEvent) {
        val block = event.block
        val player = event.player

        player.teleport(block.location.clone().apply {
            x += 0.5
            y += 0.25
            z += 0.5
            pitch = player.location.pitch
            yaw = player.location.yaw
        })
    }
}