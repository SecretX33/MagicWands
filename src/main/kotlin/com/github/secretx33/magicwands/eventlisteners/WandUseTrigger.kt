package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.events.WandSpellSwitchEvent
import com.github.secretx33.magicwands.events.WandUseEvent
import com.github.secretx33.magicwands.utils.isLeftClick
import com.github.secretx33.magicwands.utils.isRightClick
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class WandUseTrigger(plugin: Plugin) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerInteractEvent.onWandInteract() {
        val item = item ?: return
        if(!item.isWand()) return

        if(isLeftClick()) {
            val event = WandUseEvent(player, item)
            Bukkit.getServer().pluginManager.callEvent(event)
            return
        }

        if(isRightClick()) {
            val event = WandSpellSwitchEvent(player, item)
            Bukkit.getServer().pluginManager.callEvent(event)
        }
    }
}