package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class PreventCraftListener(plugin: Plugin, private val messages: Messages) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun CraftItemEvent.onCraftUsingStick() {
        // is player trying to use a wand to craft something?
        if(!view.topInventory.contents.any { it.isWand() }) return
        val player = view.bottomInventory.holder as? Player
        result = Event.Result.DENY
        isCancelled = true
        player?.sendMessage(messages.get(MessageKeys.CANNOT_USE_WAND_TO_CRAFT))
    }
}