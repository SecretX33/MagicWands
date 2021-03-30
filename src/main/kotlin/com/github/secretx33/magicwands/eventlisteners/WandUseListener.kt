package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.events.WandSpellSwitchEvent
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.spell.SpellType
import com.github.secretx33.magicwands.spell.SpellType.*
import com.github.secretx33.magicwands.utils.isLeftClick
import com.github.secretx33.magicwands.utils.isRightClick
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandUseListener(plugin: Plugin, private val spellManager: SpellManager,) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerInteractEvent.onWandInteract() {
        val item = item ?: return
        if(!item.isWand()) return

        if(isLeftClick()) {
            val event = getSpellEvent(player, item)
            Bukkit.getServer().pluginManager.callEvent(event)
            return
        }

        if(isRightClick()) {
            val event = WandSpellSwitchEvent(player, item)
            Bukkit.getServer().pluginManager.callEvent(event)
        }
    }

    private fun getSpellEvent(player: Player, wand: ItemStack): SpellCastEvent {
        val type = spellManager.getWandSpell(wand)

        return when(type) {
            BLIND, ENSNARE, POISON, THRUST -> EntitySpellCastEvent(player, wand, type)
            BLINK -> BlockSpellCastEvent(player, wand, type)
            LEAP, VANISH -> SpellCastEvent(player, wand, type)
        }
    }
}