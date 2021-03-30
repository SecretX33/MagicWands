package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.events.WandUseEvent
import com.github.secretx33.magicwands.manager.SpellFuelManager
import com.github.secretx33.magicwands.manager.SpellManager
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandUseListener (
    plugin: Plugin,
    private val fuelManager: SpellFuelManager,
    private val spellManager: SpellManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun WandUseEvent.onWandUse() {
        val spellType = spellManager.getWandSpell(wand)

        if(!fuelManager.hasEnoughFuel(player, spellType)) {
            player.sendMessage(messages.get(MessageKeys.NOT_ENOUGH_FUEL))
            return
        }
        val spellCastEvent = SpellCastEvent(player, spellType)
        Bukkit.getServer().pluginManager.callEvent(spellCastEvent)
    }
}