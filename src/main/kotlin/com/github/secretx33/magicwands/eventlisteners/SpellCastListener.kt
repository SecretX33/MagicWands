package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.SpellCastEvent
import com.github.secretx33.magicwands.manager.SpellFuelManager
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType.*
import com.github.secretx33.magicwands.utils.ItemUtils
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import kotlin.math.ceil

@KoinApiExtension
class SpellCastListener (
    plugin: Plugin,
    private val fuelManager: SpellFuelManager,
    private val spellManager: SpellManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun SpellCastEvent.trySpellCast() {
        // TODO("enable again when learning system is implemented")
//        require(spellManager.knows(player, spellType)) { "Player is trying to use a spell he doesn't know... HOW?" }

        // not enough fuel
        if(!fuelManager.hasEnoughFuel(player, spellType)) {
            player.sendMessage(messages.get(MessageKeys.NOT_ENOUGH_FUEL))
            isCancelled = true
            return
        }

        // still in cooldown
        val cd = spellManager.getSpellCD(player, spellType)
        if(cd > 0) {
            player.sendMessage(messages.get(MessageKeys.SPELL_IN_COOLDOWN)
                .replace("<cooldown>", (ceil(cd / 1000.0).toLong()).toString())
                .replace("<spell>", spellType.displayName))
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun SpellCastEvent.onSuccess() {
        fuelManager.consumeFuel(player, spellType)

        when(spellType){
            LEAP -> spellManager.castLeap(this)
            VANISH -> spellManager.castVanish(this)
            else -> {}
        }
        ItemUtils.increaseCastCount(wand)
        spellManager.addSpellCD(player, spellType)
    }
}