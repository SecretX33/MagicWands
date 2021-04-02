package com.github.secretx33.magicwands.eventlisteners

import com.github.secretx33.magicwands.manager.LearnedSpellsManager
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin

class PlayerLeaveListener(plugin: Plugin, private val learnedSpells: LearnedSpellsManager) : Listener {

    init { Bukkit.getPluginManager().registerEvents(this, plugin) }

    @EventHandler(priority = EventPriority.MONITOR)
    private fun PlayerQuitEvent.onPlayerQuit() {
        learnedSpells.removePlayerEntries(player)
    }

}