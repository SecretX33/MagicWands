package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.events.WandSpellSwitchEvent
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType.*
import com.github.secretx33.magicwands.utils.*
import com.github.secretx33.magicwands.utils.Utils.consoleMessage
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
class WandUseListener(plugin: Plugin) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerInteractEvent.onWandInteract() {
        val item = item ?: return
        if(!item.isWand()) return

        if(isLeftClick()) {
            consoleMessage("player interact4")
            val event = getSpellEvent(player, item) ?: return
            Bukkit.getServer().pluginManager.callEvent(event)
            return
        }

        if(isRightClick()) {
            val event = WandSpellSwitchEvent(player, item)
            Bukkit.getServer().pluginManager.callEvent(event)
        }
    }

    private fun getSpellEvent(player: Player, wand: ItemStack): SpellCastEvent? {
        return when(val type = ItemUtils.getWandSpellOrNull(wand)) {
            null -> null
            BLIND, ENSNARE, POISON, THRUST -> EntitySpellCastEvent(player, wand, type)
            BLINK -> BlockSpellCastEvent(player, wand, type)
            else -> SpellCastEvent(player, wand, type)
        }
    }
}