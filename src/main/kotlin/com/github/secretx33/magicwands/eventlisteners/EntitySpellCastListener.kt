package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.events.EntitySpellCastEvent
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.spell.SpellType
import com.github.secretx33.magicwands.spell.SpellType.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class EntitySpellCastListener (
    plugin: Plugin,
    private val spellManager: SpellManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private fun EntitySpellCastEvent.trySpellCast() {
        if(this.target == null) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_BLINK_TO_THERE))
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun EntitySpellCastEvent.onSuccess() {
        val target = target ?: return

        when(spellType){
            BLIND -> TODO()
            BLINK -> TODO()
            ENSNARE -> TODO()
            POISON -> TODO()
            THRUST -> TODO()
            else -> {}
        }
    }
}