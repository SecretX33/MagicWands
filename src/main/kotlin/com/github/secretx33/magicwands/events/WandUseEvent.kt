package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.utils.isRightClick
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandUseEvent(plugin: Plugin) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerInteractEvent.onWandUse() {
        if(!item.isWand()) return

        if(isRightClick()) {
            
        }
    }


}