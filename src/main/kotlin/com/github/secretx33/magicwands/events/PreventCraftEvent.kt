package com.github.secretx33.magicwands.events

import com.github.secretx33.magicwands.config.Config
import com.github.secretx33.magicwands.config.ConfigKeys
import com.github.secretx33.magicwands.utils.Utils.debugMessage
import com.github.secretx33.magicwands.utils.isWand
import org.bukkit.Bukkit
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.CraftItemEvent
import org.bukkit.plugin.Plugin
import org.koin.core.component.KoinApiExtension

@KoinApiExtension
class PreventCraftEvent(plugin: Plugin) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    private fun CraftItemEvent.onCraftUsingStick() {
        // is player trying to use a wand to craft something?
        if(!view.topInventory.contents.any { it.isWand() }) return

        this.result = Event.Result.DENY
        debugMessage("Denied craft of ${recipe.result.type.name}")
    }
}