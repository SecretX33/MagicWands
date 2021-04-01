package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType.*
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class EntitySpellCastListener (
    plugin: Plugin,
    private val spellManager: SpellManager,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private fun EntitySpellCastEvent.trySpellCast() {
        if(target == null || target.location.world == null || target.uniqueId == player.uniqueId)
            isCancelled = true
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun EntitySpellCastEvent.onSuccess() {
        if(target == null) return

        when(spellType){
            BLIND -> spellManager.castBlind(this)
            ENSNARE -> spellManager.castEnsnare(this)
            POISON -> spellManager.castPoison(this)
            THRUST -> spellManager.castThrust(this)
            else -> {}
        }
    }
}