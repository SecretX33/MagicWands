package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class BlockSpellCastListener (
    plugin: Plugin,
    private val spellManager: SpellManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private fun BlockSpellCastEvent.trySpellCast() {
        if(block.type == Material.AIR) {
            player.sendMessage(messages.get(MessageKeys.CANNOT_BLINK_TO_THERE))
            isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun BlockSpellCastEvent.onSuccess() {
        when(spellType){
            SpellType.BLINK -> spellManager.castBlink(this)
            else -> {}
        }
    }
}