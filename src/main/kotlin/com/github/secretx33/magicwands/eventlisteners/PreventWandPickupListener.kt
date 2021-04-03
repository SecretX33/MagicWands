package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.config.MessageKeys
import com.github.secretx33.magicwands.config.Messages
import com.github.secretx33.magicwands.utils.ItemUtils
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class PreventWandPickupListener(plugin: Plugin) : Listener {

    // work in pair with the WandDropPacketListener, it cancels the packets and this one cancel the actual pickup
    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL)
    private fun EntityPickupItemEvent.onWandPickup() {
        val item = item.itemStack
        if(!item.isWand()) return

        val ownerUuid = ItemUtils.getWandOwnerUuid(item) ?: return
        // prevent pickup of wand dropped in the group if it's not the original owner
        if(entity.uniqueId != ownerUuid) isCancelled = true
    }
}