package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.events.BlockSpellCastEvent
import com.github.secretx33.magicwands.manager.SpellManager
import com.github.secretx33.magicwands.model.SpellType
import com.google.common.cache.CacheBuilder
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension
import java.util.concurrent.TimeUnit

@KoinApiExtension
class BlockSpellCastListener (
    plugin: Plugin,
    private val spellManager: SpellManager,
    private val messages: Messages,
) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    private var sentMessages = CacheBuilder.newBuilder()
        .expireAfterWrite(messageDelay, TimeUnit.MILLISECONDS)
        .build<Player, Long>()

    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    private fun BlockSpellCastEvent.trySpellCast() {
        if(target == null || target.block.type == Material.AIR) {
            isCancelled = true
            if(player.canSendCDMessage()) {
                sentMessages.put(player, System.currentTimeMillis())
                player.sendMessage(messages.get(MessageKeys.CANNOT_BLINK_TO_THERE))
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    private fun BlockSpellCastEvent.onSuccess() {
        if(spellType == SpellType.BLINK) spellManager.castBlink(this)
    }

    private fun Player.canSendCDMessage(): Boolean = sentMessages.getIfPresent(this).let { it == null || (it + messageDelay) < System.currentTimeMillis() }

    private companion object {
        const val messageDelay: Long = 750L
    }
}
