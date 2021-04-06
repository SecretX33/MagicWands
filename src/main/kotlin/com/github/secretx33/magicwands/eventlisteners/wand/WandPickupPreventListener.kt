package com.github.secretx33.magicwands.eventlisteners.wand

import com.github.secretx33.magicwands.utils.ItemUtils
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandPickupPreventListener(plugin: Plugin) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    // works in pair with the WandDropPacketListener, this one cancel the actual pickup and the other cancels the packets
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun EntityPickupItemEvent.onWandPickup() {
        val item = item.itemStack
        if(!item.isWand()) return

        val ownerUuid = ItemUtils.getWandOwnerUuid(item) ?: return
        // prevent pickup of wand dropped in the group if it's not the original owner
        if(entity.uniqueId != ownerUuid) isCancelled = true
    }
}
